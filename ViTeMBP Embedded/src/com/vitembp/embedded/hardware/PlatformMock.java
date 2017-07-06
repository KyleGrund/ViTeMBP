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

import com.vitembp.embedded.data.ConsumerIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates a mock implementation of the Platform class.
 */
class PlatformMock extends Platform{
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    @Override
    public ConsumerIOException<Boolean> getSetSyncLightTarget() {
        return (Boolean t) -> {
            if (t) {
                LOGGER.info("Enabled synchronization light.");
            } else {
                LOGGER.info("Disabled synchronization light.");
            }   
        };
    }

    @Override
    public void setKeypadCallback(Consumer<Character> callback) {
        LOGGER.info("Set keypad callback.");
    }

    @Override
    public Set<Sensor> getSensors() {
        Set<Sensor> toReturn = new HashSet<>();
        toReturn.add(new AccelerometerMock("Accelerometer"));
        return toReturn;
    }

    @Override
    Path getDefaultConfigPath() {
        return Paths.get("/com/vitembp/embedded/configuration/DefaultConfigMock.xml");
    }
}
