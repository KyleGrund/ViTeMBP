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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * An implementation of Capture using in-memory collection classes.
 */
class InMemoryCapture extends Capture {
    /**
     * The names of the sensors whose data is represented by this sample.
     */
    private final Set<String> names;
    
    /**
     * The map of sensor names to their UUID type.
     */
    private final Map<String, UUID> types;
    
    /**
     * The set of samples.
     */
    private final ArrayList<Sample> samples;
    
    /**
     * Initializes a new instance of the InMemoryCapture class.
     * @param frequency The frequency of the data samples.
     * @param nameToIds A map of sensor names to type UUIDs.
     */
    public InMemoryCapture(double frequency, Map<String, UUID> nameToIds) {
        super(frequency);
        
        this.samples = new ArrayList<>();
        
        this.names = new HashSet<>(nameToIds.keySet());
        this.types = new HashMap<>(nameToIds);
    }
    
    @Override
    public Iterable<Sample> getSamples() {
        return Collections.unmodifiableList(this.samples);
    }
    
    @Override
    protected void addSample(Sample toAdd) {        
        // create a new sample and add it to the samples array list
        this.samples.add(toAdd);
    }
    
    @Override
    public void save() {
        // nothing to do to save an in-memory capture
    }

    @Override
    public void load() {
        // nothing to do to load an in-memory capture
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
    protected void readSamplesFrom(XMLStreamReader toReadFrom) throws XMLStreamException {
        while ("sample".equals(toReadFrom.getLocalName())) {
            int index = this.samples.size();
            Instant time = this.startTime.plusNanos(this.nanoSecondInterval * index);
            this.samples.add(new Sample(this.samples.size(), time, toReadFrom));
        }
    }

    @Override
    protected int getSampleCount() {
        return this.samples.size();
    }
}
