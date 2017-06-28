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
import com.vitembp.embedded.data.CaptureFactory;
import com.vitembp.embedded.data.CaptureTypes;
import com.vitembp.embedded.datacollection.CaptureSession;
import com.vitembp.embedded.hardware.HardwareInterface;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;

/**
 * The class containing the implementation for the StartCapture state.
 */
class CreateCapture implements ControllerState {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    @Override
    public Class execute(ExecutionContext state) {
        HardwareInterface hardware = state.getHardware();
        SystemConfig config = state.getConfig();
        
        // create new capture
        // make map of sensor names to types
        Map<String, UUID> sensorTypes = new HashMap<>();
        hardware.getSensors().forEach((n, s) -> {
            if (s == null) {
                LOGGER.error("Sensor \"" + n + "\" not bound.");
            } else {
                sensorTypes.put(n, s.getType());
            }
        });

        // create the capture data store
        Capture dataStore = null;
        
        try {
            dataStore = CaptureFactory.buildCapture(config.getCaptureType(), config.getSamplingFrequency(), sensorTypes);
        } catch (InstantiationException ex) {
            LOGGER.error("Could not create capture for configured type \"" + config.getCaptureType().toString() + "\".", ex);
        }

        if (dataStore == null) {
            try {
                dataStore = CaptureFactory.buildCapture(
                        CaptureTypes.InMemory,
                        config.getSamplingFrequency(),
                        sensorTypes);
            } catch (InstantiationException ex) {
                LOGGER.error("Could not create a data Capture target.", ex);
                throw new IllegalStateException("Could not create a data Capture target.", ex);
            }
        }

        // build and return a new capture session
        CaptureSession captureSession = new CaptureSession(hardware.getSensors(), dataStore);

        state.setCaptureSession(captureSession);
        
        // transition to wait for end class
        return StartCapture.class;
    }
}
