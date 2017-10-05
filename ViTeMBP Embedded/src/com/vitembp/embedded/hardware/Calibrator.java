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
package com.vitembp.embedded.hardware;

/**
 * A class that provides a uniform calibration routine for sensors.
 */
public abstract class Calibrator {
    /**
     * Returns a boolean value indicating whether there is another step to
     * complete calibration.
     * @return A boolean value indicating whether there is another step to
     * complete calibration.
     */
    public abstract boolean isCalibrating();
    
    /**
     * Gets the string to display to the user to instruct them on how to perform
     * the next calibration step.
     * @return The string to display to the user.
     */
    public abstract String getStepPrompt();
    
    /**
     * Indicates to the calibrator the the user has selected to move to the next
     * step.
     */
    public abstract void nextStep();
    
    /**
     * Returns the calibration data when completed.
     * @return The string representing calibration data.
     */
    public abstract String getCalibrationData();
}
