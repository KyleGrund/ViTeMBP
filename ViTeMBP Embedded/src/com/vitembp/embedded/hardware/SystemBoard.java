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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;

/**
 * Class providing an interface to embedded system boards.
 */
abstract class SystemBoard {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * Lock object for singleton creation critical section.
     */
    private static final Object SINGLETON_CREATE_LOCK = new Object();
    
    /**
     * The system board singleton.
     */
    private static SystemBoard singletonInstance = null;
    
    /**
     * Gets the available I2C system busses.
     * @return The available I2C system busses.
     */
    public abstract Set<I2CBus> getI2CBusses();
    
    /**
     * Gets the available system board GPIO ports.
     * @return The available system board GPIO ports.
     */
    public abstract Set<GPIOPort> getGPIOPorts();
    
    /**
     * Detects the current board the system is operating on and creates the
     * appropriate singleton instance.
     * @return The board instance for the system that the program is currently
     * executing on. If the system is executing on an unknown board a mock
     * simulation will be returned.
     */
    public static SystemBoard getBoard() {
        synchronized (SystemBoard.SINGLETON_CREATE_LOCK) {
            // create the singleton instance if it doesn't already exist
            if (SystemBoard.singletonInstance == null) {
                // linux distrobutions have a version file used to access distro info
                Path versionFile = Paths.get("/proc/version");

                // if the version file contains udooneo we are running on a UDOO NEO board
                if (versionFile.toFile().exists()) {
                    try {
                        List<String> versionLines = Files.readAllLines(versionFile);
                        if (versionLines.size() > 0) {
                            if (versionLines.get(0).contains("udooneo")) {
                                // return a udoo neo system board instance
                                SystemBoard.singletonInstance = new SystemBoardUdooNeo();
                            }
                        }
                    } catch (IOException ex) {
                        LOGGER.error("Version file exists but could not be read.", ex);
                    }
                }

                // no system board was able to be made, so return a mock
                if (SystemBoard.singletonInstance == null) {
                    SystemBoard.singletonInstance = new SystemBoardMock();
                }
            }
        }
        
        // return the singleton instance
        return SystemBoard.singletonInstance;
    }
}
