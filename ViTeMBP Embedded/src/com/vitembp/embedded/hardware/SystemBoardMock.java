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
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * A mock system bus to be used when not running on a compatible system board.
 */
class SystemBoardMock extends SystemBoard {

    @Override
    public Set<I2CBus> getI2CBusses() {
        return new HashSet<>();
    }

    @Override
    public Set<GPIOPort> getGPIOPorts() {
        return new HashSet<>();
    }

    @Override
    public Path getConfigDirectory() {
        return Paths.get("");
    }

    @Override
    public Path getLogDirectory() {
        return Paths.get("");
    }
    
}
