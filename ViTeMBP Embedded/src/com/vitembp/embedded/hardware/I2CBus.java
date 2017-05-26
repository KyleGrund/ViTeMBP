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
 * Base class providing uniform interface for an I2C interface.
 */
abstract class I2CBus {
    /**
     * Reads the specified number of bytes from the device.
     * @param address The address of the device to read from.
     * @param readCount The number of bytes to read from the device.
     * @return The bytes read from the device.
     */
    public abstract byte[] read(int address, int readCount);
        
    /**
     * Writes the specified bytes to the device.
     * @param address The address of the device to write to.
     * @param toWrite The bytes to write to the device.
     */
    public abstract void write(int address, byte[] toWrite);
    
    /**
     * Writes bytes to and then reads bytes from the device.
     * @param address The address of the device to write to and read from.
     * @param toWrite The bytes to write.
     * @param readCount The number of bytes to read.
     * @return The bytes read from the device.
     */
    public abstract byte[] writeRead(int address, byte[] toWrite, int readCount);
    
    /**
     * Gets an I2CDevice object for all devices which can be enumerated on this
     * bus.
     * @return I2CDevice objects for all enumerated devices.
     */
    public abstract Iterable<I2CDevice> getDevices();
}
