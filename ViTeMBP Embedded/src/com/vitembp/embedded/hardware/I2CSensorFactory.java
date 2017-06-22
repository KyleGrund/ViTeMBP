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

import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;

/**
 * This class creates Sensor instances for devices on an I2C bus.
 */
class I2CSensorFactory {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * Gets a set of devices built for the given I2C bus.
     * @param bus The bus to build devices for.
     * @return Set of devices built for the given I2C bus.
     */
    public static Set<Sensor> getI2CSensors(I2CBus bus) {
        Set<Sensor> found = new HashSet<>();
        // go through all devices on the bus
        for (I2CDevice dev : bus.getDevices()) {
            LOGGER.info("Enumerating bus: " + bus.getName());
            
            // create sensor interface objects based on device addresses
            if (dev.getAddress() == 32) {
                LOGGER.info("Adding AccelerometerFXOS8700CQ at address 32.");
                found.add(new AccelerometerFXOS8700CQ(bus.getName() + ":" + dev.getAddress(), dev));
            }
        }   
        
        return found;
    }
}
