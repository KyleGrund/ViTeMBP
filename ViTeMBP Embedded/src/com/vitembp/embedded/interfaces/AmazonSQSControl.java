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
package com.vitembp.embedded.interfaces;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Allows control via an Amazon SQS queue.
 */
public class AmazonSQSControl {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * The singleton instance of this class.
     */
    private static AmazonSQSControl singleton;
    
    /**
     * Flag indicating whether the instance is running.
     */
    private boolean isRunning = false;
    
    /**
     * Client instance to use to connect to SQS.
     */
    AmazonSQS sqsClient;
    
    /**
     * The name of the queue to monitor.
     */
    private final String queueName;
    
    /**
     * The thread which runs the function to processes messages.
     */
    private Thread messageProcessThread;
    
    /**
     * The URL to use when accessing the queue.
     */
    private String queueUrl = null;
    
    /**
     * The the client for backing store where the stored commands are located.
     */
    private final AmazonDynamoDB client;
    
    /**
     * The function which parses messages.
     */
    private final Function<String, String> messageParser;
    
    /**
     * Initializes a new instance of the AmazonSQSControl class.
     */
    public AmazonSQSControl(String queueName, Function<String, String> msgParser) {
        // create client to use to communicate with sqs
        this.sqsClient = AmazonSQSClientBuilder.defaultClient();
        
        // save the function which parses messages
        this.messageParser = msgParser;
        
        // save the name of the queue for this device
        this.queueName = queueName;
        
        // build DynamoDB client with default credentials
        this.client = AmazonDynamoDBClient.builder().build();
    }

