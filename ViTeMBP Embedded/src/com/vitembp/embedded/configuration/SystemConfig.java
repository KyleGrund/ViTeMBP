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
package com.vitembp.embedded.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides a set of system configuration data to use during system operation.
 */
public class SystemConfig {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * The singleton instance for this class.
     */
    private static final SystemConfig SINGLETON = new SystemConfig();
    
    /**
     * The sampling frequency to use when polling data from the sensors.
     */
    private double samplingFrequency = 29.97;
    
    /**
     * The name of sensors configured for the system.
     */
    private Set<String> sensorNames = new HashSet<>();
    
    /**
     * The binding of sensor name to system resource.
     */
    private Map<String, String> sensorBindings = new HashMap();
    
    /**
     * Initializes a new instance of the SystemConfig class.
     */
    private SystemConfig() {
        LOGGER.info("Loading system configuration.");
        
        // try to load config from disk
        
    }
    
    /**
     * Gets the sampling frequency to use when polling data from the sensors.
     * @return The sampling frequency to use when polling data from the sensors.
     */
    public double getSamplingFrequency() {
        return this.samplingFrequency;
    }
    
    /**
     * Gets the names of the sensors configured for use in the system.
     * @return The names of the sensors configured for use in the system.
     */
    public Set<String> getSensorNames() {
        return Collections.unmodifiableSet(this.sensorNames);
    }
    
    /**
     * Gets a mapping of sensor names to their binding location.
     * @return A mapping of sensor names to their binding location.
     */
    public Map<String, String> getSensorBindings() {
        return Collections.unmodifiableMap(this.sensorBindings);
    }
    
    /**
     * Gets the SystemConfig singleton instance.
     * @return The SystemConfig singleton instance.
     */
    public static SystemConfig getConfig() {
        return SystemConfig.SINGLETON;
    }
    
    /**
     * Writes configuration to an XMLStreamWriter.
     * @param toWriteTo The XMLStreamWriter to write to.
     * @throws XMLStreamException If an exception occurs writing to the stream.
     */
    public void writeTo(XMLStreamWriter toWriteTo) throws XMLStreamException {
        toWriteTo.writeStartDocument();
        
        toWriteTo.writeStartElement("configuration");
        
        // save sampling frequency
        toWriteTo.writeStartElement("samplingfrequency");
        toWriteTo.writeCharacters(Double.toString(this.samplingFrequency));
        toWriteTo.writeEndElement();
        
        // save sensor names
        toWriteTo.writeStartElement("sensornames");
        for (String name : this.sensorNames) {
            toWriteTo.writeStartElement("name");
            toWriteTo.writeCharacters(name);
            toWriteTo.writeEndElement();
        }
        toWriteTo.writeEndElement();
        
        // close configuration
        toWriteTo.writeEndElement();
        
        // close document
        toWriteTo.writeEndDocument();
    }
    
    /**
     * Read configuration data from an XMLStreamWriter.
     * @param toReadFrom The XMLStreamReader to read from.
     * @throws XMLStreamException If an exception occurs writing to the stream.
     */
    protected void readFrom(XMLStreamReader toReadFrom) throws XMLStreamException {
        if (toReadFrom.getEventType() != XMLStreamConstants.START_DOCUMENT) {
            throw new XMLStreamException("Expected start of document not found.", toReadFrom.getLocation());
        }
        
        // read into configuration element
        if (toReadFrom.next() != XMLStreamConstants.START_ELEMENT || !"configuration".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <configuration> not found.", toReadFrom.getLocation());
        }
        
        // read into sampling frequency element
        if (toReadFrom.next() != XMLStreamConstants.START_ELEMENT || !"samplingfrequency".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <samplingfrequency> not found.", toReadFrom.getLocation());
        }
        
        // read and parse sampling frequency
        if (toReadFrom.next() != XMLStreamConstants.CHARACTERS) {
            throw new XMLStreamException("Expected sampling frequency string not found.", toReadFrom.getLocation());
        }
        
        try {
            this.samplingFrequency = Double.valueOf(toReadFrom.getText());
        } catch (NumberFormatException ex) {
            LOGGER.error("Error parsing sampling frequency when loading Capture from XML.", ex);
            throw new XMLStreamException("Error parsing sampling frequency when loading Capture from XML.", toReadFrom.getLocation(), ex);
        }
        
        // read into close sampleing interval element
        if (toReadFrom.next() != XMLStreamConstants.END_ELEMENT || !"samplingfrequency".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </samplingfrequency> not found.", toReadFrom.getLocation());
        }
        
        // read into sensor names element
        if (toReadFrom.next() != XMLStreamConstants.START_ELEMENT || !"sensornames".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <sensornames> not found.", toReadFrom.getLocation());
        }
        
        // set of sensor names
        Set<String> sensorNames = new HashSet<>();
        
        // add a name element for each name entry
        while (toReadFrom.next() == XMLStreamConstants.START_ELEMENT && "name".equals(toReadFrom.getLocalName())) {
            if (toReadFrom.next() != XMLStreamConstants.CHARACTERS) {
                throw new XMLStreamException("Expected sensor name string not found.", toReadFrom.getLocation());
            }
            
            String sensorName = toReadFrom.getText();
            
            // read into close name element
            if (toReadFrom.next() != XMLStreamConstants.END_ELEMENT || !"name".equals(toReadFrom.getLocalName())) {
                throw new XMLStreamException("Expected </name> not found.", toReadFrom.getLocation());
            }
            
            // successfully found a sensor name, save it
            sensorNames.add(sensorName);
        }
        
        // read into close element
        if (toReadFrom.getEventType() != XMLStreamConstants.END_ELEMENT || !"sensornames".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </sensornames> not found.", toReadFrom.getLocation());
        }
        
        // read into close configuration
        if (toReadFrom.next() != XMLStreamConstants.END_ELEMENT || !"configuration".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </configuration> not found.", toReadFrom.getLocation());
        }
        
        // read to end of document
        if (toReadFrom.next() != XMLStreamConstants.END_DOCUMENT) {
            throw new XMLStreamException("Expected end of document not found.", toReadFrom.getLocation());
        }
        
        this.sensorNames = sensorNames;
    }
}
