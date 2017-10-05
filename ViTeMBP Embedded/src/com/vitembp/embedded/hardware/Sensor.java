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

import java.util.UUID;

/**
 * This class provides a uniform interface for any sensor.
 */
public abstract class Sensor {
    /**
     * Gets the UUID which defines the type of sensor. This type can be used to
     * determine the format of the sample data that is created.
     * @return The UUID which defines the type of sensor.
     */
    public abstract UUID getType();
    
    /**
     * Gets the serial number of this sensor.
     * @return The serial number of the sensor.
     */
    public abstract UUID getSerial();
    
    /**
     * The initialize function will be called when the system first starts.
     */
    public abstract void initialize();
    
    /**
     * This function will read a sample from the sensor and return a UTF-8
     * String representation of the data.
     * @return A UTF-8 String representation of the data.
     */
    public abstract String readSample();
    
    /**
     * Gets the calibrator object used to calibrate this sensor.
     * @return The calibrator object used to calibrate this sensor.
     */
    public abstract Calibrator getCalibrator();
}
