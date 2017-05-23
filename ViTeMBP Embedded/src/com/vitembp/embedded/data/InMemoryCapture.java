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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * An implementation of Capture using in-memory collection classes.
 */
public class InMemoryCapture extends Capture {
    /**
     * The set of samples.
     */
    private final ArrayList<Sample> samples;
    
    /**
     * Initializes a new instance of the InMemoryCapture class.
     */
    public InMemoryCapture() {
        this.samples = new ArrayList<>();
    }
    
    @Override
    public Iterable<Sample> getSamples() {
        return Collections.unmodifiableList(this.samples);
    }

    @Override
    public void addSample(Map<Sensor, String> data) {
        // create a new sample and add it to the samples array list
        this.samples.add(new InMemorySample(this.samples.size(), Instant.now(), data));
    }
    
    @Override
    public void save() {
        // nothing to do to save an in-memory capture
    }

    @Override
    public void load() {
        // nothing to do to load an in-memory capture
    }
}
