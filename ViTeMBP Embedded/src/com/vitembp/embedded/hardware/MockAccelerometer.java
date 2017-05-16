/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vitembp.embedded.hardware;

import java.util.Random;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A mock Sensor implementation which generates random vector data.
 */
public class MockAccelerometer extends Sensor {
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
    public MockAccelerometer(String name) {
        super(name);
    }
    
    @Override
    public UUID getType() {
        return MockAccelerometer.TYPE_UUID;
    }

    @Override
    public void initialize() {
        LOGGER.debug("Initialized sensor type: " + MockAccelerometer.TYPE_UUID.toString() + ".");
    }

    @Override
    public String readSample() {
        StringBuilder toReturn = new StringBuilder();
        toReturn.append("(");
        toReturn.append(this.randomGenerator.nextGaussian() * MockAccelerometer.MAX_SENSOR_VALUE);
        toReturn.append(",");
        toReturn.append(this.randomGenerator.nextGaussian() * MockAccelerometer.MAX_SENSOR_VALUE);
        toReturn.append(",");
        toReturn.append(this.randomGenerator.nextGaussian() * MockAccelerometer.MAX_SENSOR_VALUE);
        toReturn.append(")");
        
        return toReturn.toString();
    }
}
