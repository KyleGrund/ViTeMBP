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

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.apache.logging.log4j.LogManager;

/**
 * An implementation of Capture using in-memory collection classes.
 */
class UuidStringStorePagingCapture extends Capture {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * The names of the sensors whose data is represented by this sample.
     */
    private final Set<String> names;
    
    /**
     * The map of sensor names to their UUID type.
     */
    private final Map<String, UUID> types;
    
    /**
     * The persistent storage this instance uses.
     */
    private final UuidStringLocation store;
    
    /**
     * The size of a sample page.
     */
    private final int pageSize;
    
    /**
     * Manager which provides paging ability
     */
    private SamplePageManager manager;
    
    /**
     * The number of samples in the capture.
     */
    private int sampleCount = 0;
    
    /**
     * Initializes a new instance of the InMemoryCapture class and stores it to
     * the persistent storage.
     * @param frequency The frequency at which samples were taken.
     * @param store The persistent storage this instance uses.
     * @param nameToIds A map of sensor names to type UUIDs.
     */
    public UuidStringStorePagingCapture(double frequency, UuidStringLocation store, int pageSize, Map<String, UUID> nameToIds) {
        super(frequency);
        
        // save refrences to parameters        
        this.store = store;
        this.names = new HashSet<>(nameToIds.keySet());
        this.types = new HashMap<>(nameToIds);
        this.pageSize = pageSize;
    }
    
    @Override
    public Iterable<Sample> getSamples() {
        // this is the hard part.
        return this.manager.getSamples();
    }

    @Override
    public void addSample(Map<String, String> data) {
        // if this is the first sample, set start time
        checkStartTime();
        
        // calculate the sample using the calculated interval
        Instant sampleTime = this.startTime.plusNanos(this.nanoSecondInterval * this.sampleCount);
        
        // create a new sample and add it to the samples array list
        this.manager.addSample(new Sample(sampleCount, sampleTime, data));
        sampleCount++;
    }

    /**
     * If there are no samples, set the current time as the capture start time.
     */
    private void checkStartTime() {
        // if this is the first sample, set the start time
        if (this.sampleCount == 0) {
            this.startTime = Instant.now();
            
            // build a page manager, now that we have the start time all
            // dependencies have been bound
            this.manager = new SamplePageManager(store, pageSize, this.startTime, this.nanoSecondInterval);
        }
    }
    
    @Override
    public void save() throws IOException {
        this.manager.save();
    }

    @Override
    public void load() throws IOException {
        this.manager.load();
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
    protected void addSample(Sample toAdd) {
        // if this is the first sample, set start time
        checkStartTime();
        
        this.manager.addSample(toAdd);
        this.sampleCount++;
    }

    @Override
    protected int getSampleCount() {
        return this.manager.getSampleCount();
    }
    
    @Override
    protected void writeSamplesTo(XMLStreamWriter toWriteTo) throws XMLStreamException {
        try {
            this.save();
        } catch (IOException ex) {
            throw new XMLStreamException("IOException occurred while saving UuidStringStorePagingCapture.", ex);
        }
    }
    
    @Override
    protected void readSamplesFrom(XMLStreamReader toReadFrom) throws XMLStreamException {
        try {
            this.load();
        } catch (IOException ex) {
            throw new XMLStreamException("IOException occurred while loading UuidStringStorePagingCapture.", ex);
        }
    }
}
