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
 * A class containing the data needed for an I2C data transaction.
 */
class I2CBusTransaction {
    /**
     * The address of the device on the bus.
     */
    private final int deviceAddress;
    
    /**
     * The bytes to write to the device.
     */
    private final int[] toWrite;
    
    /**
     * The bytes to read from the device.
     */
    private final int bytesToRead;
    
    /**
     * Initializes a new instance of the I2CBusTransaction.
     * @param deviceAddress The address of the device on the bus.
     * @param toWrite The bytes to write to the device.
     * @param bytesToRead The bytes to read from the device.
     */
    public I2CBusTransaction(int deviceAddress, int[] toWrite, int bytesToRead) {
        this.deviceAddress = deviceAddress;
        this.toWrite = toWrite;
        this.bytesToRead = bytesToRead;
    }
    
    /**
     * Gets the address of the device on the bus to communicate with.
     * @return The address of the device on the bus to communicate with.
     */
    public int getDeviceAddress() {
        return this.deviceAddress;
    }
    
    /**
     * Gets the bytes to write to the device.
     * @return The bytes to write to the device.
     */
    public int[] getBytesToWrite() {
        return this.toWrite;
    }
    
    /**
     * Gets the bytes to read from the device.
     * @return The bytes to read from the device.
     */
    public int getBytesToRead() {
        return this.bytesToRead;
    }
}
