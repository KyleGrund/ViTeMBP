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
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * An implementation of Sample using in-memory collection classes.
 */
public class InMemorySample extends Sample {
    /**
     * The index of this sample.
     */
    private final int index;
    
    /**
     * The time this sample was taken.
     */
    private final Instant time;
    
    /**
     * The names of the sensors whose data is represented by this sample.
     */
    private final Set<String> names;
    
    /**
     * The map of sensor names to their UUID type.
     */
    private final Map<String, UUID> types;
    
    /**
     * The data that was taken for this sample.
     */
    private final Map<String, String> data;

    /**
     * Initializes a new instance of the InMemorySample class.
     * @param index The index of this sample instance.
     * @param time The time this sampe was created.
     * @param data The data for this sample.
     */
    public InMemorySample(int index, Instant time, Map<Sensor, String> data) {
        // save index and time of this sample
        this.index = index;
        this.time = time;
        
        // create data structures to hold data
        this.names = new HashSet<>();
        this.types = new HashMap<>();
        this.data = new HashMap<>();
        
        // add the data to respective data structures
        data.forEach((Sensor s, String d) -> {
            this.names.add(s.getName());
            this.types.put(s.getName(), s.getType());
            this.data.put(s.getName(), d);
        });
    }
    
    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public Instant getTime() {
        return this.time;
    }

    @Override
    public Set<String> getSensorNames() {
        return Collections.unmodifiableSet(this.names);
    }

    @Override
    public Map<String, UUID> getSensorTypes() {
        return Collections.unmodifiableMap(this.types);
    }

    @Override
    public Map<String, String> getSensorData() {
        return Collections.unmodifiableMap(this.data);
    }
}
