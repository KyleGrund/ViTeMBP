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
 * Sensor implementation for EAW0J rotary encoder.
 */
class RotaryEncoderEAW0J extends RotarySensor {
    /**
     * The maximum value the encoder will output.
     */
    private static final double MAXIMUM_ENCODER_VALUE = 127.0d;
    
    /**
     * A UUID representing the type of this sensor.
     */
    static final UUID TYPE_UUID = UUID.fromString("75d05ba8-639c-46e6-a940-591d920a2d86");
    
    /**
     * The function which applies the calibration data.
     */
    private Function<Double, Double> calFunction;
    
    /**
     * Instantiates a new instance of the RotaryEncoderEAW0J class.
     * @param name The name of the sensor.
     * @param calData The sensor calibration data.
     */
    public RotaryEncoderEAW0J(String name, String calData) {
        super(name);
        
        // decode cal data of the form "([min],[max])"
        if (calData != null && !calData.isEmpty()) {
            if (!calData.startsWith("(") || !calData.endsWith(")")) {
                throw new IllegalArgumentException("EAW0J calibration data must be of the form \"([near],[middle],[far])\".");
            }
            
            String[] split = calData.substring(1, calData.length() - 1).split(",");
            if (split.length != 3) {
                throw new IllegalArgumentException("EAW0J calibration data must be of the form \"([near],[middle],[far])\".");
            }
            
            int calNear, calMiddle, calFar;
            
            try {
                calNear = Integer.parseInt(split[0]);
                calMiddle = Integer.parseInt(split[1]);
                calFar = Integer.parseInt(split[2]);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("EAW0J calibration data must be of the form \"([near],[middle],[far])\".", ex);
            }
            
            // sensor values are from 0 to 127 use this and three cal points to determine the direction of rotation
            if (calNear < calMiddle && calMiddle < calFar) {
                // N < M < F
                this.calFunction = (point) -> 1.0d - ((point - calNear) / (calFar - calNear));
            } else if ((calFar < calNear && calNear < calMiddle) ||
                    (calMiddle < calFar && calFar < calNear)) {
                // F < N < M
                // M < F < N
                this.calFunction = (point) -> {
                    double val = point - calNear;
                    if (val < 0) {
                        val += 128;
                    }
                    return 1.0d - (val / ((128 - calNear) + calFar));
                };
            } else if (calFar < calMiddle && calMiddle < calNear) {
                // F < M < N
                this.calFunction = (point) -> (point - calFar) / (calNear - calFar);
            } else if ((calNear < calFar && calFar < calMiddle) ||
                    (calMiddle < calNear && calNear < calFar)) {
                // N < F < M
                // M < N < F
                this.calFunction = (point) -> {
                    double val = point;
                    if (val < calFar) {
                        val += 128;
                    }
                    val -= calFar;
                    val /= ((128 - calFar) + calNear);
                    
                    return val;
                };
            } else {
                throw new IllegalArgumentException("EAW0J calibration data is invalid, it must contain 3 unique values.");
            }
            
            // add bounds checking to keep cal funtion between 0 and 1.
            this.calFunction = 
                    this.calFunction
                    .andThen((Function<Double, Double>)(v -> (v < 0) ? 0 : (v > 1) ? 1 : v));
        } else {
            // no cal data so just return original value
            this.calFunction = d -> d;
        }
    }
    
    @Override
    public Optional<Double> getPositionDegrees(Sample toDecode) {
        Optional<Double> value = getPositionPercentage(toDecode);
        if (value.isPresent()) {
            return Optional.of(value.get() * 365.0d);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Double> getPositionRadians(Sample toDecode) {
        Optional<Double> value = getPositionPercentage(toDecode);
        if (value.isPresent()) {
            return Optional.of(value.get() * Math.PI * 2.0d);
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<Double> getPositionPercentage(Sample toDecode) {
        String data = this.getData(toDecode);
        
        // handle missing samples
        if (data == null || "".equals(data)) {
            return Optional.empty();
        }
        
        // get data
        int position = Integer.parseInt(data);
        
        // apply calibration
        double calibrated = this.calFunction.apply((double)position);
        
        
        // convert to percent
        return Optional.of(calibrated);
    }
}
