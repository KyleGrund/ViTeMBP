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
import com.vitembp.services.config.ServicesConfig;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
     * @param functions The functionality to provide an interface for.
     */
    public static void acceptArgs(String[] args, ApiFunctions functions) {
        // log call
        LOGGER.info("Processing command: " + Arrays.toString(args));
        
        // if there were any arguments passed to the jvm
        if (args.length > 0) {
            if (processServerCommands(args, functions)) {
                return;
            } else if (processStandardCommands(args, functions)) {
                return;
            } 
        }
        
        // print usage as a valid command was not found
        printUsage();
    }

    private static boolean processServerCommands(String[] args, ApiFunctions functions) {
        if (args[0].toUpperCase().equals("-HS")) {
            if (args.length >= 2) {
                // get the queue name
                int port = Integer.parseInt(args[1]);
                
                // set the service config to match command line args
                ServicesConfig.getConfig().setEnableHttpInterface(true);
                ServicesConfig.getConfig().setHttpInterfacePort(port);
            }
        } else if (args[0].toUpperCase().equals("-SQS")) {
            // command: -sqs <queue_name>
            if (args.length >= 2) {
                // get the queue name
                String name = args[1];
                
                // set the service config to match command line args
                ServicesConfig.getConfig().setEnableSqsInterface(true);
                ServicesConfig.getConfig().setSqsQueueName(name);
            }
        }
        return false;
    }

    public static boolean processStandardCommands(String[] args, ApiFunctions functions) {
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
                        return true;
                    }
                }
                // valid command was parsed, try to execute it
                List<Integer> frames = new ArrayList<>();
                try {
                    frames = functions.findChannelSyncFrames(
                            videoFile.toString(),
                            chan);
                } catch (IOException ex) {
                    LOGGER.error("Exception finding sync frames.", ex);
                }
                // display results to console
                System.out.println("Frames: " + Arrays.toString(frames.toArray()));
                return true;
            }
        } else if (args[0].toUpperCase().equals("-FSD")) {
            // command: -fsd <filename> <outputFilename> [<color channel>]
            if (args.length >= 3) {
                // gets the input video file name
                Path videoFile = Paths.get(args[1]);
                
                // gets the input video file name
                Path outputFile = Paths.get(args[2]);
                
                // default channel to process to green
                COLOR_CHANNELS chan = COLOR_CHANNELS.GREEN;
                // parse color channel name if it is specified
                if (args.length >=4) {
                    try {
                        chan = COLOR_CHANNELS.valueOf(args[3].toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        // name is not valid
                        LOGGER.info("Invalid color channel specified.", ex);
                        printUsage();
                        return true;
                    }
                }
                // valid command was parsed, try to execute it
                List<Integer> frames = new ArrayList<>();
                try {
                    frames = functions.findChannelSyncFramesDiag(
                            videoFile.toString(),
                            chan,
                            outputFile);
                } catch (IOException ex) {
                    LOGGER.error("Exception finding sync frames.", ex);
                }
                // display results to console
                System.out.println("Frames: " + Arrays.toString(frames.toArray()));
                return true;
            }
        } else if (args[0].toUpperCase().equals("-PV")) {
            // command: -pv <uuid> <videoLocation>
            if (args.length >= 4) {
                // gets the input video file name
                UUID captureLocation = UUID.fromString(args[1]);
                
                // gets the input video file name
                String videoFile = args[2];
                
                // gets the destination bucket to put the processed video
                String destinationBucket = args[3];
                
                // gets the destination filename to put the processed video in
                // the target bucket
                String destinationFilename = args[4];
                
                try {
                    // process the video
                    functions.processCaptureVideo(captureLocation, videoFile, destinationBucket, destinationFilename);
                } catch (IOException ex) {
                    LOGGER.error("IOException processing video for capture.", ex);
                }

                // no exceptions, return success
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
