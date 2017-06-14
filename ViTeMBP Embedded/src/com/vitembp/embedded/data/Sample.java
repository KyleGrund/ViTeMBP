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
import java.io.StringWriter;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.apache.logging.log4j.LogManager;

/**
 * This class provides a model for a single data sample. This class should be
 * implemented for each type of underlying data store.
 */
public class Sample {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * The index of this sample.
     */
    private final int index;
    
    /**
     * The time this sample was taken.
     */
    private final Instant time;
    
    /**
     * The data that was taken for this sample.
     */
    private final Map<String, String> data;
    
    /**
     * Initializes a new instance of the InMemorySample class.
     * @param index The index of this sample instance.
     * @param time The time this sample was created.
     * @param data The data for this sample.
     */
    public Sample(int index, Instant time, Map<String, String> data) {
        // save index and time of this sample
        this.index = index;
        this.time = time;
        
        // create read only data structure to hold data
        this.data = Collections.unmodifiableMap(data);
    }
    
    /**
     * Gets the index of the sample which is the number of samples after the
     * first sample which has an index of 0.
     * @return The index of the sample.
     */
    public int getIndex() {
        return this.index;
    }
    
    /**
     * Gets the time that the samples were taken.
     * @return The time that the samples were taken.
     */
    public Instant getTime() {
        return this.time;
    }
    
    
    /**
     * Gets a Map of sensor name strings to sensor data sample strings.
     * @return A Map of sensor name strings to sensor data sample strings.
     */
    public Map<String, String> getSensorData() {
        return this.data;
    }
    
    /**
     * Returns a representation of this class as an XML fragment.
     * @return A representation of this class as an XML fragment.
     */
    protected String toXmlFragment() {
        StringWriter sw = new StringWriter();
            
        try {
            XMLStreamWriter toWriteTo = XMLOutputFactory.newFactory().createXMLStreamWriter(sw);
            this.writeTo(toWriteTo);
        } catch (XMLStreamException ex) {
            LOGGER.error("XMLStreamException ocurred while writing sample to stream.", ex);
        }
            
        return sw.toString();
    }
    
    /**
     * Writes this sample to an XMLStreamWriter.
     * @param toWriteTo The XMLStreamWriter to write to.
     * @throws XMLStreamException If an exception occurs writing to the stream.
     */
    protected void writeTo(XMLStreamWriter toWriteTo) throws XMLStreamException {
        Map<String, String> data = this.getSensorData();
        
        // add a sample element
        toWriteTo.writeStartElement("sample");
        
        // add a sensor element for each data entry
        for (String name : data.keySet()) {
            toWriteTo.writeStartElement("sensor");
            toWriteTo.writeAttribute("name", name);
            toWriteTo.writeCharacters(data.get(name));
            toWriteTo.writeEndElement();
        }
        
        toWriteTo.writeEndElement();
    }
    
    /**
     * Read this sample from an XMLStreamWriter.
     * @param toReadFrom The XMLStreamReader to read from.
     * @return Map containing loaded data.
     * @throws XMLStreamException If an exception occurs writing to the stream.
     */
    protected Map<String, String> readFrom(XMLStreamReader toReadFrom) throws XMLStreamException {
        Map<String, String> data = new HashMap<>();
        
        // read sample element
        if (!"sample".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <sample> not found.", toReadFrom.getLocation());
        }
        toReadFrom.next();
        
        // add a sensor element for each data entry
        while ("sensor".equals(toReadFrom.getLocalName())) {
            // a sensor element must have a name attribute
            if (toReadFrom.getAttributeCount() != 1 || "name".equals(toReadFrom.getAttributeName(0))) {
                throw new XMLStreamException("Expected name attribute not found.", toReadFrom.getLocation());
            }
            String name = toReadFrom.getAttributeValue(0);
            
            if (toReadFrom.next() != XMLStreamConstants.CHARACTERS) {
                throw new XMLStreamException("Expected sensor data  string not found.", toReadFrom.getLocation());
            }
            
            // store the sensor data
            data.put(name, toReadFrom.getText());
            
            if (toReadFrom.next() != XMLStreamConstants.END_ELEMENT || !"sensor".equals(toReadFrom.getLocalName())) {
                throw new XMLStreamException("Expected </sensor> not found.", toReadFrom.getLocation());
            }
            toReadFrom.next();
        }
        
        if (toReadFrom.getEventType() != XMLStreamConstants.END_ELEMENT || !"sample".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </sample> not found.", toReadFrom.getLocation());
        }
        
        // read past close element
        toReadFrom.next();
        
        return data;
    }
}