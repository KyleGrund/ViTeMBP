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
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.Charsets;

/**
 * This class creates sensor objects for serial busses.
 */
class SerialBusSensorFactory {

    /**
     * Builds sensor instances for serial a bus.
     * @param bus The bus to build sensor objects for.
     * @return The set of sensors connected to the bus.
     */
    static Set<Sensor> getSerialSensors(SerialBus bus) throws IOException {
        try {
            HashSet<Sensor> toReturn = new HashSet<>();
            
            // query serial bus for sensor information
            bus.writeBytes(new byte[] { 'i' });
            byte[] respBytes = bus.readBytes(36);
            String resp = new String(respBytes, Charsets.UTF_8);
            
            if ("3972d3a9-d55f-4e74-a61f-f2f8fe62f858".equals(resp)) {
                toReturn.add(new DistanceVL53L0X(bus));
            } else if ("fe3c4af2-feb4-4c9b-a717-2d0db3052293".equals(resp)) {
                toReturn.add(new AccelerometerFXOS8700CQSerial(bus));
            } else if ("75d05ba8-639c-46e6-a940-591d920a2d86".equals(resp)) {
                toReturn.add(new RotaryEncoderEAW0J(bus));
            }
            
            return toReturn;
        } catch (IOException ex) {
            throw new IOException("Error enumerating bus: " + bus.getName(), ex);
        }
    }
    
}
