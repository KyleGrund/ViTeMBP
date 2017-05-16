/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vitembp.embedded.hardware;

import java.util.UUID;

/**
 * This class provides a uniform interface for any sensor.
 */
public abstract class Sensor {
    /**
     * The name of this sensor instance.
     */
    private final String name;
    
    /**
     * Initializes a new instance of the Sensor class.
     * @param name The name of this sensor instance as used in the system.
     */
    protected Sensor(String name) {
        this.name = name;
    }
    
    /**
     * Gets the UUID which defines the type of sensor. This type can be used to
     * determine the format of the sample data that is created.
     * @return The UUID which defines the type of sensor.
     */
    public abstract UUID getType();
    
    /**
     * Gets the name of the sensor as used in the system. This defines the use
     * of the sensor, i.e. "Front Wheel Speed Sensor".
     * @return The name of the sensor as used in the system.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * The initialize function will be called when the system first starts.
     */
    public abstract void initialize();
    
    /**
     * This function will read a sample from the sensor and return a UTF-8
     * String represenation of the data.
     * @return A UTF-8 String representation of the data.
     */
    public abstract String readSample();
}
