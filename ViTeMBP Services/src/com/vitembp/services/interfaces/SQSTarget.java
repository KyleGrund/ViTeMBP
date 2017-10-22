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
     * @return The result of the command.
     */
    public static String parseCommand(String cmd, ApiFunctions functions) {
        LOGGER.info("Processing SQS message: " + cmd);
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
