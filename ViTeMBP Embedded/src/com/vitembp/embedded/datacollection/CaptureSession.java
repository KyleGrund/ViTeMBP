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
package com.vitembp.embedded.datacollection;

import com.vitembp.embedded.data.Capture;
import com.vitembp.embedded.hardware.Sensor;
import java.io.IOException;
import java.util.Map;

/**
 * A class that uses a SensorSampler to create a Capture.
 */
public class CaptureSession {
    /**
     * The sampler that is taking data from the sensors.
     */
    private final SensorSampler sampler;
    
    /**
     * The capture to save data to.
     */
    private final Capture data;
    
    /**
     * Initializes a new instance of the CaptureSession class.
     * @param sensors The sensors to sample data from.
     * @param session The capture to save data to.
     */
    public CaptureSession(Map<String, Sensor> sensors, Capture session) {
        this.sampler = new SensorSampler(session.getSampleFrequency(), sensors, this::callback);
        this.data = session;
    }
    
    /**
     * Start capturing data.
     */
    public void start() {
        this.sampler.start();
    }
    
    /**
     * Stops capturing data.
     */
    public void stop() {
        this.sampler.stop();
    }
    
    /**
     * Saves the current capture.
     * @throws IOException If an IOException occurs while saving the capture.
     */
    public void saveCapture() throws IOException {
        this.data.save();
    }
    
    /**
     * Callback target which stores data from the sampler.
     * @param data 
     */
    private void callback(Map<String, String> data) {
        this.data.addSample(data);
    }
}
