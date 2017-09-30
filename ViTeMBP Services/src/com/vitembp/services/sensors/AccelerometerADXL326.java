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
import java.util.UUID;

/**
 * Sensor driver for the ADXL326 16G accelerometer.
 */
public class AccelerometerADXL326 extends AccelerometerThreeAxis {
    
    /**
     * A UUID representing the type of this sensor.
     */
    public static final UUID TYPE_UUID = UUID.fromString("f06ee9e1-345a-490d-8b03-a736a5e5d7bf");
    
    /**
     * Initializes a new instance of the AccelerometerADXL326 class.
     * @param name The name of the sensor as used in the system configuration.
     */
    AccelerometerADXL326(String name) {
        super(name);
    }

    @Override
    public Optional<Double> getXAxisG(Sample toDecode) {
        String data = this.getData(toDecode);
        
        // handle missing samples
        if (data == null) {
            return Optional.empty();
        }
        
        String[] values = data.split(",");
        String trimmed = values[0].trim().substring(1);
        double value = Double.parseDouble(trimmed);
        return Optional.of(value);
    }

    @Override
    public Optional<Double> getYAxisG(Sample toDecode) {
        String data = this.getData(toDecode);
        
        // handle missing samples
        if (data == null) {
            return Optional.empty();
        }
        
        String[] values = data.split(",");
        String trimmed = values[1].trim();
        double value = Double.parseDouble(trimmed);
        return Optional.of(value);
    }

    @Override
    public Optional<Double> getZAxisG(Sample toDecode) {
        String data = this.getData(toDecode);
        
        // handle missing samples
        if (data == null) {
            return Optional.empty();
        }
        
        String[] values = data.split(",");
        String trimmed = values[2].trim();
        trimmed = trimmed.substring(0, trimmed.length() - 1);
        double value = Double.parseDouble(trimmed);
        return Optional.of(value);
    }
}
