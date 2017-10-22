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

import com.vitembp.embedded.configuration.CloudConfigSync;
import com.vitembp.embedded.hardware.HardwareInterface;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides a target for processing SQS messages.
 */
public class SQSTarget {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Parses a message sent through the FROMUUID command.
     * @param toProcess The message to process.
     */
    public static String parseUuidMessage(String toProcess) {
        LOGGER.info("Processing uuid message: " + toProcess);
        String failureReason = null;
        
        // process message
        String upperCase = toProcess.toUpperCase();
        if ("FROMUUID".startsWith(upperCase)) {
            failureReason = "Cannot nest FROMUUID messages.";
            LOGGER.error(failureReason);
        } else if ("REBOOT".equals(upperCase)) {
            try {
                HardwareInterface.getInterface().restartSystem();
                return "Success.";
            } catch (IOException ex) {
                failureReason = "Error processing reboot command.";
                LOGGER.error(failureReason, ex);
            }
        } else if ("SHUTDOWN".equals(upperCase)) {
            try {
                HardwareInterface.getInterface().shutDownSystem();
                return "Success.";
            } catch (IOException ex) {
                failureReason = "Error processing reboot command.";
                LOGGER.error(failureReason, ex);
            }
        } else if ("UPDATECONFIG".equals(upperCase)) {
            // trigger the cloud configuration service to check for updates
            CloudConfigSync.checkForUpdates();
            return "Success.";
        } else if ("STARTCAPTURE".equals(upperCase)) {
            try {
                // send the keypress '1' to signal start capture
                HardwareInterface.getInterface().generateKeyPress('1');
                return "Sent start capture signal.";
            } catch (InterruptedException ex) {
                failureReason = "Interrupted sending start capture keypress.";
                LOGGER.error(failureReason, ex);
            }
        } else if ("ENDCAPTURE".equals(upperCase)) {
            try {
                // send the keypress '4' to signal start capture
                HardwareInterface.getInterface().generateKeyPress('4');
                return "Sent end capture signal.";
            } catch (InterruptedException ex) {
                failureReason = "Interrupted sending end capture keypress.";
                LOGGER.error(failureReason, ex);
            }
        }
        
        // return as specific a failure messsage as possible
        if (failureReason != null) {
            return "Failed: " + failureReason;
        } else {
            return "Failed.";
        }
    }
}
