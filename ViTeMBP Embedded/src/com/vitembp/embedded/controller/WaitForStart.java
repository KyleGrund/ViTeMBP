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

import java.io.IOException;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;

/**
 * The class containing the implementation for the WaitForStart state.
 */
class WaitForStart implements ControllerState {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

    @Override
    public Class execute(ExecutionContext state) {
        // flash the sync light to indicate that we are ready to start
        try {
            state.flashSyncLight(Arrays.asList(new Integer[] { 100, 200, 100 }));
        } catch (IOException ex) {
            LOGGER.error("IOException flashing sync light.", ex);
        }
        
        // wait for flash to complete
        try {
            Thread.sleep(400);
        } catch (InterruptedException ex) {
            LOGGER.error("IOException flashing sync light.", ex);
        }
        
        // wait for a keypress
        char key = '\0';
        try {
            key = state.getKeyPress();
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted waiting for key press.", ex);
        }
        
        // the 1 key triggers starting the capture
        if (key == '1') {
            return StartCapture.class;
        }
        
        // no valid key was pressed, return to this wait state
        return this.getClass();
    }
}
