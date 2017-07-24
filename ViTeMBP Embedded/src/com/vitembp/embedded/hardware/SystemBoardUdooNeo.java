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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A SystemBoard implementation for the UDOO NEO.
 */
class SystemBoardUdooNeo extends SystemBoard {
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
    SystemBoardUdooNeo() throws IOException {
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
        return Paths.get("/home/udooer/config");
    }

    @Override
    public Path getLogDirectory() {
        return Paths.get("/home/udooer/logs");
    }

    @Override
    public Set<SerialBus> getSerialBusses() {
        Set<Path> busFiles = new HashSet<>();
        try {
        // builds a stream of files in /dev that start with ttyACM or ttyMMC
        // and then adds them to toReturn with the foreach call
        Files.find(
                Paths.get("/dev/"),
                0,
                (elem, prop) -> 
                        elem.getFileName().toString().startsWith("ttyACM") ||
                        elem.getFileName().toString().startsWith("ttyMCC"))
                .forEach(busFiles::add);
        } catch (IOException ex) {
            LOGGER.error("Unexpected error enumerating serial busses.", ex);
        }
        
        // use factory to create and return bus instances
        return SerialBusFactory.getSerialBusses(busFiles);
    }
}
