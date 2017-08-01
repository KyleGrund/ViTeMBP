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
import java.util.UUID;
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
            System.out.println("Starting HTTP server.");
            try {
                Http server = new Http(8080, functions);
            } catch (IOException ex) {
                LOGGER.error("Exception starting web server.", ex);
            }
            return true;
        } else if (args[0].toUpperCase().equals("-SQS")) {
            // command: -sqs <queue_name>
            if (args.length >= 2) {
                // get the queue name
                String name = args[1];
                
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
                    boolean result = processStandardCommands(
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
        }
        return false;
    }

    private static boolean processStandardCommands(String[] args, ApiFunctions functions) {
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
            if (args.length >= 2) {
                // gets the input video file name
                UUID captureLocation = UUID.fromString(args[1]);
                
                // gets the input video file name
                String videoFile = (args[2]);
                
                try {
                    // process the video
                    functions.processCaptureVideo(captureLocation, videoFile);
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
