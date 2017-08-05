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
package com.vitembp.services.sensors;

import com.vitembp.embedded.data.Sample;

/**
 * A class providing an interface to a three axis accelerometer sensor.
 */
public abstract class AccelerometerThreeAxis extends Sensor {
    /**
     * Initializes a new instance of the AccelerometerThreeAxis class.
     * @param name The name of the sensor.
     */
    protected AccelerometerThreeAxis(String name) {
        super(name);
    }
    /**
     * Gets the acceleration in the X axis in Gs.
     * @param toDecode The sample containing data to decode.
     * @return The acceleration in the X axis in Gs.
     */
    public abstract double getXAxisG(Sample toDecode);
    
    /**
     * Gets the acceleration in the Y axis in Gs.
     * @param toDecode The sample containing data to decode.
     * @return The acceleration in the Y axis in Gs.
     */
    public abstract double getYAxisG(Sample toDecode);
    
    /**
     * Gets the acceleration in the Z axis in Gs.
     * @param toDecode The sample containing data to decode.
     * @return The acceleration in the Z axis in Gs.
     */
    public abstract double getZAxisG(Sample toDecode);
}
