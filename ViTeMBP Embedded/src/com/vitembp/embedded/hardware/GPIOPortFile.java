/*
 * Video Telemetry for Mountain Bike Platform back-end services.
 * Copyright (C) 2017 Kyle Grund
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vitembp.embedded.hardware;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;

/**
 * A GPIOPort implementation for the Linux file system interface.
 */
class GPIOPortFile extends GPIOPort {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * The path to the direction control file for this port.
     */
    private Path directionPath;

    /**
     * The path to the port state control file for this port.
     */
    private Path valuePath;
    
    /**
     * The name of this port.
     */
    private String portname;
    
    /**
     * A boolean value indicating whether the direction of the port has been set
     * and thus whether setDirection member is valid.
     */
    private boolean hasDirectionBeenSet = false;
    
    /**
     * A GPIODirection indicating what the direction of the port has been set to.
     */
    private GPIODirection setDirection;
    
    /**
     * Initializes a new instance of the GPIOPortFile class.
     * @param portDir The directory containing the GPIO interface files.
     * @throws java.io.IOException
     */
    private GPIOPortFile(Path portDir) throws IOException {
        // validate the supplied directory
        if (!portDir.toFile().exists()) {
            LOGGER.error("GPIO path \"" + portDir.toString() + "\" not found.");
            throw new IllegalArgumentException("GPIO directory not found.");
        }
        
        // make sure the directory follows the Linux naming  convention
        String portDirName = portDir.getName(portDir.getNameCount() - 1).toString();
        if (!portDirName.startsWith("gpio")) {
            LOGGER.error("GPIO path \"" + portDir.toString() + "\" must start with \"gpio\".");
            throw new IllegalArgumentException("GPIO directory must start with \"gpio\".");
        }
        
        // parse out the gpio number and use it to generate this port's name
        try {
            int gpioNumber = Integer.parseUnsignedInt(portDirName.substring(4));
            this.portname = "gpio" + Integer.toString(gpioNumber);
        } catch (NumberFormatException ex) {
            LOGGER.error("GPIO path \"" + portDir.toString() + "\" must be of the form \"gpio[port number]\".");
            throw new IllegalArgumentException("GPIO directory must be of the form \"gpio[port number]\".", ex);
        }
        
        // verify the file which controls the direction of the port exists
        Path direction = portDir.resolve("direction");
        if (!direction.toFile().exists()) {
            LOGGER.error("GPIO direction file not found in path \"" + portDir.toString() + "\".");
            throw new IOException("GPIO direction file not found.");
        }
        this.directionPath = direction;
        
        // verify the file which controls the port state exists
        Path value = portDir.resolve("value");
        if (!value.toFile().exists()) {
            LOGGER.error("GPIO value file not found in path \"" + portDir.toString() + "\".");
            throw new IOException("GPIO value file not found.");
        }
        this.valuePath = value;
    }
    
    static Set<GPIOPort> buildPortsForPath(Path ports) throws IOException {
        Set<GPIOPort> toReturn = new HashSet<>();
        
        // make sure ports is a directory
        if (!Files.isDirectory(ports)) {
            LOGGER.error("Cannot build GPIO ports for a path which is not a directory.");
            throw new IOException("The ports parameter must be a directory.");
        }
        
        // enumerate all paths from directory of format gpio###
        Stream<Path> toTest = Files.find(
                ports,
                1,
                (file, attr) -> {
                    // file must start with gpio
                    if (!file.getFileName().toString().startsWith("gpio")) {
                        return false;
                    }
                    
                    // the rest of the file must be in a uint
                    try {
                        int gpioNumber = Integer.parseUnsignedInt(file.getFileName().toString().substring(4));
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                    
                    // the file must be a directory
                    return Files.isDirectory(file); },
                FileVisitOption.FOLLOW_LINKS);
            
        // build GPIOPortFiles for matching files
        toTest.forEach((dir) -> {
            GPIOPort toAdd;
            try {
                toAdd = new GPIOPortFile(dir);
                toReturn.add(toAdd);
            } catch (IOException ex) {
                LOGGER.error("Could not create a GPIOPortFile instance for the path: \"" + dir.toString() + "\".", ex);
            }
        });
        
        return toReturn;
    }
    
    @Override
    public String getName() {
        return this.portname;
    }

    @Override
    public GPIODirection getDirection() throws IOException {
        // read lines from the direction file
        List<String> dirLines = Files.readAllLines(this.directionPath);
        
        // the file should only have a single line indicating the direction
        if (dirLines.size() != 1) {
            throw new IOException(
                    "Unexpected number of lines read from direction file for " +
                            this.portname +
                            ", read " +
                            Integer.toString(dirLines.size()) +
                            " lines.");
        }
        
        // parse the read line
        String line = dirLines.get(0);
        switch (line) {
            case "in":
                return GPIODirection.Input;
            case "out":
                return GPIODirection.Output;
            default:
                throw new IOException("Unexpected data read from direction file: \"" + line + "\".");
        }
    }

    @Override
    public void setDirection(GPIODirection direction) throws IOException {
        // write in or out to the direction file to set the port direction
        switch (direction) {
            case Input:
                Files.write(this.directionPath, Arrays.asList(new String[] { "in" }), StandardOpenOption.WRITE);
                this.setDirection = GPIODirection.Input;
                break;
            case Output:
                Files.write(this.directionPath, Arrays.asList(new String[] { "out" }), StandardOpenOption.WRITE);
                this.setDirection = GPIODirection.Output;
                break;
            default:
                throw new IOException("Linux GPIO file interface only supports input or output modes.");
        }
        
        this.hasDirectionBeenSet = true;
    }

    @Override
    public boolean getValue() throws IOException {
        // read lines from the value file
        List<String> valueLines = Files.readAllLines(this.valuePath);
        
        // the file should only have a single line indicating the port state
        if (valueLines.size() != 1) {
            throw new IOException(
                    "Unexpected number of lines read from value file for " +
                            this.portname +
                            ", read " +
                            Integer.toString(valueLines.size()) +
                            " lines.");
        }
        
        // parse the read line
        String line = valueLines.get(0);
        switch (line) {
            case "1":
                return true;
            case "0":
                return false;
            default:
                throw new IOException("Unexpected data read from value file: \"" + line + "\".");
        }
    }

    @Override
    public void setValue(boolean value) throws IOException {
        // make sure port is set to output
        if (!this.hasDirectionBeenSet || this.setDirection != GPIODirection.Output) {
            this.setDirection(GPIODirection.Output);
        }
        
        // write a 1 or 0 to the value file to set it to high or low respectively
        if (value) {
            Files.write(valuePath, Arrays.asList(new String[] { "1" }), StandardOpenOption.WRITE);
        } else {
            Files.write(valuePath, Arrays.asList(new String[] { "0" }), StandardOpenOption.WRITE);
        }
    }
}
