/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vitembp.embedded.data;

import java.time.Instant;
import java.util.Set;

/**
 * This class provides a model for a data capture session. This class should be
 * implemented for each type of underlying data store.
 */
public abstract class Capture {
    /**
     * Gets an interation of sample sets which represent the time ordered
     * data samples taken by the system.
     * @return The time ordered data samples taken by the system.
     */
    public abstract Iterable<Set<Sample>> getSamples();
    
    /**
     * Gets the time which the capture was started.
     * @return The time which the capture was started.
     */
    public abstract Instant getStartTime();
}
