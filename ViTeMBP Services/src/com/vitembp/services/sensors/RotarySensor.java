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
import java.util.Optional;

/**
 * Provides an implementation for rotary sensors.
 */
public abstract class RotarySensor extends Sensor {
    /**
     * Instantiates a new instance of the RotarySensor class.
     * @param name The name of the sensor.
     */
    protected RotarySensor(String name) {
        super(name);
    }
    
    /**
     * Gets the position of the sensor in degrees.
     * @param toDecode The sample containing the data to decode.
     * @return The position of the sensor in degrees.
     */
    public abstract Optional<Double> getPositionDegrees(Sample toDecode);
    
    /**
     * Gets the position of the sensor in radians.
     * @param toDecode The sample containing the data to decode.
     * @return The position of the sensor in radians.
     */
    public abstract Optional<Double> getPositionRadians(Sample toDecode);
    
    /**
     * Gets the position of the encoder as a value from 0 to 1.
     * @param toDecode The sample to decode.
     * @return The position of the encoder as a value from 0 to 1.
     */
    public abstract Optional<Double> getPositionPercentage(Sample toDecode);
}
