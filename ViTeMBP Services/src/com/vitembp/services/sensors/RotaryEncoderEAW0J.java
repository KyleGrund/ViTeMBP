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
     * Instantiates a new instance of the RotaryEncoderEAW0J class.
     * @param name The name of the sensor.
     */
    public RotaryEncoderEAW0J(String name) {
        super(name);
    }
    
    @Override
    public double getPositionDegrees(Sample toDecode) {
        return getPositionPercentage(toDecode) * 365.0d;
    }

    @Override
    public double getPositionRadians(Sample toDecode) {
        return getPositionPercentage(toDecode) * Math.PI * 2.0d;
    }
    
    /**
     * Gets the position of the encoder as a value from 0 to 1.
     * @param toDecode The sample to decode.
     * @return The position of the encoder as a value from 0 to 1.
     * @throws NumberFormatException If the sensor data is corrupt.
     */
    private double getPositionPercentage(Sample toDecode) throws NumberFormatException {
        String data = this.getData(toDecode);
        int position = Integer.parseInt(data);
        double percent = ((double)position) / MAXIMUM_ENCODER_VALUE;
        return percent;
    }
}
