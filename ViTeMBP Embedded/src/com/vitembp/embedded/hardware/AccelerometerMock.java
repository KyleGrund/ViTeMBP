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
package com.vitembp.embedded.hardware;

import java.util.Random;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A mock Sensor implementation which generates random vector data.
 */
public class AccelerometerMock extends Sensor {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * A UUID representing the type of this sensor.
     */
    private static final UUID TYPE_UUID = UUID.fromString("3906c164-82c8-48f8-a154-a39a9d0269fa");
    
    /**
     * The maximum value to return.
     */
    private static final double MAX_SENSOR_VALUE = 5.0d;
    
    /**
     * A psuedo-random generator for creating simulated sensor readings.
     */
    private final Random randomGenerator = new Random();
    
    /**
     * Initializes a new instance of the MockAccelerometer class.
     * @param name The name of the sensor as used in the system.
     */
    public AccelerometerMock(String name) {
        super(name);
    }
    
    @Override
    public UUID getType() {
        return AccelerometerMock.TYPE_UUID;
    }

    @Override
    public void initialize() {
        LOGGER.debug("Initialized sensor type: " + AccelerometerMock.TYPE_UUID.toString() + ".");
    }

    @Override
    public String readSample() {
        StringBuilder toReturn = new StringBuilder();
        toReturn.append("(");
        toReturn.append(this.randomGenerator.nextGaussian() * AccelerometerMock.MAX_SENSOR_VALUE);
        toReturn.append(",");
        toReturn.append(this.randomGenerator.nextGaussian() * AccelerometerMock.MAX_SENSOR_VALUE);
        toReturn.append(",");
        toReturn.append(this.randomGenerator.nextGaussian() * AccelerometerMock.MAX_SENSOR_VALUE);
        toReturn.append(")");
        
        return toReturn.toString();
    }
}
