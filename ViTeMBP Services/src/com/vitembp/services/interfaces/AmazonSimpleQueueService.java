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
package com.vitembp.services.interfaces;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.vitembp.services.ApiFunctions;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;

/**
 * Provides an interface to Amazon SQS.
 */
public class AmazonSimpleQueueService {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * Provides an interface to the AWS simple queue service.
     */
    private final AmazonSQS queue;
    
    /**
     * The name of the queue to process commands from.
     */
    private final String queueUrl;
    
    /**
     * The name of the queue.
     */
    private final String queueName;
    
    /**
     * A callback to a function which processes messages.
     */
    private final Consumer<String> callback;
    
    /**
     * Initializes a new instance of the AmazonSQS class.
     * @param functions The API functions to provide an interface for.
     * @param callback
     * @param queueName The name of the queue to connect to.
     */
    public AmazonSimpleQueueService(
            ApiFunctions functions,
            Consumer<String> callback,
            String queueName) {
        // save parameters
        this.queueName = queueName;
        this.callback = callback;
        
        // create the queue connection
        this.queue = AmazonSQSClientBuilder.standard().build();
        this.queueUrl = this.queue.getQueueUrl(queueName).getQueueUrl();
        
        // creates processing thread
        this.start();
    }
    
    /**
     * Runs a thread that repeatedly processes messages from the queue.
     */
    private void start() {
        new Thread("AmazonSQS-" + this.queueName) {
            @Override
            public void run() {
                while (true) {
                    acceptCommand();
                }
            }
        }.start();
    }
    
    /**
     * Accepts a command from the queue.
     */
    private void acceptCommand() {
        ReceiveMessageResult result = this.queue.receiveMessage(this.queueUrl);
        int count = 1;
        for (Message msg : result.getMessages()) {
            System.out.println("Got message " + count++ + ": " + msg.getBody());
            // process command
            this.callback.accept(msg.getBody());
            
            // dequeue command
            this.queue.deleteMessage(this.queueUrl, msg.getReceiptHandle());
        }
    }
}
