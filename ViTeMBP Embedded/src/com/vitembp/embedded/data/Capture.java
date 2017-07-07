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
import java.io.StringWriter;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.apache.logging.log4j.LogManager;

/**
 * This class provides a model for a data capture session. This class should be
 * implemented for each type of underlying data store.
 */
public abstract class Capture {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * The time of the first sample.
     */
    protected Instant startTime = null;
    
    /**
     * The frequency to take samples in hertz.
     */
    protected double sampleFrequency;
    
    /**
     * The interval between successive readings in nanoseconds.
     */
    protected long nanoSecondInterval;
    
    /**
     * Initializes a new instance of the Capture class.
     * @param sampleFrequency The frequency at which samples were taken.
     */
    Capture(double sampleFrequency) {
        this.sampleFrequency = sampleFrequency;
        
        // calculate the update interval from the sample frequency
        this.calculateIntervalFromFrequency();
    }
    
    /**
     * Gets an iteration of samples which represent the time ordered
     * data samples taken by the system.
     * @return The time ordered data samples taken by the system.
     */
    public abstract Iterable<Sample> getSamples();
    
    /**
     * Adds a new sample to the sample set.
     * @param data A map of sensors names to the data that was taken from them
     * for this sample.
     */
    public abstract void addSample(Map<String, String> data);
    
    /**
     * Gets the number of samples in the capture.
     * @return The number of samples in the capture.
     */
    protected abstract int getSampleCount();
    
    /**
     * Adds a new sample to the sample set.
     * @param toAdd A sample to add.
     */
    protected abstract void addSample(Sample toAdd);
    
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
     * Saves this capture session to persistent storage.
     * @throws java.io.IOException If an IO exception occurs while loading data.
     */    
    public abstract void save() throws IOException;
    
    /**
     * Loads this capture session from persistent storage.
     * @throws java.io.IOException If an IO exception occurs while loading data.
     */
    public abstract void load() throws IOException;
    
    /**
     * Reads in samples from an XMLStreamReader.
     * @param toReadFrom The XMLStreamReader to load samples from.
     * @throws XMLStreamException If there is an error reading data from XML.
     */
    protected abstract void readSamplesFrom(XMLStreamReader toReadFrom) throws XMLStreamException;
    
    /**
     * Writes samples to an XMLStreamReader.
     * @param toWriteTo The XMLStreamWriter to write samples to.
     * @throws XMLStreamException If there is an error reading data from XML.
     */
    protected abstract void writeSamplesTo(XMLStreamWriter toWriteTo) throws XMLStreamException;
    
    /**
     * Gets the sampling frequency of this capture.
     * @return The sampling frequency of this capture.
     */
    public double getSampleFrequency() {
        return this.sampleFrequency;
    }
    
    /**
     * Gets the Instant representing the time this capture was started.
     * @return An Instant representing the time this capture was started.
     */
    public Instant getStartTime() {
        // return the epoch if there is no start time
        if (this.startTime == null) {
            return Instant.EPOCH;
        }
        return this.startTime;
    }
    
    /**
     * Returns a representation of this class as an XML fragment.
     * @return A representation of this class as an XML fragment.
     */
    public String toXml() {
        StringWriter sw = new StringWriter();
            
        try {
            XMLStreamWriter toWriteTo = XMLStreams.createWriter(sw);
            this.writeTo(toWriteTo);
        } catch (XMLStreamException ex) {
            LOGGER.error("XMLStreamException ocurred while writing capture to stream.", ex);
        }
            
        return sw.toString();
    }
    
    /**
     * Writes this sample to an XMLStreamWriter.
     * @param toWriteTo The XMLStreamWriter to write to.
     * @throws XMLStreamException If an exception occurs writing to the stream.
     */
    public void writeTo(XMLStreamWriter toWriteTo) throws XMLStreamException {
        toWriteTo.writeStartDocument();
        
        Map<String, UUID> sensors = this.getSensorTypes();
        
        toWriteTo.writeStartElement("capture");
        toWriteTo.writeStartElement("starttime");
        toWriteTo.writeCharacters(this.getStartTime().toString());
        toWriteTo.writeEndElement();
        toWriteTo.writeStartElement("samplefrequency");
        toWriteTo.writeCharacters(Double.toString(this.sampleFrequency));
        toWriteTo.writeEndElement();
        toWriteTo.writeStartElement("sensors");
        for (String name : sensors.keySet()) {
            toWriteTo.writeStartElement("sensor");
            toWriteTo.writeStartElement("name");
            toWriteTo.writeCharacters(name);
            toWriteTo.writeEndElement();
            toWriteTo.writeStartElement("type");
            toWriteTo.writeCharacters(sensors.get(name).toString());
            toWriteTo.writeEndElement();
            toWriteTo.writeEndElement();
        }
        toWriteTo.writeEndElement();
        
        toWriteTo.writeStartElement("samples");
        this.writeSamplesTo(toWriteTo);
        toWriteTo.writeEndElement();
        
        toWriteTo.writeEndElement();
        toWriteTo.writeEndDocument();
    }
    
