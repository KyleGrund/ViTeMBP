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
 * Base class providing an interface to distance based sensors.
 */
public abstract class DistanceSensor extends Sensor {
    /**
     * Initializes a new instance of the DistanceSensor class.
     * @param name The name of the sensor.
     */
    protected DistanceSensor(String name) {
        super(name);
    }
    
    /**
     * Gets the distance the sensor is reading in millimeters.
     * @param toDecode The sample containing the data to decode.
     * @return The distance the sensor is reading in millimeters.
     */
    public abstract double getDistanceMilimeters(Sample toDecode);
}
