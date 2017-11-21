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

import com.vitembp.embedded.datacollection.CaptureSession;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;

/**
 * The class containing the implementation for the EndCapture state.
 */
class EndCapture implements ControllerState {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    @Override
    public Class execute(ExecutionContext state) {
        // get the current capture session
        CaptureSession session = state.getCaptureSession();
        
        // stop the current capture session
        session.stop();
        
        try {
            // save the capture data
            session.completeCapture();
            state.getSignal().returnResult("Capture ended.");
        } catch (IOException ex) {
            LOGGER.error("Error while saving data at end of capture.", ex);
            state.getSignal().returnResult("Error, capture ended, but an error occured while saving data.");
        }
        
        return WaitForStartFlashLed.class;
    }
}
