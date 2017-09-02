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

import org.apache.logging.log4j.LogManager;

/**
 * The class containing the implementation for the WaitForStart state.
 */
class WaitForStart implements ControllerState {
    /**
     * The time to wait for a key press before going to sensor idle.
     */
    private static final int KEY_PRESS_WAIT_TIME_MS = 250;
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

    @Override
    public Class execute(ExecutionContext state) {
        // wait for a keypress
        Character key = '\0';
        try {
            key = state.getHardware().getKeyPress(KEY_PRESS_WAIT_TIME_MS);
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted waiting for key press.", ex);
        }
        
        // the 1 key triggers starting the capture
        if (key != null && key == '1') {
            return CreateCapture.class;
        }
        
        // no valid key was pressed, run idle sensor state
        return IdleSensor.class;
    }
}
