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
import com.vitembp.embedded.data.CaptureTypes;
import com.vitembp.embedded.data.UuidStringTransporter;
import com.vitembp.embedded.data.UuidStringTransporterFactory;
import com.vitembp.embedded.hardware.HardwareInterface;
import org.apache.logging.log4j.LogManager;

/**
 * The class containing the implementation for the New state.
 */
class New implements ControllerState {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    @Override
    public Class execute(ExecutionContext state) {
        SystemConfig config = state.getConfig();
        
        // initialize all sensors
        HardwareInterface.getInterface().getSensors().forEach((name, sensor) -> {
            if (sensor != null) {
                LOGGER.debug("Initializing sensor \"" + name + "\".");
                sensor.initialize();
            } else {
                LOGGER.error("Configured sensor: \"" + name + "\" not bound to hardware.");
            }
        });
        
        // initialize database synchronization
        if (config.getUploadToCloud()) {
            try {
                boolean deleteOnUpload = config.getDeleteOnUploadToCloud();

                // build the uploading transport using the DynamoDB as the target
                // as this is the cloud destination
                UuidStringTransporter transport = UuidStringTransporterFactory.build(config.getCaptureType(), CaptureTypes.AmazonDynamoDB, deleteOnUpload);

                // start the synchronization thread
                transport.startSync();
            } catch (InstantiationException ex) {
                LOGGER.error("Could not start the cloud upload services.", ex);
            }
        }
        
        return WaitForStart.class;
    }
}
