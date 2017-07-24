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

/**
 * Provides a uniform interface to serial busses.
 */
abstract class SerialBus {
    /**
     * Writes bytes to a serial interface.
     * @param toWrite An array of bytes to write.
     * @throws IOException If there is an IO error writing to the port.
     */
    abstract void writeBytes(byte[] toWrite) throws IOException;
    
    /**
     * Reads bytes from a port.
     * @param len The number of bytes to read.
     * @return The array of bytes read from the port.
     * @throws IOException If there is an IO error reading from the port.
     */
    abstract byte[] readBytes(int len) throws IOException;
    
    /**
     * Gets the system unique name of the bus.
     * @return The system unique name of the bus.
     */
    abstract String getName();
}
