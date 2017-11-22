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

import com.vitembp.embedded.hardware.HardwareInterface;
import com.vitembp.embedded.hardware.Sensor;
import java.util.Map;

/**
 * State that starts a sensor calibration.
 */
class SensorCalibrationStart implements ControllerState {
    @Override
    public Class execute(ExecutionContext state) {
        SignalCalibrateSensor signal = (SignalCalibrateSensor)state.getSignal();
        
        // get the current sensors map
        Map<String, Sensor> sensors = HardwareInterface.getInterface().getSensors();
        
        // make sure the sensor name is bound
        if (!sensors.containsKey(signal.getSensorName())) {
            signal.returnResult("Cannot start calibration, sensor is not connected to the system.");
            return WaitForStart.class;
        }
        
        // set a calibrator on the state
        state.setCalibrator(sensors.get(signal.getSensorName()).getCalibrator(), signal.getSensorName());
        
        // send response
        signal.returnResult("Calibration started.");
        
        // goto calibration wait state
        return SensorCalibrationWait.class;
    }
    
}
