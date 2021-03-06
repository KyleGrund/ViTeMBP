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
 * State that flashes LED to indicate the system is waiting to start a capture.
 */
class WaitForStartFlashLed implements ControllerState {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

    @Override
    public Class execute(ExecutionContext state) {
        // flash the sync light to indicate that we are ready to start
        try {
            state.getHardware().flashSyncLight(Arrays.asList(new Integer[] { 100, 200, 100 }));
        } catch (IOException ex) {
            LOGGER.error("IOException flashing sync light.", ex);
        }
        
        // wait for flash to complete
        try {
            Thread.sleep(400);
        } catch (InterruptedException ex) {
            LOGGER.error("IOException flashing sync light.", ex);
        }
        
        // go to wait state
        return WaitForStart.class;
    }
}
