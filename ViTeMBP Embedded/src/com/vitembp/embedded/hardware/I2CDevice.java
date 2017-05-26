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

/**
 * Base class providing uniform interface for an I2C device.
 */
public class I2CDevice {
    /**
     * The bus to communicate over.
     */
    private final I2CBus bus;
    
    /**
     * The address of the device on the bus.
     */
    private final int deviceAddress;
    
    /**
     * Initializes a new instance of the I2CDevice class.
     * @param deviceAddress The address of the device on the bus.
     * @param bus The bus the device is connected to.
     */
    I2CDevice(int deviceAddress, I2CBus bus) {
        this.bus = bus;
        this.deviceAddress = deviceAddress;
    }
    
    /**
     * Reads the specified number of bytes from the device.
     * @param readCount The number of bytes to read from the device.
     * @return The bytes read from the device.
     */
    public byte[] read(int readCount) {
        return this.bus.read(this.deviceAddress, readCount);
    }
        
    /**
     * Writes the specified bytes to the device.
     * @param toWrite The bytes to write to the device.
     */
    public void write(byte[] toWrite) {
        this.bus.write(this.deviceAddress, toWrite);
    }
    
    /**
     * Writes bytes to and then reads bytes from the device.
     * @param toWrite The bytes to write.
     * @param readCount The number of bytes to read.
     * @return The bytes read from the device.
     */
    public byte[] writeRead(byte[] toWrite, int readCount) {
        return this.bus.writeRead(this.deviceAddress, toWrite, readCount);
    }
}
