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
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;

/**
 * A factory class providing serial bus interfaces.
 */
class SerialBusFactory {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * Builds serial bus interfaces for available busses on the current system.
     * @return A set of serial bus interfaces for available busses on the current system.
     */
    static Set<SerialBus> getSerialBusses(Set<Path> portFiles) {
        Set<SerialBus> toReturn = new HashSet<>();
        
        // try to open and add each to the toReturn set
        portFiles.forEach((toOpen) -> {
            try {
                SerialBus instance = new SerialBusJssc(toOpen);
                toReturn.add(instance);
            } catch (IOException ex) {
                LOGGER.info("Could not open serial port: " + toOpen.toString(), ex);
            }
        });

        return toReturn;
    }
}
