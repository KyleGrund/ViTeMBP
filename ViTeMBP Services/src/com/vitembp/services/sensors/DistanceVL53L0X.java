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
     * Initializes a new instance of the DistanceVL53L0X class.
     * @param name The name of the sensor.
     */
    DistanceVL53L0X(String name) {
        super(name);
    }

    @Override
    public Optional<Double> getDistanceMilimeters(Sample toDecode) {
        String data = this.getData(toDecode);
        if (data == null) {
            return Optional.empty();
        } else {
            return Optional.of(Double.parseDouble(data));
        }
    }    
}
