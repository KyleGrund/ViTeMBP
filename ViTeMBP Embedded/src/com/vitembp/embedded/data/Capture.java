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
package com.vitembp.embedded.data;

import com.vitembp.embedded.hardware.Sensor;
import java.util.Map;

/**
 * This class provides a model for a data capture session. This class should be
 * implemented for each type of underlying data store.
 */
public abstract class Capture {
    /**
     * Gets an interation of samples which represent the time ordered
     * data samples taken by the system.
     * @return The time ordered data samples taken by the system.
     */
    public abstract Iterable<Sample> getSamples();
    
    /**
     * Adds a new sample to the sample set.
     * @param data A map of sensors to th data that was taken from them for this
     * sample.
     */
    public abstract void addSample(Map<Sensor, String> data);
    
    /**
     * Saves this capture session to persistant storage.
     */    
    public abstract void save();
    
    /**
     * Loads this capture session from presistant storage.
     */
    public abstract void load();
}
