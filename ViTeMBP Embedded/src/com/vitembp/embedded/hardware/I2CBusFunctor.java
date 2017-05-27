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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A class that implements the I2CBus using a functor style.
 */
class I2CBusFunctor extends I2CBus {
    /**
     * The callback providing access to the I2C bus.
     */
    private final Function<I2CBusTransaction, byte[]> busCallback;
    
    /**
     * The callback which will list enumerable I2C devices.
     */
    private final Supplier<List<Integer>> deviceIDCallback;
    
    /**
     * A map of integer bus IDs to I2CDevice control objects.
     */
    private final Map<Integer, I2CDevice> devices = new HashMap<>();
    
    /**
     * Initializes a new instance of the I2CBusFunctor class.
     * @param busCallback 
     */
    I2CBusFunctor(Function<I2CBusTransaction, byte[]> busCallback, Supplier<List<Integer>> deviceIDCallback) {
        this.busCallback = busCallback;
        this.deviceIDCallback = deviceIDCallback;
    }
    
    @Override
    public byte[] read(int address, int readCount) {
        return busCallback.apply(new I2CBusTransaction(address, new byte[] { }, readCount));
    }

    @Override
    public void write(int address, byte[] toWrite) {
        busCallback.apply(new I2CBusTransaction(address, new byte[] { }, 0));
    }

    @Override
    public byte[] writeRead(int address, byte[] toWrite, int readCount) {
        return busCallback.apply(new I2CBusTransaction(address, toWrite, readCount));
    }

    @Override
    public Iterable<I2CDevice> getDevices() {
        List<I2CDevice> toReturn = new ArrayList<>();
        
        List<Integer> deviceIDs = this.deviceIDCallback.get();
        
        // make new device control objects as needed
        for (int dev : this.deviceIDCallback.get()) {
            if (!this.devices.containsKey(dev)) {
                toReturn.add(new I2CDevice(dev, this));
            }
        }
        
        // remove any device control objects as needed
        List<Integer> toRemove = new ArrayList<>();
        for (int dev : this.devices.keySet()) {
            if (!deviceIDs.contains(dev)) {
                toRemove.add(dev);
            }
        }
        
        // remove any devices that were found to no longer be on the bus
        toRemove.stream().forEach((dev) -> { this.devices.remove(dev); });
        
        return toReturn;
    }
}
