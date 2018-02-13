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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;

/**
 * Provides a target which processes SQS messages.
 */
public class SQSTarget {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * Processes the command from the SQS queue.
     * @param cmd The command to process.
     * @param functions The ApiFunctions instance to use when processing the
     * commands.
     * @return The result of the command.
     */
    public static String parseCommand(String cmd, ApiFunctions functions) {
        LOGGER.info("Processing SQS message: " + cmd);
        
        if (cmd.toUpperCase().startsWith("CAPTUREGRAPHDATACSV")) {
            if (cmd.length() != 56) {
                return "Capture summary command must be of form: \"capturesummarycsv [capture uuid]\".";
            }
            
            UUID capture = UUID.fromString(cmd.substring(20));
            
            try {
                return functions.calculageGraphDataCsv(capture, 400);
            } catch (Exception ex) {
                LOGGER.error("Exception while calculating graph data for capture.", ex);
                return "Could not create graph data for capture.";
            }
            
        } else if (cmd.toUpperCase().startsWith("CAPTUREGRAPHDATA")) {
            if (cmd.length() != 53) {
                return "Capture summary command must be of form: \"capturesummary [capture uuid]\".";
            }
            
            UUID capture = UUID.fromString(cmd.substring(17));
            
            try {
                return functions.calculageGraphData(capture, 400);
            } catch (Exception ex) {
                LOGGER.error("Exception while calculating graph data for capture.", ex);
                return "Could not create graph data for capture.";
            }
            
        } else if (cmd.toUpperCase().startsWith("CAPTURESUMMARY")) {
            if (cmd.length() != 51) {
                return "Capture summary command must be of form: \"capturesummary [capture uuid]\".";
            }
            
            UUID toDelete = UUID.fromString(cmd.substring(15));
            
            try {
                return functions.summarizeCapture(toDelete);
            } catch (Exception ex) {
                LOGGER.error("Exception while summarizing capture.", ex);
                return "Could not summarize capture.";
            }
        } else if (cmd.toUpperCase().startsWith("DELETE")) {
            if (cmd.length() != 43) {
                return "Delete command must be of form: \"delete [capture uuid]\".";
            }
            
            UUID toDelete = UUID.fromString(cmd.substring(7));
            
            try {
                return functions.deleteCapture(toDelete);
            } catch (Exception ex) {
                LOGGER.error("Exception while deleting capture.", ex);
                return "Could not delete capture.";
            }
        } else if (cmd.toUpperCase().startsWith("EXPORTRAW")) {
            if (cmd.length() < 48) {
                return "Export raw command must be of form: \"exportraw [capture uuid] [outputbucket]\".";
            }
            
            String[] splitCmd = cmd.split(" ");
            
            if (splitCmd.length < 3) {
                return "Export raw command must be of form: \"exportraw [capture uuid] [outputbucket]\".";
            }
            
            UUID toProcess = UUID.fromString(splitCmd[1]);
            
            try {
                return functions.exportRaw(toProcess, splitCmd[2]);
            } catch (Exception ex) {
                LOGGER.error("Exception while exporting raw capture data.", ex);
                return "Could not export raw capture data.";
            }
        } else if (cmd.toUpperCase().startsWith("EXPORTCAL")) {
            if (cmd.length() < 48) {
                return "Export cal command must be of form: \"exportcal [capture uuid] [outputbucket]\".";
            }
            
            String[] splitCmd = cmd.split(" ");
            
            if (splitCmd.length < 3) {
                return "Export cal command must be of form: \"exportcal [capture uuid] [outputbucket]\".";
            }
            
            UUID toProcess = UUID.fromString(splitCmd[1]);
            
            try {
                return functions.exportCal(toProcess, splitCmd[2]);
            } catch (Exception ex) {
                LOGGER.error("Exception while exporting cal capture data.", ex);
                return "Could not export cal capture data.";
            }
        } else {
        
            // process command
            try {
                // we will process the strings from toProcess and put them
                // into found
                List<String> toProcess = new ArrayList<>(Arrays.asList(cmd.split(" ")));
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
                    return "Execution succeeded.";
                } else {
                    return "Execution failed.";
                }
            } catch (Exception ex) {
                LOGGER.error("Unexpected exception processing SQS message: " + cmd, ex);
                return "Execution failed.";
            }
        }
    }
}
