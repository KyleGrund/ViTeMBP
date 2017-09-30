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
package com.vitembp.services.config;

import com.vitembp.embedded.data.XMLStreams;
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
public class ServicesConfig {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * The name of the file to save and load configuration from.
     */
    private static final Path CONFIG_FILE_PATH = Paths.get("vitembp_services_config.xml");
    
    /**
     * The singleton instance for this class.
     */
    private static final ServicesConfig SINGLETON = new ServicesConfig();
    
    /**
     * The path to the configuration file.
     */
    private final Path configFile;
    
    /**
     * A boolean value indicating whether we loaded the configuration from
     * the file system at startup.
     */
    private boolean loadedConfigFromFile = false;
    
    /**
     * A boolean value indicating whether to start HTTP interface.
     */
    private boolean enableHttpInterface = true;
    
    /**
     * The port to bind the HTTP interface to.
     */
    private int httpInterfacePort = 8080;
    
    /**
     * A boolean value indicating whether to start AWS SQS interface.
     */
    private boolean enableAwsSqsInterface = true;
    
    /**
     * The temporary directory for processing files.
     */
    private Path tempDirectory = Paths.get("");
    
    /**
     * The name of the SQS queue to bind to.
     */
    private String awsSqsQueueName = "ViTeMBP-Service-Queue";
    
    /**
     * Initializes a new instance of the SystemConfig class.
     */
    private ServicesConfig() {
        // build the system board specific configuration path
        this.configFile = ServicesConfig.CONFIG_FILE_PATH;
        
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
            try {
                this.saveConfigToPath(ServicesConfig.CONFIG_FILE_PATH);
            } catch (IOException | XMLStreamException ex) {
                LOGGER.error("Exception saving default configuraiton.", ex);
            }
        }
        
        try {
            this.tempDirectory = Files.createTempDirectory("ViTeMBP");
        } catch (IOException ex) {
            LOGGER.error("Could not create temporary directory.", ex);
        }
    }

    /**
     * Gets the directory to put temporary files during processing.
     * @return The directory to put temporary files during processing.
     */
    public Path getTemporaryDirectory() {
        return this.tempDirectory;
    }
    
    /**
     * Returns a boolean value indicating whether to enable the HTTP interface.
     * @return A boolean value indicating whether to enable the HTTP interface.
     */
    public boolean getEnableHttpInterface() {
        return this.enableHttpInterface;
    }
    
    /**
     * Gets the port to bind the HTTP interface to.
     * @return The port to bind the HTTP interface to.
     */
    public int getHttpInterfacePort() {
        return this.httpInterfacePort;
    }
    
    /**
     * Returns a boolean value indicating whether to enable the SQS interface.
     * @return A boolean value indicating whether to enable the SQS interface.
     */
    public boolean getEnableSqsInterface() {
        return this.enableAwsSqsInterface;
    }
    
    /**
     * Gets the name of the SQS queue to bind to.
     * @return The name of the SQS queue to bind to.
     */
    public String getSqsQueueName() {
        return this.awsSqsQueueName;
    }
    
    /**
     * Sets a boolean value indicating whether to enable the SQS interface.
     * @param enable A boolean value indicating whether to enable the SQS interface.
     */
    public void setEnableSqsInterface(boolean enable) {
        this.enableAwsSqsInterface = enable;
    }

    /**
     * sets the name of the SQS queue to bind to.
     * @param name The name of the SQS queue to bind to.
     */
    public void setSqsQueueName(String name) {
        this.awsSqsQueueName = name;
    }

    /**
     * Sets a boolean value indicating whether to enable the HTTP interface.
     * @param enable A boolean value indicating whether to enable the HTTP interface.
     */
    public void setEnableHttpInterface(boolean enable) {
        this.enableHttpInterface = enable;
    }

    /**
     * Sets the port to bind the HTTP interface to.
     * @param port The port to bind the HTTP interface to.
     */
    public void setHttpInterfacePort(int port) {
        this.httpInterfacePort = port;
    }
    
    /**
     * Gets the SystemConfig singleton instance.
     * @return The SystemConfig singleton instance.
     */
    public static ServicesConfig getConfig() {
        return ServicesConfig.SINGLETON;
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
//        Path configDir = configFile.getParent();
//        LOGGER.info("Saving configuration to: " + configFile);
//        // if the directory doesn't exits try create it or the writer can't be made        
//        if (!Files.isDirectory(configDir)) {
//            LOGGER.info("Config path not found, creating: " + configDir.toString());
//            Files.createDirectory(configDir);
//        }
        
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
        
        // save HTTP interface settings
        toWriteTo.writeStartElement("enablehttpinterface");
        toWriteTo.writeCharacters(Boolean.toString(this.enableHttpInterface));
        toWriteTo.writeEndElement();
        
        toWriteTo.writeStartElement("httpinterfaceport");
        toWriteTo.writeCharacters(Integer.toString(this.httpInterfacePort));
        toWriteTo.writeEndElement();
        
        // save AWS SQS settings
        toWriteTo.writeStartElement("enableawssqsinterface");
        toWriteTo.writeCharacters(Boolean.toString(this.enableAwsSqsInterface));
        toWriteTo.writeEndElement();
        
        toWriteTo.writeStartElement("awssqsqueuename");
        toWriteTo.writeCharacters(this.awsSqsQueueName);
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

        // read HTTP interface settings
        toReadFrom.next();
        this.enableHttpInterface = Boolean.parseBoolean(XMLStreams.readElement("enablehttpinterface", toReadFrom));
        this.httpInterfacePort = Integer.parseInt(XMLStreams.readElement("httpinterfaceport", toReadFrom));
        
        // read AWS SQS settings
        this.enableAwsSqsInterface = Boolean.parseBoolean(XMLStreams.readElement("enableawssqsinterface", toReadFrom));
        this.awsSqsQueueName = XMLStreams.readElement("awssqsqueuename", toReadFrom);
        
        // read into close configuration
        if (toReadFrom.getEventType() != XMLStreamConstants.END_ELEMENT || !"configuration".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </configuration> not found.", toReadFrom.getLocation());
        }
        
        // read to end of document
        if (toReadFrom.next() != XMLStreamConstants.END_DOCUMENT) {
            throw new XMLStreamException("Expected end of document not found.", toReadFrom.getLocation());
        }
    }
}
