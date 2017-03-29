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
package com.vitembp.services;

import com.vitembp.services.interfaces.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Entry point class for the ViTeMBP project back-end services.
 * @author Kyle
 */
public class ViTeMBPServices {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Entry point for the ViTeMBP project.
     * @param args the command line arguments
     */
    public static void main(String[] args) {       
        LOGGER.info("ViTeMBPServies starting.");
        CommandLine.acceptArgs(args);
        LOGGER.info("ViTeMBPServies exiting.");
    }
}
