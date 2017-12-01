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
import java.util.function.Function;

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
     * The function which applies the calibration data.
     */
    private final Function<Double, Double> calFunctionX;
    
    /**
     * The function which applies the calibration data.
     */
    private final Function<Double, Double> calFunctionY;
    
    /**
     * The function which applies the calibration data.
     */
    private final Function<Double, Double> calFunctionZ;
    
    /**
     * Initializes a new instance of the AccelerometerADXL326 class.
     * @param name The name of the sensor as used in the system configuration.
     * @param calData The sensor calibration data.
     */
    AccelerometerADXL326(String name, String calData) {
        super(name);
        if (calData != null && !calData.isEmpty()) {
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

            // parse cal values
            double calXMin = Double.parseDouble(calPairs[0].substring(1));
            double calXMax = Double.parseDouble(calPairs[1].substring(0, calPairs[1].length() - 1));
            double calYMin = Double.parseDouble(calPairs[2].substring(1));
            double calYMax = Double.parseDouble(calPairs[3].substring(0, calPairs[3].length() - 1));
            double calZMin = Double.parseDouble(calPairs[4].substring(1));
            double calZMax = Double.parseDouble(calPairs[5].substring(0, calPairs[5].length() - 1));

            // create cal functions
            this.calFunctionX = val -> (val < 0) ? val / calXMin : val / calXMax;
            this.calFunctionY = val -> (val < 0) ? val / calYMin : val / calYMax;
            this.calFunctionZ = val -> (val < 0) ? val / calZMin : val / calZMax;
        } else {
            // no cal data, so use identity function
            this.calFunctionX = val -> val;
            this.calFunctionY = val -> val;
            this.calFunctionZ = val -> val;
        }
    }

    @Override
    public Optional<Double> getXAxisG(Sample toDecode) {
        String data = this.getData(toDecode);
        
        // handle missing samples
        if (data == null || "".equals(data)) {
            return Optional.empty();
        }
        
        String[] values = data.split(",");
        if (values.length < 3) {
            throw new IllegalStateException("Invalid data found parsing ADXL326 X axis, \"" + data + "\"");
        }
        String trimmed = values[0].trim().substring(1);
        double value = Double.parseDouble(trimmed);
        
        // return calibrated value
        return Optional.of(this.calFunctionX.apply(value));
    }

    @Override
    public Optional<Double> getYAxisG(Sample toDecode) {
        String data = this.getData(toDecode);
        
        // handle missing samples
        if (data == null || "".equals(data)) {
            return Optional.empty();
        }
        
        String[] values = data.split(",");
        if (values.length < 3) {
            throw new IllegalStateException("Invalid data found parsing ADXL326 Y axis, \"" + data + "\"");
        }
        String trimmed = values[1].trim();
        double value = Double.parseDouble(trimmed);
        
        // return calibrated value
        return Optional.of(this.calFunctionY.apply(value));
    }

    @Override
    public Optional<Double> getZAxisG(Sample toDecode) {
        String data = this.getData(toDecode);
        
        // handle missing samples
        if (data == null || "".equals(data)) {
            return Optional.empty();
        }
        
        String[] values = data.split(",");
        if (values.length < 3) {
            throw new IllegalStateException("Invalid data found parsing ADXL326 Z axis, \"" + data + "\"");
        }
        String trimmed = values[2].trim();
        trimmed = trimmed.substring(0, trimmed.length() - 1);
        double value = Double.parseDouble(trimmed);
        
        // return calibrated value
        return Optional.of(this.calFunctionZ.apply(value));
    }
}
