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

import com.vitembp.embedded.data.CaptureTypes;
import com.vitembp.embedded.data.XMLStreams;
import com.vitembp.embedded.hardware.SystemInfo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
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
     * The name of the file to save and load configuration from.
     */
    private static final Path CONFIG_FILE_PATH = Paths.get("vitembp_config.xml");
    
    /**
     * A UUID representing the type of this sensor.
     */
    private static final UUID DEFAULT_UUID = UUID.fromString("2ae1239a-3389-4580-b704-ff5c7b4dd3ee");
    
    /**
     * The singleton instance for this class.
     */
    private static final SystemConfig SINGLETON = new SystemConfig();
    
    /**
     * The path to the configuration file.
     */
    private final Path configFile;
    
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
    private Map<String, UUID> sensorBindings = new HashMap<>();
    
    /**
     * A boolean value indicating whether we loaded the configuration from
     * the file system at startup.
     */
    private boolean loadedConfigFromFile = false;
    
    /**
     * The type of capture to use to store captured data.
     */
    private CaptureTypes captureType = CaptureTypes.EmbeddedH2;
    
    /**
     * A boolean value indicating whether to upload collected data to the cloud.
     */
    private boolean uploadToCloud = true;
    
    /**
     * A boolean value indicating whether to delete local data after it has been
     * synchronized with the cloud.
     */
    private boolean deleteOnUploadToCloud = true;
    
    /**
     * A boolean value indicating whether data should be stored compressed.
     */
    private boolean enableCompression = true;
    
    /**
     * The UUID representing this instance.
     */
    private UUID systemID = DEFAULT_UUID;
    
    /**
     * Initializes a new instance of the SystemConfig class.
     */
    private SystemConfig() {
        // build the system board specific configuration path
        this.configFile = SystemInfo.getConfigDirectory().resolve(SystemConfig.CONFIG_FILE_PATH);
        
        // try to load config from disk
        if (Files.exists(this.configFile)) {
            LOGGER.info("Found system config on filesystem.");
            try {
                loadConfigFromPath(this.configFile);
                this.loadedConfigFromFile = true;
            } catch (IOException | XMLStreamException ex) {
                LOGGER.error("Exception loading system config from file.", ex);
            }
        } else {
            LOGGER.info("System configuration not found on filesystem.");
        }
    }

    /**
     * Returns a boolean value indicating whether to compress data before it
     * is stored.
     * @return A boolean value indicating whether to compress data before it
     * is stored.
     */
    public boolean getEnableCompression() {
        return this.enableCompression;
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
    public Map<String, UUID> getSensorBindings() {
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
     * Returns a boolean value indicating whether the SystemConfig object was
     * initialized from the settings save to the default location in the
     * file system.
     * @return A boolean value indicating whether the SystemConfig object was
     * initialized from the settings save to the default location in the
     * file system.
     */
    public boolean initializedFromFile() {
        return this.loadedConfigFromFile;
    }
    
    /**
     * Gets the type of Capture to use to store sensor data.
     * @return The type of Capture to use to store sensor data.
     */
    public CaptureTypes getCaptureType() {
        return this.captureType;
    }
    
    /**
     * Returns a boolean value indicating whether to upload local data to the
     * cloud.
     * @return A boolean value indicating whether to upload local data to the
     * cloud.
     */
    public boolean getUploadToCloud() {
        return this.uploadToCloud;
    }
    
    /**
     * Returns a boolean value indicating whether to delete local data on
     * upload local data to the cloud.
     * @return A boolean value indicating whether to delete local data on
     * upload local data to the cloud.
     */
    public boolean getDeleteOnUploadToCloud() {
        return this.deleteOnUploadToCloud;
    }
    
    /**
     * Gets the unique UUID of this system.
     * @return The unique UUID of this system.
     */
    public UUID getSystemUUID() {
        return this.systemID;
    }
    
    /**
     * Loads the configuration from a file.
     * @param configFile The file to load from.
     */
    private void loadConfigFromPath(Path configFile) throws IOException, XMLStreamException {
        // the configuration xml will be read into this string then parsed
        String config;
        
        // path may be in the filesystem or in the local assembly, try both
        // defaulting to the more commonly used filesystem
        if (Files.exists(configFile)) {
            LOGGER.info("Loading config from path: " + configFile.toAbsolutePath().toString());
            
            // read in all lines in file and combine them to a single string
            config = Files
                    .lines(configFile)
                    .reduce("", (a, b) -> a + b);
        } else {
            // read in all lines from assembly file and combine them to a single string
            // replace the system path seperator with '/' to support operating
            // systems that use another character
            String pathToResource = configFile.toString().replace(File.separator, "/");
            
            LOGGER.info("Loading config from path: " + pathToResource);
            
            InputStream in = this.getClass().getResourceAsStream(pathToResource);
            config = new BufferedReader(new InputStreamReader(in))
                    .lines()
                    .reduce("", (a, b) -> a + b);
        }
        
        // create an XMLStreamReader for the string read in using a filter to
        // remove any extranuous white space
        XMLInputFactory factory = XMLInputFactory.newFactory();
        XMLStreamReader xmlReader =
                factory.createFilteredReader(
                        factory.createXMLStreamReader(new StringReader(config)),
                        (XMLStreamReader sr) -> !sr.isWhiteSpace());

        // read config from the file
        this.readFrom(xmlReader);
    }
    
    /**
     * Saves the configuration to a file.
     * @param configFile The file to save to.
     */
    private void saveConfigToPath(Path configFile) throws IOException, XMLStreamException {
        Path configDir = configFile.getParent();
        LOGGER.info("Saving configuration to: " + configFile);
        // if the directory doesn't exits try create it or the writer can't be made        
        if (!Files.isDirectory(configDir)) {
            LOGGER.info("Config path not found, creating: " + configDir.toString());
            Files.createDirectory(configDir);
        }
        
        // create a buffered writer to output the file to
        try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
            // create a stream to write config to
            XMLStreamWriter configOutputStream = XMLOutputFactory
                    .newFactory()
                    .createXMLStreamWriter(writer);
            
            // write config to the file
            this.writeTo(configOutputStream);
            configOutputStream.flush();
            configOutputStream.close();
        }
    }
    
    /**
     * Creates a default configuration from the file specified.
     * @param location The location of the default configuration.
     * @throws java.io.IOException If an exception occurs loading the default
     * configuration or saving the loaded configuration to the file system.
     */
    public void createDefaultConfigFrom(Path location) throws IOException {
        try {
            // prevent overwriting existing config with defaults
            if (this.loadedConfigFromFile) {
                throw new IllegalStateException("Cannot create default configuration when already initialized.");
            }
            
            LOGGER.info("Loading default configuration.");
            
            // load the config
            this.loadConfigFromPath(location);
            
            // create a new, unique, system ID
            this.systemID = UUID.randomUUID();
        } catch (XMLStreamException | IllegalStateException | IOException ex) {
            throw new IOException("Exception reading default configuration from: " + location.toString(), ex);
        }
        
        try {
            // save the configuraiton
            this.saveConfigToPath(this.configFile);
            LOGGER.info("Saved default ocnfiguration to: " + this.configFile.toString());
        } catch (XMLStreamException ex) {
            throw new IOException("Exception writing default configuration to: " + this.configFile.toString(), ex);
        }
        
        this.loadedConfigFromFile = true;
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
        toWriteTo.writeStartElement("systemid");
        toWriteTo.writeCharacters(this.systemID.toString());
        toWriteTo.writeEndElement();
        
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
        
        // save sensor bindings
        toWriteTo.writeStartElement("sensorbindings");
        for (String name : this.sensorBindings.keySet()) {
            toWriteTo.writeStartElement("sensorbinding");

            // save name
            toWriteTo.writeStartElement("name");
            toWriteTo.writeCharacters(name);
            toWriteTo.writeEndElement();
            
            // save binding
            toWriteTo.writeStartElement("binding");
            toWriteTo.writeCharacters(this.sensorBindings.get(name).toString());
            toWriteTo.writeEndElement();
            
            toWriteTo.writeEndElement();
        }
        toWriteTo.writeEndElement();
        
        // save capture type
        toWriteTo.writeStartElement("capturetype");
        toWriteTo.writeCharacters(this.captureType.name());
        toWriteTo.writeEndElement();
        
        // save capture type
        toWriteTo.writeStartElement("enablecompression");
        toWriteTo.writeCharacters(Boolean.toString(this.enableCompression));
        toWriteTo.writeEndElement();
        
        // save upload options
        toWriteTo.writeStartElement("cloud");
        toWriteTo.writeStartElement("uploadtocloud");
        toWriteTo.writeCharacters(Boolean.toString(this.uploadToCloud));
        toWriteTo.writeEndElement();
        toWriteTo.writeStartElement("deleteonuploadtocloud");
        toWriteTo.writeCharacters(Boolean.toString(this.deleteOnUploadToCloud));
        toWriteTo.writeEndElement();
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
    protected final void readFrom(XMLStreamReader toReadFrom) throws XMLStreamException {
        if (toReadFrom.getEventType() != XMLStreamConstants.START_DOCUMENT) {
            throw new XMLStreamException("Expected start of document not found.", toReadFrom.getLocation());
        }
        
        // read into configuration element
        if (toReadFrom.next() != XMLStreamConstants.START_ELEMENT || !"configuration".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <configuration> not found.", toReadFrom.getLocation());
        }

        // read system ID
        toReadFrom.next();
        this.systemID = UUID.fromString(XMLStreams.readElement("systemid", toReadFrom));
        
        // read sampling frequency
        this.samplingFrequency = Double.valueOf(XMLStreams.readElement("samplingfrequency", toReadFrom));
        
        // read into sensor names element
        if (toReadFrom.getEventType()!= XMLStreamConstants.START_ELEMENT || !"sensornames".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <sensornames> not found.", toReadFrom.getLocation());
        }
        
        // set of sensor names
        Set<String> readSensorNames = new HashSet<>();
        
        // add a name element for each name entry
        toReadFrom.next();
        while (toReadFrom.getEventType() == XMLStreamConstants.START_ELEMENT && "name".equals(toReadFrom.getLocalName())) {
            // read and save sensor name
            readSensorNames.add(XMLStreams.readElement("name", toReadFrom));
        }
        
        // read into close element
        if (toReadFrom.getEventType() != XMLStreamConstants.END_ELEMENT || !"sensornames".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </sensornames> not found.", toReadFrom.getLocation());
        }
        
        // read into sensorbindings element
        if (toReadFrom.next() != XMLStreamConstants.START_ELEMENT || !"sensorbindings".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <sensorbindings> not found.", toReadFrom.getLocation());
        }
        
        // map of sensor name to bindings
        Map<String, UUID> readSensorBindings = new HashMap<>();
        
        // add a name element for each name entry
        while (toReadFrom.next() == XMLStreamConstants.START_ELEMENT && "sensorbinding".equals(toReadFrom.getLocalName())) {
            // read name element
            toReadFrom.next();
            String sensorName = XMLStreams.readElement("name", toReadFrom);
            
            // read binding            
            UUID sensorBinding = UUID.fromString(XMLStreams.readElement("binding", toReadFrom));
            
            // successfully found a sensor name, save it
            readSensorBindings.put(sensorName, sensorBinding);
        }
        
        // read into close element
        if (toReadFrom.getEventType() != XMLStreamConstants.END_ELEMENT || !"sensorbindings".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </sensorbindings> not found.", toReadFrom.getLocation());
        }
        
        // read capture type
        toReadFrom.next();
        this.captureType = Enum.valueOf(CaptureTypes.class, XMLStreams.readElement("capturetype", toReadFrom));
        
        // read enable compression
        this.enableCompression = Boolean.valueOf(XMLStreams.readElement("enablecompression", toReadFrom));
        
        // read cloud options
        // read into cloud element
        if (toReadFrom.getEventType()!= XMLStreamConstants.START_ELEMENT || !"cloud".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <cloud> not found.", toReadFrom.getLocation());
        }
        
        toReadFrom.next();
        this.uploadToCloud = Boolean.valueOf(XMLStreams.readElement("uploadtocloud", toReadFrom));
        this.deleteOnUploadToCloud = Boolean.valueOf(XMLStreams.readElement("deleteonuploadtocloud", toReadFrom));
        
        // read into close cloud
        if (toReadFrom.getEventType()!= XMLStreamConstants.END_ELEMENT || !"cloud".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </cloud> not found.", toReadFrom.getLocation());
        }
        
        // read into close configuration
        if (toReadFrom.next() != XMLStreamConstants.END_ELEMENT || !"configuration".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </configuration> not found.", toReadFrom.getLocation());
        }
        
        // read to end of document
        if (toReadFrom.next() != XMLStreamConstants.END_DOCUMENT) {
            throw new XMLStreamException("Expected end of document not found.", toReadFrom.getLocation());
        }
        
        // configuration successfully read, safe to update settings
        this.sensorNames = readSensorNames;
        this.sensorBindings = readSensorBindings;
    }
}
