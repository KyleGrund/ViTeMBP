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

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.vitembp.embedded.configuration.CloudConfigSync;
import com.vitembp.embedded.configuration.SystemConfig;
import com.vitembp.embedded.hardware.HardwareInterface;
import java.io.IOException;
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
     * The string to prepend to the queue name when creating queues.
     */
    private static final String QUEUE_PREFIX = "ViTeMBP-Device-";
    
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
    private final String queueUrl;
    
    /**
     * Initializes a new instance of the AmazonSQSControl class.
     */
    private AmazonSQSControl(String queueName) {
        // create client to use to communicate with sqs
        this.sqsClient = AmazonSQSClientBuilder.defaultClient();
        
        // save the name of the queue for this device
        this.queueName = queueName;
        
        // create the queue
        this.sqsClient.createQueue(this.queueName);
        this.queueUrl = this.sqsClient.getQueueUrl(queueName).getQueueUrl();
    }
    
    /**
     * Starts the cloud sync service.
     */
    public synchronized void start() {
        // if not already started build and start the sync thread
        if (this.isRunning != true) {
            this.messageProcessThread = new Thread(this::processMessages);
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
    
    private void processMessages() {
        while (isRunning) {
            // check for new commands
            ReceiveMessageResult result = sqsClient.receiveMessage(queueUrl);
            
            LOGGER.info("Processing messages from SQS device queue.");
            
            // process commands
            result.getMessages().forEach((msg) -> {
                // get message text
                String toProcess = msg.getBody();
                
                // remove message from queue
                this.sqsClient.deleteMessage(this.queueUrl, msg.getReceiptHandle());
                
                // process the message
                this.parseMessage(toProcess);
            });
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
        if ("REBOOT".equals(upperCase)) {
            try {
                HardwareInterface.getInterface().restartSystem();
            } catch (IOException ex) {
                LOGGER.error("Error processing reboot command.", ex);
            }
        } else if ("SHUTDOWN".equals(upperCase)) {
            try {
                HardwareInterface.getInterface().shutDownSystem();
            } catch (IOException ex) {
                LOGGER.error("Error processing reboot command.", ex);
            }
        } else if ("UPDATECONFIG".equals(upperCase)) {
            // trigger the cloud configuration service to check for updates
            CloudConfigSync.checkForUpdates();
        } else if ("STARTCAPTURE".equals(upperCase)) {
            try {
                // send the keypress '1' to signal start capture
                HardwareInterface.getInterface().generateKeyPress('1');
            } catch (InterruptedException ex) {
                LOGGER.error("Interrupted sending start capture keypress.", ex);
            }
        } else if ("ENDCAPTURE".equals(upperCase)) {
            try {
                // send the keypress '4' to signal start capture
                HardwareInterface.getInterface().generateKeyPress('4');
            } catch (InterruptedException ex) {
                LOGGER.error("Interrupted sending end capture keypress.", ex);
            }
        }
    }
    
    /**
     * Gets the singleton instance of the AmazonSQSControl class.
     * @return The singleton instance of the AmazonSQSControl class.
     */
    public synchronized static AmazonSQSControl getSingleton(){
        // build singleton is neccessary.
        if (AmazonSQSControl.singleton == null) {
            String name = QUEUE_PREFIX + SystemConfig.getConfig().getSystemUUID().toString();
            AmazonSQSControl.singleton = new AmazonSQSControl(name);
        }
        
        return AmazonSQSControl.singleton;
    }
}
