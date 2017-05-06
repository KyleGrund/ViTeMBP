/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vitembp.embedded.hardware;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates a mock implementeation of the Platform class.
 */
public class MockPlatform extends Platform{
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
        return new HashMap<>();
    }
    
}
