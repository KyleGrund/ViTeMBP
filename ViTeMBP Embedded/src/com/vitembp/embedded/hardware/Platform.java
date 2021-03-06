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
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Class providing an interface to the ViTeMBP hardware platform.
 */
abstract class Platform {
    /**
     * Lock object for singleton creation critical section.
     */
    private static final Object SINGLETON_CREATE_LOCK = new Object();
    
    /**
     * The system board singleton.
     */
    private static Platform singletonInstance = null;
    
    /**
     * Gets a Consumer which takes a Boolean value and sets the state of the
     * synchronization light to on if it is true, or off if it is false.
     * @return A Consumer which can be used to set the state of the
     * synchronization light.
     */
    public abstract ConsumerIOException<Boolean> getSetSyncLightTarget();
    
    /**
     * Gets a Consumer which takes a Boolean value and sets the state of the
     * buzzer to on if it is true, or off if it is false.
     * @return A Consumer which can be used to set the state of the buzzer.
     */
    public abstract ConsumerIOException<Boolean> getBuzzerTarget();
    
    /**
     * Sets the callback function which will process key press events.
     * @param callback The function which will process key press events.
     */
    public abstract void setKeypadCallback(Consumer<Character> callback);
    
    /**
     * Gets the sensor control interface objects.
     * @return A Map of String sensor names to their control interface object.
     */
    public abstract Set<Sensor> getSensors();
    
    /**
     * Gets the path to the default configuration file for the platform.
     */
    abstract Path getDefaultConfigPath();
    
    /**
     * Initializes platform resources.
     */
    abstract void initialize();
    
    /**
     * Sets the interface metric of the wired Ethernet interface.
     * @param metric The metric to set the interface to.
     */
    abstract void setWiredEthernetMetric(int metric) throws IOException;
    
    /**
     * Sets the interface metric of the wireless Ethernet interface.
     * @param metric The metric to set the interface to.
     */
    abstract void setWirelessEthernetMetric(int metric) throws IOException;
    
    /**
     * Sets the interface metric of the Bluetooth interface.
     * @param metric The metric to set the interface to.
     */
    abstract void setBluetoothMetric(int metric) throws IOException;
    
    /**
     * Returns a platform object for the system hardware that the program 
     * is currently executing on.
     * @return A platform object for the system hardware that the program 
     * is currently executing on.
     */
    public static Platform getPlatform() {
        // synchronize singleton creation check critical section
        synchronized (Platform.SINGLETON_CREATE_LOCK) {
            // create the singleton instance if it doesn't already exist
            if (Platform.singletonInstance == null) {
                // get the system board and use factory to build plaform
                Platform.singletonInstance = PlatformFactory.build(SystemBoard.getBoard());
                
                // initialize the platform
                Platform.singletonInstance.initialize();
            }
        }
        
        // return singleton
        return Platform.singletonInstance;
    }
}
