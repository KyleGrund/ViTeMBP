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
import com.vitembp.embedded.hardware.HardwareInterface;
import com.vitembp.embedded.hardware.Sensor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;

/**
 * State variable for state machine states.
 */
class ExecutionContext {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    private final HardwareInterface hardware;
    
    /**
     * Initializes a new instance of the ExecutionContext class.
     * @param hardware 
     */
    ExecutionContext() {
        this.hardware = HardwareInterface.getInterface();
    }

    /**
     * Flashes the sync light with the list of integers indicating the durations.
     * @param durations The delays between turning the sync light on and off.
     * @throws java.io.IOException If an error occurs accessing IO setting sync light state.
     */
    public void flashSyncLight(List<Integer> durations) throws IOException {
        this.hardware.flashSyncLight(durations);
    }
    
    /**
     * Waits for and returns a key press.
     * @return The character corresponding to the key pressed.
     * @throws java.lang.InterruptedException If a Thread wait for a key press
     * is interrupted.
     */
    public char getKeyPress() throws InterruptedException {
        return this.hardware.getKeyPress();
    }
    
    /**
     * Creates and returns a new capture session.
     * @return A new capture session.
     */
    CaptureSession getNewCaptureSession() {
        // collect params
        SystemConfig config = SystemConfig.getConfig();
        
        double sampleFrequency = 29.97;
        Capture dataStore = null;

        return new CaptureSession(this.hardware.getSensors(), dataStore);
    }
}
