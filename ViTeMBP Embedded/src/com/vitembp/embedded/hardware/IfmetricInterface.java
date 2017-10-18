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
package com.vitembp.embedded.hardware;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;

/**
 * Provides an interface to the ifmetric system utility.
 */
class IfmetricInterface {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * Sets the interface metric.
     * @param iface The interface to set.
     * @param metric The metric to set the interface to.
     */
    static void setInterfaceMetric(String iface, int metric) throws IOException {
        LOGGER.info("Setting interface \"" + iface + "\" metric to: " + Integer.toString(metric) + ".");
        
        // run command: "ifconfig [interface] [metric]"
        List<String> processArgs = new ArrayList<>();
        processArgs.add("ifmetric");
        processArgs.add(iface);
        processArgs.add(Integer.toString(metric));
        
        ProcessBuilder pb = new ProcessBuilder(processArgs);
        
        LOGGER.debug("Executing command: " + Arrays.toString(pb.command().toArray()));
        
        // execute the command
        Process proc = pb.start();
        
        try {
            // execute and wait for the command
            int result = proc.waitFor();
            if (result != 0) {
                // result is exit level, log anything > 0 as an error
                LOGGER.error("The ifmetric command exited with level: " + Integer.toString(result));
                BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                String line = br.readLine();
                while (line != null) {
                    LOGGER.error(line);
                    line = br.readLine();
                }
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted while waiting for ifmetric completion.", ex);
        }
    }
}
