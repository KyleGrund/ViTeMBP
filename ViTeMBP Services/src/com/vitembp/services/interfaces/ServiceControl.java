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

import com.vitembp.embedded.interfaces.AmazonSQSControl;
import com.vitembp.services.ApiFunctions;
import com.vitembp.services.config.ServicesConfig;
import java.io.IOException;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides an interface for starting services.
 */
public class ServiceControl {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Starts services based on configuration.
     * @param functions The API functions to expose to the interfaces.
     */
    public static void startServices(ApiFunctions functions) {
        ServicesConfig config = ServicesConfig.getConfig();
        
        // start the HTTP interface if enabled
        if (config.getEnableHttpInterface()) {
            ServiceControl.startHttpService(functions, config.getHttpInterfacePort());
        }
        
        // start the SQS interface if enabled
        if (config.getEnableSqsInterface()) {
            ServiceControl.startSqsService(functions, config.getSqsQueueName());
        }
    }
    
    /**
     * Starts the amazon simple queue service.
     * @param functions The control object for executing the api calls.
     * @param name The name of the queue to attach to.
     * @return A boolean value indicating whether the service stated
     * successfully.
     */
    private static boolean startSqsService(final ApiFunctions functions, String name) {
        try {
            // create the SQS interface
            AmazonSQSControl sqs = new AmazonSQSControl(
                    name,
                    (cmd) -> SQSTarget.parseCommand(cmd, functions));
            sqs.start();
        } catch (Exception ex) {
            LOGGER.error("Unexpected exception starting SQS service.", ex);
            return false;
        }
        return true;
    }

    /**
     * Starts the local HTTP interface.
     * @param functions The API functions interface to use to execute requests.
     * @return A boolean value indicating whether starting was successful.
     */
    private static boolean startHttpService(ApiFunctions functions, int port) {
        System.out.println("Starting HTTP server.");
        try {
            Http server = new Http(port, functions);
        } catch (IOException ex) {
            LOGGER.error("Exception starting web server.", ex);
            return false;
        }
        return true;
    }
}
