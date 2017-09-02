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
import com.vitembp.embedded.configuration.SystemConfig;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This class provides an abstraction for peripheral interface board.
 */
public class HardwareInterface {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * Singleton instance of this class.
     */
    private static HardwareInterface singleton = null;
    
    /**
     * The platform interface to hardware.
     */
    private Platform platform;
    
    /**
     * The configuration object containing details used to initialize the interface.
     */
    private SystemConfig config;
    
    /**
     * Mapping of sensor system names to sensor control objects.
     */
    private Map<String, Sensor> sensors;
    
    /**
     * A queue for key press events.
     */
    private final LinkedBlockingQueue<Character> keyPresses;
    
    /**
     * Initializes a new instance of the HardwareInterface class.
     */
    private HardwareInterface() {
        this.keyPresses = new LinkedBlockingQueue<>();
        this.sensors  = new HashMap<>();
        this.initializeResources();
    }
    
    /**
     * Gets the bound sensors to the system.
     * @return A map of sensor names to the sensor bound to it.
     */
    public Map<String, Sensor> getSensors() {
        return this.sensors;
    }
    
    /**
     * Sets the state of the synchronization light.
     * @param state Boolean value indicating whether to illuminate sync light.
     * @throws IOException If there is an IOException while setting sync light.
     */
    public void setSyncLight(boolean state) throws IOException {
        this.platform.getSetSyncLightTarget().accept(state);
    }
    
    /**
     * Flashes the sync light with the list of integers indicating the durations.
     * @param durations The delays between turning the sync light on and off.
     * @throws java.io.IOException If an error occurs accessing sync light IO.
     */
    public void flashSyncLight(List<Integer> durations) throws IOException {
        ConsumerIOException<Boolean> light = this.platform.getSetSyncLightTarget();
        Runnable lightTask = () -> {
            try {
                // initially disable light
                boolean lightState = false;
                light.accept(false);
                
                // flip light state and wait for duration
                for (int wait : durations) {
                    lightState = !lightState;
                    light.accept(lightState);
                    Thread.sleep(wait);
                }
                
                // always disable light
                light.accept(false);
            } catch (InterruptedException ex) {
                LOGGER.error("Thread sleep interrupted flashing sync light.", ex);
            } catch (IOException ex) {
                LOGGER.error("IOException while flashing sync light.", ex);
            }
        };
        
        new Thread(lightTask, "syncLight").start();
    }
    
    /**
     * Sounds the buzzer for the number of milliseconds provided.
     * @param duration The number of milliseconds to sound the buzzer for.
     * @throws java.io.IOException If an error occurs accessing buzzer IO.
     */
    public void soundBuzzer(int duration) throws IOException {
        ConsumerIOException<Boolean> buzzer = this.platform.getBuzzerTarget();
        Runnable buzzTask = () -> {
            try {
                buzzer.accept(true);
                Thread.sleep(duration);
                buzzer.accept(false);
            } catch (InterruptedException ex) {
                LOGGER.error("Thread sleep interrupted sounding buzzer.", ex);
            }   catch (IOException ex) {
                LOGGER.error("IO exception occured sounding buzzer.", ex);
            }
        };
        new Thread(buzzTask, "Buzzer").start();
    }
    
    /**
     * Waits for and returns a key press.
     * @return The character corresponding to the key pressed.
     * @throws java.lang.InterruptedException If a Thread wait for a key press
     * is interrupted.
     */
    public Character getKeyPress() throws InterruptedException {
        return this.keyPresses.take();
    }
    
    /**
     * Waits for and returns a key press.
     * @param timeout The number of milliseconds to wait for a key press.
     * @return The character corresponding to the key pressed.
     * @throws java.lang.InterruptedException If a Thread wait for a key press
     * is interrupted.
     */
    public Character getKeyPress(int timeout) throws InterruptedException {
        return this.keyPresses.poll(timeout, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Adds a key press to the processing queue.
     * @param key The key to add.
     * @throws InterruptedException If the insertion operation is interrupted.
     */
    public void generateKeyPress(Character key) throws InterruptedException {
        this.keyPresses.put(key);
    }
    
    /**
     * Initializes resources such as resolving sensor bindings for system
     * interface.
     */
    private void initializeResources() {
        LOGGER.info("Initializing hardware resoureces.");
        this.platform = Platform.getPlatform();
        this.config = SystemConfig.getConfig();
        
        // if the configuration was not loaded from disk,
        // attempt to load defaults for the platform
        if (!this.config.initializedFromFile()) {
            try {
                this.config.createDefaultConfigFrom(this.platform.getDefaultConfigPath());
            } catch (IOException ex) {
                LOGGER.error("Could not create platform specific default configuration.", ex);
            }
        }
        
        this.platform.getSensors().forEach((s) -> LOGGER.info(
                "Found sensor, " + s.getSerial().toString() + ", of type, " +
                s.getType().toString() + "."));
        
        // names to sensor bindings
        Map<String, Sensor> bindings = new HashMap<>();
        
        // for each name
        this.config.getSensorNames().forEach((name) -> {
            // get binding
            UUID bindingAddress = this.config.getSensorBindings().get(name);

            // get matching sensor if one is available, otherwise gets null
            Sensor match = this.platform.getSensors().stream()
                    .filter((d) -> d.getSerial().equals(bindingAddress))
                    .findFirst()
                    .orElse(null);
            
            if (match == null) {
                LOGGER.info("Could not bind sensor \"" + name + "\" to \"" + bindingAddress + "\".");
            } else {
                LOGGER.info("Sensor \"" + name + "\" bound to \"" + bindingAddress + "\"");
            }
            
            // add sensor to bindings
            bindings.put(name, match);
        });
        
        // register key press listener to store presses into a queue
        this.platform.setKeypadCallback(this.keyPresses::add);
        
        // save updated bindings
        this.sensors = bindings;
    }
    
    /**
     * Gets an instance of the HardwareInterface class used to access the
     * hardware the program is currently executing on.
     * @return An instance of the HardwareInterface class for the hardware the
     * program is currently executing on.
     */
    public synchronized static HardwareInterface getInterface() {
        // build the singleton instance if it has not been built already
        if (HardwareInterface.singleton == null) {
            HardwareInterface.singleton = new HardwareInterface();
        }
        
        return HardwareInterface.singleton;
    }
}
