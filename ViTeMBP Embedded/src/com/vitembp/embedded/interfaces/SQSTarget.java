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
import com.vitembp.embedded.controller.SignalCalibrateNextStep;
import com.vitembp.embedded.controller.SignalCalibrateSensor;
import com.vitembp.embedded.controller.SignalEndCapture;
import com.vitembp.embedded.controller.SignalGetCalibrationStatus;
import com.vitembp.embedded.controller.SignalStartCapture;
import com.vitembp.embedded.controller.StateMachine;
import com.vitembp.embedded.hardware.HardwareInterface;
import com.vitembp.embedded.hardware.Sensor;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
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
     * @return The result of processing the message.
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
            // send start command
            LinkedBlockingQueue<String> result = new LinkedBlockingQueue<>();
            StateMachine.getSingleton().enqueueSignal(new SignalStartCapture(result::add));
            try {
                return result.take();
            } catch (InterruptedException ex) {
                failureReason = "Interrupted waiting for result of start capture command.";
                LOGGER.error(failureReason, ex);
            }
        } else if ("ENDCAPTURE".equals(upperCase)) {
            // send the signal to end capture
            LinkedBlockingQueue<String> result = new LinkedBlockingQueue<>();
            StateMachine.getSingleton().enqueueSignal(new SignalEndCapture(result::add));
            try {
                return result.take();
            } catch (InterruptedException ex) {
                failureReason = "Interrupted waiting for result of end capture command.";
                LOGGER.error(failureReason, ex);
            }
        } else if ("LISTSENSORS".equals(upperCase)) {
            StringBuilder toReturn = new StringBuilder();
            
            // start JSON array
            toReturn.append("[");
            
            // add names
            Map<String, Sensor> toAdd = HardwareInterface.getInterface().getSensors();
            toAdd.keySet().forEach((name) -> {
                if (toAdd.get(name) != null) {
                    toReturn.append("\"");
                    toReturn.append(name);
                    toReturn.append("\",");
                }
            });
            
            // replace last comma with a close array
            toReturn.setCharAt(toReturn.length() - 1, ']');
            return toReturn.toString();
        } else if (upperCase.startsWith("CALSENSOR")) {
            // command must be of the form: "calsensor [sensor name]"
            if (toProcess.length() < 10) {
                failureReason = "Sensor name required.";
            } else {
                String sensorName = toProcess.substring(10);
                // send start calibration command
                LinkedBlockingQueue<String> result = new LinkedBlockingQueue<>();
                StateMachine.getSingleton().enqueueSignal(new SignalCalibrateSensor(sensorName, result::add));
                try {
                    return result.take();
                } catch (InterruptedException ex) {
                    failureReason = "Interrupted waiting for result of start calibration command.";
                    LOGGER.error(failureReason, ex);
                }
            }
        } else if ("CALSTATUS".equals(upperCase)) {
            // send cas status command
            LinkedBlockingQueue<String> result = new LinkedBlockingQueue<>();
            StateMachine.getSingleton().enqueueSignal(new SignalGetCalibrationStatus(result::add));
            try {
                return result.take();
            } catch (InterruptedException ex) {
                failureReason = "Interrupted waiting for result of get calibration status command.";
                LOGGER.error(failureReason, ex);
            }
        } else if ("CALNEXTSTEP".equals(upperCase)) {
            // send cas status command
            LinkedBlockingQueue<String> result = new LinkedBlockingQueue<>();
            StateMachine.getSingleton().enqueueSignal(new SignalCalibrateNextStep(result::add));
            try {
                return result.take();
            } catch (InterruptedException ex) {
                failureReason = "Interrupted waiting for result of next calibration step command.";
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
