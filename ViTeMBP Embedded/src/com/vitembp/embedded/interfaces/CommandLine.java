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

import com.vitembp.embedded.hardware.Platform;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A user interface which provides command line functionality.
 */
public class CommandLine {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Accepts and appropriately displays command line arguments.
     * @param args The command line arguments passed to the 
     * @param platform The platform to provide an interface for.
     */
    public static void acceptArgs(String[] args, Platform platform) {
        // log call
        LOGGER.info("Processing command: " + Arrays.toString(args));
        
        // if there were any arguments passed to the jvm
        if (args.length > 0) {
            if (processServerCommands(args, platform)) {
                return;
            }
        }
        
        // print usage as a valid command was not found
        printUsage();
    }

    private static boolean processServerCommands(String[] args, Platform platform) {
        if (args[0].toUpperCase().equals("-HTTP")) {
            // command: -sqs <queue_name>
            if (args.length >= 2) {
                try {
                    // get port to host server on
                    int port = Integer.parseInt(args[1]);
                    
                    System.out.println("Starting HTTP server.");
                    Http server = new Http(8080, platform);
                } catch (IOException ex) {
                    LOGGER.error("Exception starting web server.", ex);
                } catch (NumberFormatException ex) {
                    LOGGER.error("Invalid port number \"" + args[1] + "\" starting web server.", ex);
                    return false;
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Prints the command line usage instructions to the system standard out.
     */
    private static void printUsage(){
        try {
            // get the usage text to output to the standard out
            InputStream usageText =
                    CommandLine.class.getResourceAsStream("CmdLineUsage.txt");
            
            // write output to console
            int inputData;
            while ((inputData = usageText.read()) != -1) {
                System.out.write(inputData);
            }
        } catch (IOException ex) {
            LOGGER.error("Could not emit command line usage.", ex);
        }
    }
}
