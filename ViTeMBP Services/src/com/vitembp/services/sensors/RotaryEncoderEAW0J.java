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
     * The sensor calibration near point position.
     */
    private double calNear;
    
    /**
     * The sensor calibration middle point position.
     */
    private double calMiddle;
    
    /**
     * The sensor calibration far point position.
     */
    private double calFar;
    
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
            
            String[] split = calData.substring(1, calData.length() - 2).split(",");
            if (split.length != 3) {
                throw new IllegalArgumentException("EAW0J calibration data must be of the form \"([near],[middle],[far])\".");
            }
            
            try {
                this.calNear = Double.parseDouble(split[0].substring(1));
                this.calMiddle = Double.parseDouble(split[1].substring(0, split[1].length() - 2));
                this.calFar = Double.parseDouble(split[1].substring(0, split[1].length() - 2));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("VL53L0X calibration data must be of the form \"([min],[max])\".", ex);
            }
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
    
    /**
     * Gets the position of the encoder as a value from 0 to 1.
     * @param toDecode The sample to decode.
     * @return The position of the encoder as a value from 0 to 1.
     * @throws NumberFormatException If the sensor data is corrupt.
     */
    private Optional<Double> getPositionPercentage(Sample toDecode) throws NumberFormatException {
        String data = this.getData(toDecode);
        
        // handle missing samples
        if (data == null) {
            return Optional.empty();
        }
        
        int position = Integer.parseInt(data);
        double percent = ((double)position) / MAXIMUM_ENCODER_VALUE;
        return Optional.of(percent);
    }
}
