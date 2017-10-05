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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
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
     * A UUID representing the serial number of this sensor.
     */
    private static final UUID SERIAL_UUID = UUID.fromString("62a37d20-6986-4273-92b0-3fbb4a0b7c77");
    
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
     */
    public AccelerometerMock() {
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

    @Override
    public UUID getSerial() {
        return SERIAL_UUID;
    }
    
    @Override
    public Calibrator getCalibrator() {
        List<String> userPrompts = Arrays.asList(new String[] {
            "To calibrate the accelerometer, slowly rotate the sensor in " +
                    "both a horizontal and a vertical circle so that all " + 
                    " edges face down one at a time."
        });
        
        // these will hold min minum and maximum values of the data readings
        final Map<String, Float> maximums = new HashMap<>();
        maximums.put("x", Float.MIN_VALUE);
        maximums.put("y", Float.MIN_VALUE);
        maximums.put("z", Float.MIN_VALUE);
        final Map<String, Float> minimums = new HashMap<>();
        minimums.put("x", Float.MAX_VALUE);
        minimums.put("y", Float.MAX_VALUE);
        minimums.put("z", Float.MAX_VALUE);
        
        // build up the data consumers
        List<Consumer<String>> sampleConsumers = new ArrayList<>();
        sampleConsumers.add(
            (String s) -> {
               // parse values
               String[] vals = s.replace("(", "").replace(")", "").split(",");
               float x = Float.parseFloat(vals[0]);
               float y = Float.parseFloat(vals[0]);
               float z = Float.parseFloat(vals[0]);
               
               // update maximum values
               maximums.put("x", Float.max(maximums.get("x"), x));
               maximums.put("y", Float.max(maximums.get("y"), y));
               maximums.put("z", Float.max(maximums.get("z"), z));
               
               // update minimum values
               minimums.put("x", Float.min(minimums.get("x"), x));
               minimums.put("y", Float.min(minimums.get("y"), y));
               minimums.put("z", Float.min(minimums.get("z"), z));
            });
        
        // formats and returns the calibration data
        Supplier<String> getDataCallback = () -> {
            return "[(" + Float.toString(minimums.get("x")) + "," + Float.toString(maximums.get("x")) + ")," +
                    "(" + Float.toString(minimums.get("y")) + "," + Float.toString(maximums.get("y")) + ")," +
                    "(" + Float.toString(minimums.get("z")) + "," + Float.toString(maximums.get("z")) + ")]";
        };
        
        // return the calibrator
        return new CalibratorReducer(
                this,
                29.97f,
                userPrompts,
                sampleConsumers,
                getDataCallback
        );
    }
}
