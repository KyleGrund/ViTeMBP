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
     * Error message for invalid calibration data.
     */
    private static final String CALIBRATION_DATA_FORMAT_ERROR = "Calibration data must be of the format: \"[([xMin],[xMax]),([yMin],[yMax]),([zMin],[zMax])]\".";
    
    /**
     * A UUID representing the type of this sensor.
     */
    public static final UUID TYPE_UUID = UUID.fromString("f06ee9e1-345a-490d-8b03-a736a5e5d7bf");
    
    /**
     * The sensor calibration data.
     */
    private final double calXMin, calXMax, calYMin, calYMax, calZMin, calZMax;
    
    /**
     * Initializes a new instance of the AccelerometerADXL326 class.
     * @param name The name of the sensor as used in the system configuration.
     * @param calData The sensor calibration data.
     */
    AccelerometerADXL326(String name, String calData) {
        super(name);
        if (!calData.startsWith("[") || !calData.endsWith("]")) {
            throw new IllegalArgumentException(CALIBRATION_DATA_FORMAT_ERROR);
        }
        
        // split into the x, y, z min/max pairs
        String[] calPairs = calData.substring(1, calData.length() - 1).split(",");
        if (calPairs.length != 6 ||
                !calPairs[0].startsWith("(") || !calPairs[1].endsWith(")") ||
                !calPairs[2].startsWith("(") || !calPairs[3].endsWith(")") ||
                !calPairs[4].startsWith("(") || !calPairs[5].endsWith(")")) {
            throw new IllegalArgumentException(CALIBRATION_DATA_FORMAT_ERROR);
        }
        
        this.calXMin = Double.parseDouble(calPairs[0].substring(1));
        this.calXMax = Double.parseDouble(calPairs[1].substring(0, calPairs[1].length() - 1));
        this.calYMin = Double.parseDouble(calPairs[2].substring(1));
        this.calYMax = Double.parseDouble(calPairs[3].substring(0, calPairs[3].length() - 1));
        this.calZMin = Double.parseDouble(calPairs[4].substring(1));
        this.calZMax = Double.parseDouble(calPairs[5].substring(0, calPairs[5].length() - 1));
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
        
        // apply calibration
        if (value < 0) {
            value /= this.calXMin;
        } else {
            value /= this.calXMax;
        }
        
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
        
        // apply calibration
        if (value < 0) {
            value /= this.calYMin;
        } else {
            value /= this.calYMax;
        }
        
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
        
        // apply calibration
        if (value < 0) {
            value /= this.calZMin;
        } else {
            value /= this.calZMax;
        }
        
        return Optional.of(value);
    }
}
