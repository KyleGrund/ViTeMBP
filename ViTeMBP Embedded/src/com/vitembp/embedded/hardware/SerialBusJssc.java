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
import java.nio.file.Path;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.apache.logging.log4j.LogManager;

/**
 * SerialBus implementation using the jssc library.
 */
class SerialBusJssc extends SerialBus {
    /**
     * The control object for the serial port under control.
     */
    SerialPort port;
    
    /**
     * Initializes a new instance of the SerialBusJssc class.
     * @param file The path to the serial port.
     */
    SerialBusJssc(Path file) throws IOException {
        this.port = new SerialPort(file.toString());
        try {
            this.port.openPort();
        } catch (SerialPortException ex) {
            throw new IOException("Could not open serial port for path: " + file.toString(), ex);
        }
    }
    
    @Override
    void writeBytes(byte[] toWrite) throws IOException {
        try {
            port.writeBytes(toWrite);
        } catch (SerialPortException ex) {
            throw new IOException("Error writing to serial port: " + this.port.getPortName(), ex);
        }
    }

    @Override
    byte[] readBytes(int len) throws IOException {
        try {
            return port.readBytes(len);
        } catch (SerialPortException ex) {
            throw new IOException("Error writing to serial port: " + this.port.getPortName(), ex);
        }
    }

    @Override
    String getName() {
        return this.port.getPortName();
    }
}
