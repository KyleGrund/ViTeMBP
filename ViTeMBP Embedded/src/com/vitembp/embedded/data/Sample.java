/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vitembp.embedded.data;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * This class provides a model for a single data sample. This class should be
 * implemented for each type of underlying data store.
 */
public abstract class Sample {
    /**
     * Gets the index of the sample which is the number of samples after the
     * first sample which has an index of 0.
     * @return The index of the sample.
     */
    public abstract int getIndex();
    
    /**
     * Gets the time that the samples were taken.
     * @return The time that the samples were taken.
     */
    public abstract Instant getTime();
    
    /**
     * Returns a set of Strings representing the names of the sensors in this
     * sample.
     * @return A set of Strings representing the names of the sensors in this
     * sample.
     */
    public abstract Set<String> getSensorNames();
    
    /**
     * Gets a Map of sensor name strings to sensor type strings.
     * @return A Map of sensor name strings to sensor type strings.
     */
    public abstract Map<String, String> getSensorTypes();
    
    /**
     * Gets a Map of sensor name strings to sensor data sample strings.
     * @return A Map of sensor name strings to sensor data sample strings.
     */
    public abstract Map<String, String> getSensorData();
}
