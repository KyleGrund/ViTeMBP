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

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.apache.logging.log4j.LogManager;

/**
 * Class providing synchronization with a cloud configuration provider.
 */
public class CloudConfigSync {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * The interval in milliseconds between configuration checks.
     */
    private static final int CHECK_INTERVAL_MS = 30 * 1000;
    
    /**
     * Thread that runs the sync target.
     */
    private static Thread syncThread;
    
    /**
     * A flag indicating whether the sync should be running.
     */
    private static boolean isRunning = false;
    
    /**
     * The connection to the database.
     */
    private static AmazonDynamoDB client;
    private static boolean deviceRegistered;
    
    /**
     * Static initializer.
     */
    static {
        // build DynamoDB client with default credentials
        client =  AmazonDynamoDBClient.builder().build();
    }
    
    /**
     * Starts the cloud sync service.
     */
    public static synchronized void Start() {
        // if not already started build and start the sync thread
        if (CloudConfigSync.isRunning != true) {
            CloudConfigSync.syncThread = new Thread(CloudConfigSync::SyncThreadTarget);
            CloudConfigSync.isRunning = true;
            CloudConfigSync.syncThread.start();
            LOGGER.info("Configuration cloud sync service started.");
        }
    }
    
    /**
     * Stops the cloud sync service.
     */
    public static synchronized void Stop(){
        if (CloudConfigSync.isRunning) {
            CloudConfigSync.isRunning = false;
            try {
                CloudConfigSync.syncThread.join();
                LOGGER.info("Configuration cloud sync service stopped.");
            } catch (InterruptedException ex) {
                LOGGER.error("Interrupted wiating for config sync thread to complete.", ex);
            }
        }
    }
    
    /**
     * The function that the sync thread runs.
     */
    private static void SyncThreadTarget() {
        long checkStarted, msToNextStart;
        while (isRunning) {
            checkStarted = System.currentTimeMillis();
            
            // check configuration
            CloudConfigSync.CheckConfiguration();
            
            // wait until next check time
            msToNextStart = CHECK_INTERVAL_MS - (System.currentTimeMillis() - checkStarted);
            if (msToNextStart > 0) {
                try {
                    LOGGER.debug(
                            "Waiting " +
                            Long.toString(msToNextStart) +
                            "ms until next sync.");
                    Thread.sleep(msToNextStart);
                } catch (InterruptedException ex) {
                    LOGGER.error("Interrupted waiting for next check interval.", ex);
                    isRunning = false;
                }
            }
        }
    }
    
    /**
     * Checks if the configuration has changed.
     */
    private static void CheckConfiguration() {
        // register the device if it is not in the table
        if (!CloudConfigSync.deviceRegistered) {
            try {
                CloudConfigSync.registerDevice();
            } catch (IOException ex) {
                // log the error
                LOGGER.error("Could not register device.", ex);
                // do not continue we cannot update config if it is not registered
                return;
            }
        }

        // check if configuration has changed
        boolean hasChanged;
        String newConfig;
        
        // perform query
        Map<String, AttributeValue> reqkey = new HashMap<>();
        reqkey.put("ID", new AttributeValue().withS(SystemConfig.getConfig().getSystemUUID().toString()));
        
        GetItemRequest request = new GetItemRequest()
                .withTableName("DEVICES")
                .withKey(reqkey)
                .withProjectionExpression("CONFIG,UPDATED");
        
        // try to get the data
        GetItemResult result = client.getItem(request);
        if (result != null && result.getItem() != null) {
            // parse data from response if the table has the attributes
            Map<String, AttributeValue> attributes = result.getItem();

            // can not continue if the UPDATED and CONFIG attributes are not
            // present in the table row
            if (!attributes.containsKey("UPDATED") || !attributes.containsKey("CONFIG")) {
                LOGGER.error("Configuration is not available in database.");
                return;
            }
            
            // attributes are present, get their values
            hasChanged = Boolean.parseBoolean(result.getItem().get("UPDATED").getS());
            newConfig = result.getItem().get("CONFIG").getS();
        } else {
            // query failed so do not continue
            LOGGER.error("Could not query configuration in databse.");
            return;
        }
        
        // update if changed
        if (hasChanged) {
            try {
                updateConfiguration(newConfig);
            } catch (XMLStreamException ex) {
                LOGGER.error("Could not update configuration.", ex);
                return;
            }
            
            // change updated to false
            Map<String, AttributeValue> keyAttribs = new HashMap<>();
            keyAttribs.put("ID", new AttributeValue().withS(SystemConfig.getConfig().getSystemUUID().toString()));
            Map<String, AttributeValueUpdate> updateAttribs = new HashMap<>();
            updateAttribs.put("UPDATED", new AttributeValueUpdate()
                    .withValue(new AttributeValue().withS(Boolean.toString(false)))
                    .withAction(AttributeAction.PUT));
            
            client.updateItem("DEVICES", keyAttribs, updateAttribs);
            LOGGER.info("Config updated flag set to false.");
        }
    }
    
    /**
     * Updates the configuration from the string read from the database.
     * @param newConfig The string read from the database.
     * @throws XMLStreamException If there is an exception while updating the
     * configuration.
     */
    private static void updateConfiguration(String newConfig) throws XMLStreamException {
        // load config from the remote string
        LOGGER.info("Loading new configuration: " + newConfig);
        SystemConfig.getConfig().readFromString(newConfig);
        
        try {
            // save the config to the local system
            SystemConfig.getConfig().saveToLocalSystem();
        } catch (IOException ex) {
            throw new XMLStreamException("Exception saving config.", ex);
        }
    }
    
    /**
     * Registers the device's ID to the database.
     * @throws IOException 
     */
    private static void registerDevice() throws IOException {
        // get configuration string
        String config;
        try {
            config = SystemConfig.getConfig().writeToString();
        } catch (XMLStreamException ex) {
            throw new IOException("Could not generate configuration string.", ex);
        }
        
        // add holds data to store in table.
        Map<String, AttributeValue> itemToSet = new HashMap<>();
        PutItemRequest pir = new PutItemRequest();
        pir.setConditionExpression("attribute_not_exists(ID)");
        // add the system ID
        itemToSet.put(
                "ID",
                new AttributeValue(SystemConfig.getConfig().getSystemUUID().toString()));
        itemToSet.put(
                "CONFIG",
                new AttributeValue(config));
        itemToSet.put(
                "UPDATED",
                new AttributeValue(Boolean.toString(false)));
        pir.setTableName("DEVICES");
        pir.setItem(itemToSet);
        
        try {
            CloudConfigSync.client.putItem(pir);
            CloudConfigSync.deviceRegistered = true;
            LOGGER.info("Device successfully registered.");
        } catch (ConditionalCheckFailedException e) {
            CloudConfigSync.deviceRegistered = true;
            LOGGER.debug("Device already registered.");
        } catch (ResourceNotFoundException e) {
            LOGGER.error("The database does not contain the device index table.", e);
        } catch (AmazonServiceException e) {
            LOGGER.error("Exception occurred writing to database.", e);
        }
    }
    
}
