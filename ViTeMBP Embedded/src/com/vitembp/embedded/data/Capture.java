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

import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
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
     * Gets an iteration of samples which represent the time ordered
     * data samples taken by the system.
     * @return The time ordered data samples taken by the system.
     */
    public abstract Iterable<Sample> getSamples();
    
    /**
     * Adds a new sample to the sample set.
     * @param data A map of sensors names to th data that was taken from them
     * for this sample.
     */
    public abstract void addSample(Map<String, String> data);
    
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
     */    
    public abstract void save();
    
    /**
     * Loads this capture session from persistent storage.
     */
    public abstract void load();
    
    /**
     * Returns a representation of this class as an XML fragment.
     * @return A representation of this class as an XML fragment.
     */
    public String toXml() {
        StringWriter sw = new StringWriter();
            
        try {
            XMLStreamWriter toWriteTo = XMLOutputFactory.newFactory().createXMLStreamWriter(sw);
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
        for (Sample sample : this.getSamples()) {
            sample.writeTo(toWriteTo);
        }
        toWriteTo.writeEndElement();
        
        toWriteTo.writeEndElement();
        toWriteTo.writeEndDocument();
    }
}