    /**
     * Attempts to create the queue for this device.
     */
    public void createQueue() {
        // if the queue was not yet created
        if (this.queueUrl == null) {
            // create the queue
            this.sqsClient.createQueue(new CreateQueueRequest().withQueueName(this.queueName).addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20"));
            // save the url for use when acknowledging messages
            this.queueUrl = this.sqsClient.getQueueUrl(this.queueName).getQueueUrl();
        }
    }
    
    /**
     * Starts the cloud sync service.
     */
    public synchronized void start() {
        // if not already started build and start the sync thread
        if (this.isRunning != true) {
            this.messageProcessThread = new Thread(this::processMessages);
            this.messageProcessThread.setName("AWSSQS-Control");
            this.isRunning = true;
            this.messageProcessThread.start();
            LOGGER.info("AmazonSQSControl service started.");
        }
    }
    
    /**
     * Stops the cloud sync service.
     */
    public synchronized void stop(){
        if (this.isRunning) {
            this.isRunning = false;
            try {
                this.messageProcessThread.join();
                LOGGER.info("AmazonSQSControl service stopped.");
            } catch (InterruptedException ex) {
                LOGGER.error("Interrupted wiating for message processing thread to complete.", ex);
            }
        }
    }
    
    /**
     * Processes messages from the device's SQS queue.
     */
    private void processMessages() {
        int startErrorBackoff = 200;
        int errorBackoff = startErrorBackoff;
        float errorFactor = 2;
        int errorMax = 5000;
        while (isRunning) {
            try {
                // create the queue in this try-block so if the service starts
                // when a connection is not available it will cleanly retry
                this.createQueue();
                
                // check for new commands
                ReceiveMessageResult result = sqsClient.receiveMessage(queueUrl);

                // process commands
                result.getMessages().forEach((msg) -> {
                    // get message text
                    String toProcess = msg.getBody();

                    // remove message from queue
                    this.sqsClient.deleteMessage(this.queueUrl, msg.getReceiptHandle());

                    // process the message
                    this.parseMessage(toProcess);
                });
                
                // reset error backoff on success
                errorBackoff = startErrorBackoff;
            } catch (Exception e) {
                LOGGER.error("Unexpected Exception processing SQS queue.", e);
                
                // wait for the backoff period to prevent retry flooding
                try {
                    LOGGER.error("Backing off for " + Integer.toString(errorBackoff) + "ms.");
                    Thread.sleep(errorBackoff);
                } catch (InterruptedException ex) {
                    LOGGER.error("Interrupted while waiting for backoff on SQS queue failure.", ex);
                }
                
                // increase backoff factor until it is at the max value
                errorBackoff *= errorFactor;
                if (errorBackoff > errorMax) {
                    errorBackoff = errorMax;
                }
                
            }
        }
    }
    
    /**
     * Parses a message from the queue.
     * @param toProcess The message to process.
     */
    private void parseMessage(String toProcess) {
        LOGGER.info("Processing device queue message: " + toProcess);
        
        // process message
        String upperCase = toProcess.toUpperCase();
        if (upperCase.startsWith("FROMUUID")) {
            String[] split = toProcess.split(" ");
            
            // from must be "FROMUUID [UUID LOCATION]"
            if (split.length != 2) {
                LOGGER.error("Invalid format processing FROMUUID command.");
            } else {
                UUID location = null;
                try {
                    location = UUID.fromString(split[1]);
                } catch (IllegalArgumentException e) {
                    LOGGER.error("UUID location is not valid.", e);
                    return;
                }
                
                String uuidCommand = null;
                try {
                    uuidCommand = this.readData(location);
                } catch (IOException ex) {
                    LOGGER.error("Could not read command from database store while processing FROMUUID command.", ex);
                    return;
                }
                
                try {
                    this.deleteData(location);
                } catch (IOException ex) {
                    LOGGER.error("Could not delete command from database store while processing FROMUUID command.", ex);
                    return;
                }
                
                LOGGER.info("Processing command from database: " + uuidCommand);
                
                if (uuidCommand.length() < 37) {
                    LOGGER.error("Command not of form: \"[UUID] [COMMAND]\".");
                    return;
                }
                
                UUID responseLocation = null;
                try {
                    responseLocation = UUID.fromString(uuidCommand.substring(0, 36));
                } catch (IllegalArgumentException ex) {
                    LOGGER.error("UUID location is not valid.", ex);
                    return;
                }
                
                String result = this.messageParser.apply(uuidCommand.substring(37));
                LOGGER.info("Command result: " + result);
                try {
                    this.writeData(responseLocation, result);
                } catch (IOException ex) {
                    LOGGER.error("Could not write result of proccsing FROMUUID command.", ex);
                }
            }
        } else {
            // use the uuid processing to prevent duplication
            String result = this.messageParser.apply(toProcess);
            LOGGER.info("Command result: " + result);
        }
    }
    
    /**
     * Deletes a value from the DATA table.
     * @param location The location to delete in the table.
     * @throws IOException If an error occurs deleting the data.
     */
    private void deleteData(UUID location) throws IOException {
        try {
            // build the request to put toWrite in VALUE at the ID location
            Map<String, AttributeValue> keyAttribs = new HashMap<>();
            keyAttribs.put("ID", new AttributeValue().withS(location.toString()));

            // write the request to the DATA table
            client.deleteItem("DATA", keyAttribs);
        } catch (Exception ex) {
            throw new IOException(
                    "Unexpected exception deleting data from location: " + 
                            location.toString(),
                    ex);
        }
    }
    
    /**
     * Deletes a value from the DATA table.
     * @param location The location to delete in the table.
     * @param toWrite The data to write to the table row.
     * @throws IOException If an error occurs writing the data.
     */
    private void writeData(UUID location, String toWrite) throws IOException {
        try {
            // build the request to put toWrite in VALUE at the ID location
            Map<String, AttributeValue> keyAttribs = new HashMap<>();
            keyAttribs.put("ID", new AttributeValue().withS(location.toString()));
            Map<String, AttributeValueUpdate> updateAttribs = new HashMap<>();
            updateAttribs.put("VALUE", new AttributeValueUpdate()
                    .withValue(new AttributeValue().withS(toWrite))
                    .withAction(AttributeAction.PUT));

            // write the request to the DATA table
            client.updateItem("DATA", keyAttribs, updateAttribs);
        } catch (Exception ex) {
            throw new IOException(
                    "Unexpected exception writing \"" + 
                            toWrite  +
                            "\" to location: " +
                            location.toString(),
                    ex);
        }
    }
    
    /**
     * Reads a value from the DATA table.
     * @param location The location to read VALUE from in the table.
     * @return The data from the table.
     * @throws IOException If an error occurs reading the data.
     */
    private String readData(UUID location) throws IOException{
        try {
            Map<String, AttributeValue> reqkey = new HashMap<>();
            reqkey.put("ID", new AttributeValue().withS(location.toString()));

            GetItemRequest request = new GetItemRequest()
                    .withTableName("DATA")
                    .withKey(reqkey)
                    .withAttributesToGet(Arrays.asList(new String[] { "VALUE" }));

            // try to get the data
            GetItemResult result = client.getItem(request);
            if (result != null && result.getItem() != null) {
                // parse data from response if the table has the attributes
                Map<String, AttributeValue> attributes = result.getItem();

                // can not continue if the VALUE is attribute is not present
                if (!attributes.containsKey("VALUE")) {
                    LOGGER.error("Value attribute is not in database.");
                    return null;
                }

                // data are present, get their values
                return result.getItem().get("VALUE").getS();
            } else {
                // query failed so do not continue
                LOGGER.error("Could not get value from databse.");
                return null;
            }
        } catch (Exception ex) {
            throw new IOException(
                    "Unexpected exception reading from location: " + location.toString(),
                    ex);
        }
    }
}