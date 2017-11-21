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

import com.vitembp.embedded.hardware.Calibrator;

/**
 * State that waits for external input to control a sensor calibration.
 */
class SensorCalibrationStatus implements ControllerState {
    /**
     * The response that is sent when a 
     */
    static final String NOT_RUNNING_RESPONSE = "{\"isCalibrating\":false,\"stepPrompt\":\"\"}";
    
    @Override
    public Class execute(ExecutionContext state) {
        Calibrator calibrator = state.getCalibrator();
        
        // if there is no calibrator a calibraiton has not been started, simply
        // return an empty json response
        if (calibrator == null) {
            state.getSignal().returnResult(NOT_RUNNING_RESPONSE);
        }
        
        // return response
        state.getSignal().returnResult(getJsonResponse(calibrator));
        
        // no valid signal received, return to this state
        return this.getClass();
    }
    
    /**
     * Builds a JSON status object for the given calibrator.
     * @param calibrator The calibrator to build a status response for.
     * @return The calibrator status as a JSON string.
     */
    static String getJsonResponse(Calibrator calibrator) {
        return "{\"isCalibrating\":" +
                Boolean.toString(calibrator.isCalibrating()) +
                ",\"stepPrompt\":\"" +
                calibrator.getStepPrompt() +
                "\"}";
    }
}
