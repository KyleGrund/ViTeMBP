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
import com.vitembp.embedded.datacollection.CaptureSession;
import com.vitembp.embedded.hardware.HardwareInterface;

/**
 * State variable for state machine states.
 */
class ExecutionContext {  
    /**
     * The capture session currently in use.
     */
    private CaptureSession captureSession;
    
    /**
     * Initializes a new instance of the ExecutionContext class.
     */
    ExecutionContext() {
    }

    /**
     * Gets the system configuration object.
     * @return The system configuration object.
     */
    public SystemConfig getConfig() {
        return SystemConfig.getConfig();
    }
    
    /**
     * Gets the capture session to use in the controller state machine.
     * @return 
     */
    CaptureSession getCaptureSession() {
        return this.captureSession;
    }

    /**
     * Creates a new capture session.
     */
    void setCaptureSession(CaptureSession session) {
        this.captureSession = session;
    }
    
    /**
     * Gets the interface to the system hardware.
     * @return The interface to the system hardware.
     */
    HardwareInterface getHardware() {
        return HardwareInterface.getInterface();
    }
}
