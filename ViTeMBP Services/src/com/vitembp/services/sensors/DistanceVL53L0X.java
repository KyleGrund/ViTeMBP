package com.vitembp.services.sensors;

import com.vitembp.embedded.data.Sample;
import java.util.Optional;
import java.util.UUID;

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

/**
 * Class providing an interface to the VL53L0X sensor.
 */
class DistanceVL53L0X extends DistanceSensor {
    /**
     * A UUID representing the type of this sensor.
     */
    static final UUID TYPE_UUID = UUID.fromString("3972d3a9-d55f-4e74-a61f-f2f8fe62f858");
    
    /**
     * The calibrated minimum value.
     */
    private double calMinimum = Double.MIN_VALUE;
    
    /**
     * The calibrated maximum value.
     */
    private double calMaximum = Double.MAX_VALUE;
    
    /**
     * Initializes a new instance of the DistanceVL53L0X class.
     * @param name The name of the sensor.
     * @param calData The sensor calibration data.
     */
    DistanceVL53L0X(String name, String calData) {
        super(name);
        
        // decode cal data of the form "([min],[max])"
        if (calData != null && !calData.isEmpty()) {
            String[] split = calData.split(",");
            if (split.length != 2) {
                throw new IllegalArgumentException("VL53L0X calibration data must be of the form \"([min],[max])\".");
            }
            
            try {
                this.calMinimum = Double.parseDouble(split[0].substring(1));
                this.calMaximum = Double.parseDouble(split[1].substring(0, split[1].length() - 2));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("VL53L0X calibration data must be of the form \"([min],[max])\".", ex);
            }
        }
    }

    @Override
    public Optional<Double> getDistanceMilimeters(Sample toDecode) {
        String data = this.getData(toDecode);
        if (data == null) {
            return Optional.empty();
        } else {
            // get the sensor reading value
            double value = Double.parseDouble(data);
            
            // apply calibration
            value = Math.min(value, this.calMaximum);
            value = Math.max(value, this.calMinimum);
            value -= this.calMinimum;
            
            // return calibrated value
            return Optional.of(value);
        }
    }
    
    @Override
    public Optional<Double> getDistancePercent(Sample toDecode) {
        Optional<Double> value = this.getDistanceMilimeters(toDecode);
        if (value.isPresent()) {
            return Optional.of(value.get() / (this.calMaximum - this.calMinimum));
        } else {
            return Optional.empty();
        }
    }
}
