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

import java.nio.file.Path;

/**
 * This class provides access to system specific properties.
 */
public final class SystemInfo {
    /**
     * Prevents instantiation of this class.
     */
    private SystemInfo() {}
    
    /**
     * Gets a path object indicating the directory to read and write config
     * files from.
     * @return A path object indicating the directory to read and write config
     * files from.
     */
    public static Path getConfigDirectory() {
        return SystemBoard.getBoard().getConfigDirectory();
    }
    
    /**
     * Gets a path object indicating the directory to read and write log
     * files from.
     * @return A path object indicating the directory to read and write log
     * files from.
     */
    public static Path getLogDirectory() {
        return SystemBoard.getBoard().getLogDirectory();
    }
}
