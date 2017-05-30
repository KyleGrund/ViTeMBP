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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;

/**
 * A factory for Platform instances.
 */
class PlatformFactory {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * Builds a Platform for a SystemBoardMock instance.
     * @param board The system board to build a Platform for.
     * @return A Platform for a SystemBoardMock instance.
     */
    public static Platform build(SystemBoardMock board){
        return new PlatformFunctor(
                (Boolean t) -> {
                    if (t) {
                        LOGGER.info("Enabled synchronization light.");
                    } else {
                        LOGGER.info("Disabled synchronization light.");
                    }   
                },
                (Consumer<Character> cb) -> {
                    LOGGER.info("Set keypad callback.");
                },
                () -> {
                    Set<Sensor> toReturn = new HashSet<>();
                    toReturn.add(new AccelerometerMock("Accelerometer"));
                    return toReturn;
                });
    }
    
    /**
     * Builds a Platform for a SystemBoardUdooNeo instance.
     * @param board The system board to build a Platform for.
     * @return A Platform for a SystemBoardUdooNeo instance.
     */
    public static Platform build(SystemBoardUdooNeo board){
        return new PlatformFunctor(
                (Boolean t) -> {
                    if (t) {
                        LOGGER.info("Enabled synchronization light.");
                    } else {
                        LOGGER.info("Disabled synchronization light.");
                    }   
                },
                (Consumer<Character> cb) -> {
                    LOGGER.info("Set keypad callback.");
                },
                () -> {
                    Set<Sensor> toReturn = new HashSet<>();
                    toReturn.add(new AccelerometerMock("Accelerometer"));
                    return toReturn;
                });
    }
}
