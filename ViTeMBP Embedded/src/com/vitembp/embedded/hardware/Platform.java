/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
