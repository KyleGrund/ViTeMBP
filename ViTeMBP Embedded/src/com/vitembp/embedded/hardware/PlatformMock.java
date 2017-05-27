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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates a mock implementation of the Platform class.
 */
public class PlatformMock extends Platform{
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    @Override
    public Consumer<Boolean> getSetSyncLightTarget() {
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
    public Map<UUID, Sensor> getSensorMap() {
        HashMap<UUID, Sensor> toReturn = new HashMap<>();
        toReturn.put(UUID.randomUUID(), new AccelerometerMock("Mock Accelerometer"));
        return toReturn;
    }
}
