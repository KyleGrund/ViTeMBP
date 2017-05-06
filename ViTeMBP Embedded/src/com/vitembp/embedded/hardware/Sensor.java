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
     * Gets the UUID which defines the type of sensor.
     * @return The UUID which defines the type of sensor.
     */
    public abstract UUID getType();
    
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
