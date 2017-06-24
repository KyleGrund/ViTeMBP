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
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;

/**
 * The class containing the implementation for the StartCapture state.
 */
class StartCapture implements ControllerState {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    @Override
    public Class execute(ExecutionContext state) {
        // this represents the time to enable the sync light for
        List<Integer> syncLightDuration = Arrays.asList(new Integer[] { 2000 });
        
        try {
            // create new capture
            CaptureSession cs = state.getNewCaptureSession();
            
            // start capture
            cs.start();
            
            // enable sync LED
            state.flashSyncLight(syncLightDuration);
        } catch (IOException ex) {
            LOGGER.error("Error flashing sync light when starting new capture.", ex);
        }
        
        // transition to wait for end class
        return WaitForEnd.class;
    }
}
