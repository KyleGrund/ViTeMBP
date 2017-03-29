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
import com.vitembp.services.ApiFunctions.COLOR_CHANNELS;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
     */
    public static void acceptArgs(String[] args) {
        // log call
        LOGGER.info("Processing command: " + Arrays.toString(args));
        
        // if there were any arguments passed to the jvm
        if (args.length > 0) {
            if (args[0].toUpperCase().equals("-FS")) {
                // command: -fs <filename> [<color channel>]
                if (args.length >= 2) {
                    // gets the video file name
                    Path videoFile = Paths.get(args[1]);
                    
                    // default channel to process to green
                    COLOR_CHANNELS chan = COLOR_CHANNELS.GREEN;

                    // parse color channel name if it is specified
                    if (args.length >=3) {
                        try {
                            chan = COLOR_CHANNELS.valueOf(args[2].toUpperCase());
                        } catch (IllegalArgumentException ex) {
                            // name is not valid
                            LOGGER.info("Invalid color channel specified.", ex);
                            printUsage();
                            return;
                        }
                    }
                    
                    // valid command was parsed, try to execute it
                    List<Integer> frames = new ArrayList<>();
                    try {
                        frames = ApiFunctions.findChannelSyncFrames(
                                videoFile,
                                chan);
                    } catch (IOException ex) {
                        LOGGER.error("Exception finding sync frames.", ex);
                    }
                    
                    // display results to console
                    System.out.println("Frames: " + Arrays.toString(frames.toArray()));
                    
                    return;
                }
            }
        }
        
        // print usage as a valid command was not found
        printUsage();
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
