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
package com.vitembp.sensors;

import java.util.UUID;

/**
 * A factory class which creates sensor objects.
 */
public class SensorFactory {
    public static Sensor getSensor(String name, UUID type) throws InstantiationException {
        // build sensor object based on type UUID
        if (RotaryEncoderEAW0J.TYPE_UUID.equals(type)) {
            return new RotaryEncoderEAW0J(name);
        } else if (AccelerometerFXOS8700CQSerial.TYPE_UUID.equals(type)) {
            return new AccelerometerFXOS8700CQSerial(name);
        } else if (DistanceVL53L0X.TYPE_UUID.equals(type)) {
            return new DistanceVL53L0X(name);
        }
        
        // throw exception indicating no sensor type could be built
        throw new InstantiationException(
                "Could not create sensor named \"" +
                name +
                "\" with ID \"" +
                type.toString() +
                "\".");
    }
}