    /**
     * Read data for this Capture from an XMLStreamWriter.
     * @param toReadFrom The XMLStreamReader to read from.
     * @throws XMLStreamException If an exception occurs writing to the stream.
     */
    protected void readFrom(XMLStreamReader toReadFrom) throws XMLStreamException {
        if (toReadFrom.getEventType() != XMLStreamConstants.START_DOCUMENT) {
            throw new XMLStreamException("Expected start of document not found.", toReadFrom.getLocation());
        }
        
        // read into capture element
        if (toReadFrom.next() != XMLStreamConstants.START_ELEMENT || !"capture".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <capture> not found.", toReadFrom.getLocation());
        }
        
        // read into start time element
        if (toReadFrom.next() != XMLStreamConstants.START_ELEMENT || !"starttime".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <starttime> not found.", toReadFrom.getLocation());
        }
        
        // read and parse start time
        if (toReadFrom.next() != XMLStreamConstants.CHARACTERS) {
            throw new XMLStreamException("Expected start time string not found.", toReadFrom.getLocation());
        }
        
        try {
            this.startTime = Instant.parse(toReadFrom.getText());
        } catch (DateTimeParseException ex) {
            LOGGER.error("Error parsing start time when loading Capture from XML.", ex);
            throw new XMLStreamException("Error parsing start time when loading Capture from XML.", toReadFrom.getLocation(), ex);
        }

        // read into close start time element
        if (toReadFrom.next() != XMLStreamConstants.END_ELEMENT || !"starttime".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </starttime> not found.", toReadFrom.getLocation());
        }
        
        // read into sample frequency element
        if (toReadFrom.next() != XMLStreamConstants.START_ELEMENT || !"samplefrequency".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <samplefrequency> not found.", toReadFrom.getLocation());
        }
        
        // read and parse sample frequency
        if (toReadFrom.next() != XMLStreamConstants.CHARACTERS) {
            throw new XMLStreamException("Expected sample frequency string not found.", toReadFrom.getLocation());
        }
        
        try {
            this.sampleFrequency = Double.valueOf(toReadFrom.getText());
        } catch (NumberFormatException ex) {
            LOGGER.error("Error parsing sample frequency when loading Capture from XML.", ex);
            throw new XMLStreamException("Error parsing sample frequency when loading Capture from XML.", toReadFrom.getLocation(), ex);
        }
        
        // the sample frequency was updated so recalculate the update interval
        this.calculateIntervalFromFrequency();
        
        // read into close sample interval element
        if (toReadFrom.next() != XMLStreamConstants.END_ELEMENT || !"samplefrequency".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </samplefrequency> not found.", toReadFrom.getLocation());
        }
        
        // read into sensors element
        if (toReadFrom.next() != XMLStreamConstants.START_ELEMENT || !"sensors".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <sensors> not found.", toReadFrom.getLocation());
        }
        toReadFrom.next();
        
        // map of sensor name to type
        Map<String, String> sensorTypes = new HashMap<>();
        
        // add a sensor element for each data entry
        while ("sensor".equals(toReadFrom.getLocalName())) {
            // read into name element
            if (toReadFrom.next() != XMLStreamConstants.START_ELEMENT || !"name".equals(toReadFrom.getLocalName())) {
                throw new XMLStreamException("Expected <name> not found.", toReadFrom.getLocation());
            }
            
            if (toReadFrom.next() != XMLStreamConstants.CHARACTERS) {
                throw new XMLStreamException("Expected sensor name string not found.", toReadFrom.getLocation());
            }
            
            String sensorName = toReadFrom.getText();
            
            // read into close name element
            if (toReadFrom.next() != XMLStreamConstants.END_ELEMENT || !"name".equals(toReadFrom.getLocalName())) {
                throw new XMLStreamException("Expected </name> not found.", toReadFrom.getLocation());
            }
            
            // read into type element
            if (toReadFrom.next() != XMLStreamConstants.START_ELEMENT || !"type".equals(toReadFrom.getLocalName())) {
                throw new XMLStreamException("Expected <type> not found.", toReadFrom.getLocation());
            }
            
            if (toReadFrom.next() != XMLStreamConstants.CHARACTERS) {
                throw new XMLStreamException("Expected sensor type string not found.", toReadFrom.getLocation());
            }
            
            String sensorType = toReadFrom.getText();
            
            // read into close type element
            if (toReadFrom.next() != XMLStreamConstants.END_ELEMENT || !"type".equals(toReadFrom.getLocalName())) {
                throw new XMLStreamException("Expected </type> not found.", toReadFrom.getLocation());
            }
        
            // read into close element
            if (toReadFrom.next() != XMLStreamConstants.END_ELEMENT || !"sensor".equals(toReadFrom.getLocalName())) {
                throw new XMLStreamException("Expected </sensor> not found.", toReadFrom.getLocation());
            }
            
            // successfully found a sensor, save it
            sensorTypes.put(sensorName, sensorType);
            
            // read past close element
            toReadFrom.next();
        }
        
        // read into close element
        if (toReadFrom.getEventType() != XMLStreamConstants.END_ELEMENT || !"sensors".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </sensors> not found.", toReadFrom.getLocation());
        }
        
        // read into samples element
        if (toReadFrom.next() != XMLStreamConstants.START_ELEMENT || !"samples".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <samples> not found.", toReadFrom.getLocation());
        }
        
        // read samples by using subclass implementation as the base class
        // doesn't know how to handle samples
        this.readSamplesFrom(toReadFrom);
        
        // read into close samples element
        if (toReadFrom.getEventType() != XMLStreamConstants.END_ELEMENT || !"samples".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </samples> not found.", toReadFrom.getLocation());
        }
        
        // read into close capture
        if (toReadFrom.next() != XMLStreamConstants.END_ELEMENT || !"capture".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </capture> not found.", toReadFrom.getLocation());
        }
        
        // read to end of document
        if (toReadFrom.next() != XMLStreamConstants.END_DOCUMENT) {
            throw new XMLStreamException("Expected end of document not found.", toReadFrom.getLocation());
        }
    }
    
    /**
     * Calculates the sampling interval from the frequency.
     */
    private void calculateIntervalFromFrequency() {
        // calculate the nanoseconds between successive samples by 1/f * 10^9.
        this.nanoSecondInterval = Math.round((1.0d / this.sampleFrequency) * Math.pow(10.0d, 9.0d));
    }
}
