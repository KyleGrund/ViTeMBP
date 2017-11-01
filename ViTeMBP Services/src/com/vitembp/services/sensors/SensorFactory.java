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

import com.vitembp.embedded.data.Capture;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A factory class which creates sensor objects.
 */
public class SensorFactory {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Gets a sensor for the name and type provided.
     * @param name The name of the sensor to build.
     * @param type The type of the sensor to build.
     * @param calData The sensor calibration data.
     * @return A sensor object for the provided name and type.
     * @throws InstantiationException If the sensor type is unknown.
     */
    public static Sensor getSensor(String name, UUID type, String calData) throws InstantiationException {
        // build sensor object based on type UUID
        if (RotaryEncoderEAW0J.TYPE_UUID.equals(type)) {
            return new RotaryEncoderEAW0J(name, calData);
        } else if (AccelerometerFXOS8700CQSerial.TYPE_UUID.equals(type)) {
            return new AccelerometerFXOS8700CQSerial(name, calData);
        } else if (DistanceVL53L0X.TYPE_UUID.equals(type)) {
            return new DistanceVL53L0X(name, calData);
        } else if (AccelerometerADXL326.TYPE_UUID.equals(type)){
            return new AccelerometerADXL326(name, calData);
        }
        
        // throw exception indicating no sensor type could be built
        throw new InstantiationException(
                "Could not create sensor named \"" +
                name +
                "\" with ID \"" +
                type.toString() +
                "\".");
    }
    
    /**
     * Builds sensor objects for all known sensors in the capture.
     * @param toBuildFor The capture to build sensors for.
     * @return A list containing sensor objects for all known sensors.
     */
    public static Map<String, Sensor> getSensors(Capture toBuildFor) {
        Map<String, Sensor> toReturn = new HashMap<>();
        
        toBuildFor.getSensorTypes().entrySet().stream()
                .map((entry) -> {
                    try {
                        return SensorFactory.getSensor(
                                entry.getKey(),
                                entry.getValue(),
                                toBuildFor.getSensorCalibrations().get(entry.getKey()));
                    } catch (InstantiationException ex) {
                        LOGGER.error("Unknown sensor: " + entry.getKey() + ", " + entry.getValue().toString() + ".", ex);
                        return null;
                    }
                })
                .forEach(s -> toReturn.put(s.getName(), s));
        
        return toReturn;
    }
}
