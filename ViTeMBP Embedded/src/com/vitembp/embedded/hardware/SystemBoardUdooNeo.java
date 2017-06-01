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
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * A SystemBoard implementation for the UDOO NEO.
 */
class SystemBoardUdooNeo extends SystemBoard{
    /**
     * The GPIO ports available for this system board.
     */
    private final Set<GPIOPort> gpioPorts;
    
    SystemBoardUdooNeo() throws IOException {
        // builds GPIO port interfaces
        this.gpioPorts = GPIOPortFile.buildPortsForPath(Paths.get("/sys/class/gpio"));
    }
            
    @Override
    public Set<I2CBus> getI2CBusses() {
        return new HashSet<>();
    }

    @Override
    public Set<GPIOPort> getGPIOPorts() {
        return this.gpioPorts;
    }
    
}
