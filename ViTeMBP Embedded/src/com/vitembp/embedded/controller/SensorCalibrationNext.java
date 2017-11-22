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
 * State that starts a sensor calibration.
 */
class SensorCalibrationNext implements ControllerState {
    @Override
    public Class execute(ExecutionContext state) {
        Calibrator calibrator = state.getCalibrator();
        
        // go to next step
        calibrator.nextStep();
        
        // if the calibration is finished go to wait for start
        if (!calibrator.isCalibrating()) {
            state.getSignal().returnResult("Success, calibration complete.");
            
            // clear calibrator state
            state.setCalibrator(null, "");
            
            // go to idle
            return WaitForStart.class;
        }

        // send response
        state.getSignal().returnResult("Success, transitioned to next calibration step.");
        
        // goto calibration wait state
        return SensorCalibrationWait.class;
    }
    
}
