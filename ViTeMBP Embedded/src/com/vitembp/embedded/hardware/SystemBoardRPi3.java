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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A SystemBoard implementation for the UDOO NEO.
 */
class SystemBoardRPi3 extends SystemBoard {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * The GPIO ports available for this system board.
     */
    private final Set<GPIOPort> gpioPorts;
    
    /**
     * The I2C busses for this system.
     */
    private final Set<I2CBus> i2cBusses;
    
    /**
     * Initializes a new instance of the SystemBoardUdooNeo.
     * @throws IOException If there is an IOException communicating with the
     * system hardware.
     */
    SystemBoardRPi3() throws IOException {
        // builds GPIO port interfaces
        this.gpioPorts = GPIOPortFile.buildPortsForPath(Paths.get("/sys/class/gpio"));
        
        // builds I2C bus interfaces.
        this.i2cBusses = I2CBusI2CDev.buildBusesForPath(Paths.get("/sys/class/i2c-dev"));
    }
            
    @Override
    public Set<I2CBus> getI2CBusses() {
        return this.i2cBusses;
    }

    @Override
    public Set<GPIOPort> getGPIOPorts() {
        return this.gpioPorts;
    }

    @Override
    public Path getConfigDirectory() {
        return Paths.get("/home/pi/config");
    }

    @Override
    public Path getLogDirectory() {
        return Paths.get("/home/pi/logs");
    }

    @Override
    public Set<SerialBus> getSerialBusses() {
        Set<Path> busFiles = new HashSet<>();
  
        // get an array of files in /dev that start with ttyACM or ttyMMC
        File[] busses = Paths.get("/dev/")
            .toFile()
            .listFiles((dir, name) -> name.startsWith("ttyACM"));
        
        // then add their paths to toReturn
        for (File toAdd : busses) {
            busFiles.add(toAdd.toPath());
        }
        
        // use factory to create and return bus instances
        return SerialBusFactory.getSerialBusses(busFiles);
    }
    
    /**
     * Returns a boolean value indicating whether the board is detected.
     * @return A boolean value indicating whether the board is detected.
     */
    public static boolean isBoardDetected() {
        // the RPi3 will have it's board type in the base board model file
        Path modelFile = Paths.get("/sys/firmware/devicetree/base/model");

        // if the version file contains udooneo we are running on a RPi 3
        if (modelFile.toFile().exists()) {
            try {
                List<String> versionLines = Files.readAllLines(modelFile);
                if (versionLines.size() > 0) {
                    if (versionLines.get(0).contains("Raspberry Pi 3")) {
                        // indicate a RPi 3 board was found
                        return true;
                    }
                }
            } catch (IOException ex) {
                LOGGER.error("Model file exists but could not be read.", ex);
            }
        }

        return false;
    }
}