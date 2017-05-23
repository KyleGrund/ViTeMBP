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

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class providing an interface to the ViTeMBP hardware platform.
 */
public abstract class Platform {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Gets a Consumer which takes a Boolean value and sets the state of the
     * synchroniation light to on if it is true, or off if it is false.
     * @return A Consumer which can be used to set the state of the
     * synchroniation light.
     */
    public abstract Consumer<Boolean> getSetSyncLightTarget();
    
    /**
     * Sets the callback function which will process keypress events.
     * @param callback The function which will process keypress events.
     */
    public abstract void setKeypadCallback(Consumer<Character> callback);
    
    /**
     * Gets a Map of UUID sensor types to their control interface object
     * instance.
     * @return A Map of UUID sensor types to their control interface object.
     */
    public abstract Map<UUID, Sensor> getSensorMap();
    
    /**
     * Returns a platform object for the system hardware that the program 
     * is currently executing on.
     * @return A platform object for the system hardware that the program 
     * is currently executing on.
     */
    public static Platform getPlatform() {
        LOGGER.info("Using mock system platform.");
        return new MockPlatform();
    }
}
