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
import java.util.List;
import java.util.Map;

/**
 * A class that uses a SensorSampler to create a Capture.
 */
public class CaptureSession {
    private final SensorSampler sampler;
    private final Capture data;
    
    public CaptureSession(double frequency, List<Sensor> sensors, Capture session) {
        this.sampler = new SensorSampler(frequency, sensors, this::callback);
        this.data = session;
    }
    
    public void start() {
        this.sampler.start();
    }
    
    public void stop() {
        this.sampler.stop();
    }
    
    private void callback(Map<Sensor, String> data) {
        this.data.addSample(data);
    }
}
