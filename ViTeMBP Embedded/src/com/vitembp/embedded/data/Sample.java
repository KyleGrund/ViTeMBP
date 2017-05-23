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

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
     * Gets a Map of sensor name strings to sensor type UUIDs.
     * @return A Map of sensor name strings to sensor type strings.
     */
    public abstract Map<String, UUID> getSensorTypes();
    
    /**
     * Gets a Map of sensor name strings to sensor data sample strings.
     * @return A Map of sensor name strings to sensor data sample strings.
     */
    public abstract Map<String, String> getSensorData();
}