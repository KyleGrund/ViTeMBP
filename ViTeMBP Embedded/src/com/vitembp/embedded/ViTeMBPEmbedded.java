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
package com.vitembp.embedded;

import com.vitembp.embedded.configuration.SystemConfig;
import com.vitembp.embedded.controller.StateMachine;
import com.vitembp.embedded.gui800x480.GUI;
import com.vitembp.embedded.interfaces.AmazonSQSControl;
import com.vitembp.embedded.interfaces.CommandLine;
import com.vitembp.embedded.interfaces.SQSTarget;
import org.apache.logging.log4j.LogManager;

/**
 * Class containing the main entry point for program.
 */
public class ViTeMBPEmbedded {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // process command line arguments
        CommandLine.acceptArgs(args);
        
        // create system controller state machine
        StateMachine machine = new StateMachine();
        
        // start GUI
        GUI.start(machine::setSensorsChangedCallback, machine::setSensorsReadCallback);
        
        // start state machine
        machine.start();
        
        // start sqs message processor
        String queueName = "ViTeMBP-Device-" + SystemConfig.getConfig().getSystemUUID().toString();
        new AmazonSQSControl(queueName, SQSTarget::parseUuidMessage, 1).start();
    }
}