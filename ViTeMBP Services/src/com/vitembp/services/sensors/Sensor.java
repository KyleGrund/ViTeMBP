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
 * A base class providing an interface for sensors.
 */
public abstract class Sensor {
    /**
     * The name of the sensor.
     */
    private final String name;
    
    /**
     * Initializes a new instance of the Sensor class.
     * @param name The name of the sensor.
     */
    protected Sensor(String name) {
        this.name = name;
    }
    
    /**
     * Gets the name of the sensor.
     * @return The name of the sensor.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Gets the data for this sensor from a sample.
     * @param toDecode The sample containing data to decode.
     * @return The sample data for this sensor.
     */
    protected String getData(Sample toDecode) {
        return toDecode.getSensorData().get(this.name);
    }
}
