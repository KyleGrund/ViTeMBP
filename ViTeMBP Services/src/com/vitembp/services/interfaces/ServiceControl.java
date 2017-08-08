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

import com.vitembp.services.ApiFunctions;
import com.vitembp.services.config.ServicesConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private static boolean startSqsService(ApiFunctions functions, String name) {
        //create callback consumer which processes commands on the queue
        Consumer<String> callback = (str) -> {
            // we will process the strings from toProcess and put them
            // into found
            List<String> toProcess = new ArrayList<>(Arrays.asList(str.split(" ")));
            ArrayList<String> found = new ArrayList<>();
            
            // prime our algorithm by putting the first element to
            // process into found
            if (!toProcess.isEmpty()) {
                found.add(toProcess.remove(0));
            }
            
            while (!toProcess.isEmpty()) {
                // if the last found element starts with a quote and
                // doesn't end with one, just append the next element
                // as we are still inside a quote region othwerwise
                // just add the element
                if (found.get(found.size() - 1).startsWith("\"") &&
                        !found.get(found.size() - 1).endsWith("\"")) {
                    found.set(
                            found.size() - 1,
                            found.get(found.size() - 1) + " " + toProcess.remove(0));
                } else {
                    found.add(toProcess.remove(0));
                }
            }
            
            // trim open and closed quotes
            for (int i = 0; i < found.size(); i++) {
                String toProc = found.get(i);
                if (toProc.startsWith("\"") && toProc.endsWith("\"")) {
                    found.set(i, toProc.substring(1, toProc.length() - 1));
                }
                
                LOGGER.info(Integer.toString(i) + ": " + toProc + " -> " + found.get(i));
            }
            
            String[] foundArgs = found.toArray(new String[0]);
            LOGGER.info("Got args: " + Arrays.toString(foundArgs));
            
            // execute command
            boolean result = CommandLine.processStandardCommands(
                    foundArgs,
                    functions);
            
            if (result) {
                LOGGER.info("Execution succeeded.");
            } else {
                LOGGER.info("Execution failed.");
            }
        };
        
        // create the SQS interface
        AmazonSimpleQueueService sqs = new AmazonSimpleQueueService(
                functions,
                callback,
                name);
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
