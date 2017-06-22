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
package com.vitembp.embedded.controller;

import com.vitembp.embedded.configuration.SystemConfig;
import com.vitembp.embedded.data.Capture;
import com.vitembp.embedded.datacollection.CaptureSession;
import com.vitembp.embedded.hardware.ConsumerIOException;
import com.vitembp.embedded.hardware.Platform;
import com.vitembp.embedded.hardware.Sensor;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;

/**
 * State variable for state machine states.
 */
class ExecutionContext {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * Hardware platform interface.
     */
    private final Platform hardwarePlatform;
    
    /**
     * A queue for key press events.
     */
    private final LinkedBlockingQueue<Character> keyPresses;
    
    /**
     * Initializes a new instance of the ExecutionContext class.
     * @param hardware 
     */
    ExecutionContext(Platform hardware) {
        this.hardwarePlatform = hardware;
        
        // register key press listener to store presses into a queue
        this.keyPresses = new LinkedBlockingQueue();
        this.hardwarePlatform.setKeypadCallback(this.keyPresses::add);
    }

    /**
     * Flashes the sync light with the list of integers indicating the durations.
     */
    public void flashSyncLight(List<Integer> durations) throws IOException {
        ConsumerIOException light = this.hardwarePlatform.getSetSyncLightTarget();
        boolean lightState = false;
        light.accept(false);
        for (int wait : durations) {
            lightState = !lightState;
            light.accept(lightState);
            try {
                Thread.sleep(wait);
            } catch (InterruptedException ex) {
                LOGGER.error("Thread sleep interrupted flashing sync light.", ex);
            }
        }
        
        light.accept(false);
    }
    
    /**
     * Waits for and returns a key press.
     * @return The character corresponding to the key pressed.
     */
    public char getKeyPress() throws InterruptedException {
        return this.keyPresses.take();
    }

    /**
     * Creates and returns a new capture session.
     * @return A new capture session.
     */
    CaptureSession getNewCaptureSession() {
        // collect params
        SystemConfig config = SystemConfig.getConfig();
        
        
        
        double sampleFrequency = 29.97;
        List<Sensor> sensors = Arrays.asList(this.hardwarePlatform.getSensors().toArray(new Sensor[] { }));
        Capture dataStore = null;
        
        return new CaptureSession(sensors, dataStore);
    }
}
