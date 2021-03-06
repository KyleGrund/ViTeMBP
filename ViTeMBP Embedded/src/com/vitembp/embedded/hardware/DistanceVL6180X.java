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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;

/**
 * Provides an interface for the VL6180X sensor.
 */
public class DistanceVL6180X extends Sensor {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

    /**
     * A UUID representing the un-initialized eeprom data.
     */
    private static final UUID DEFAULT_UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
    
    /**
     * A UUID representing the type of this sensor.
     */
    public static final UUID TYPE_UUID = UUID.fromString("416ffd9b-67ac-4cb3-9c04-30ba1f8640ca");
    
    /**
     * The bus to communicate with the sensors on.
     */
    private final SerialBus bus;
    
    /**
     * Initializes a new instance of the DistanceVL53L0X class.
     * @param bus The bus this sensor is connected to.
     */
    DistanceVL6180X(SerialBus bus) {
        this.bus = bus;
    }

    @Override
    public UUID getType() {
        return TYPE_UUID;
    }

    @Override
    public void initialize() {
    }

    @Override
    public String readSample() {
        try {
            this.bus.writeBytes(new byte[] { 'r' });
            byte[] resp = this.bus.readBytes(1);
            
            return Integer.toString(resp[0] & 0xff);
        } catch (IOException ex) {
            LOGGER.error("Error reading sample from VL6180X " + this.bus.getName(), ex);
            return null;
        }
    }

    @Override
    public UUID getSerial() {
        // default the serial to un-initialized value in case read fails
        UUID serial = DEFAULT_UUID;
        
        // try to read in the sensor's serial
        try {
            this.bus.writeBytes(new byte[]{ 's' });
            byte[] resp = this.bus.readBytes(16);
            serial = UUID.nameUUIDFromBytes(resp);
        } catch (IOException ex) {
            LOGGER.error("Error reading serial from sensor at: " + bus.getName(), ex);
        }
        
        // if it is still the default value, initialize it to a random uuid
        if (DEFAULT_UUID.equals(serial)) {
            serial = UUID.randomUUID();
            long msBits = serial.getMostSignificantBits();
            long lsBits = serial.getLeastSignificantBits();
            byte[] toWrite = new byte[17];
            
            // the write to eeprom command
            toWrite[0] = (byte)'e';
            
            // decode the uuid bytes
            toWrite[1] = (byte)lsBits;
            toWrite[2] = (byte)(lsBits >> 8);
            toWrite[3] = (byte)(lsBits >> 16);
            toWrite[4] = (byte)(lsBits >> 24);
            toWrite[5] = (byte)(lsBits >> 32);
            toWrite[6] = (byte)(lsBits >> 40);
            toWrite[7] = (byte)(lsBits >> 48);
            toWrite[8] = (byte)(lsBits >> 56);
            toWrite[9] = (byte)msBits;
            toWrite[10] = (byte)(msBits >> 8);
            toWrite[11] = (byte)(msBits >> 16);
            toWrite[12] = (byte)(msBits >> 24);
            toWrite[13] = (byte)(msBits >> 32);
            toWrite[14] = (byte)(msBits >> 40);
            toWrite[15] = (byte)(msBits >> 48);
            toWrite[16] = (byte)(msBits >> 56);
            
            try {
                this.bus.writeBytes(toWrite);
            } catch (IOException ex) {
                LOGGER.error("Exception writing serial number to sensor.", ex);
            }
        }
        
        return serial;
    }
    
    @Override
    public Calibrator getCalibrator() {
        List<String> userPrompts = Arrays.asList(new String[] {
            "To calibrate the distance sensor, slowly move the sensor through " +
                    "its full range of travel and then click next."
        });
        
        // these will hold maximum values of the data readings
        final Map<String, Float> value = new HashMap<>();
        value.put("maximum", Float.MIN_VALUE);
        value.put("minimum", Float.MAX_VALUE);
        
        // build up the data consumers
        List<Consumer<String>> sampleConsumers = new ArrayList<>();
        sampleConsumers.add(
            (String s) -> {
               // parse value
               int x = Integer.parseInt(s);
               
               // update maximum values
               value.put("maximum", Float.max(value.get("maximum"), x));
               value.put("minimum", Float.min(value.get("minimum"), x));
            });
        
        // formats and returns the calibration data
        Supplier<String> getDataCallback = () -> {
            return "(" + Float.toString(value.get("minimum")) + "," +
                    Float.toString(value.get("maximum")) + ")";
        };
        
        // return the calibrator
        return new CalibratorReducer(
                this,
                29.97f,
                userPrompts,
                sampleConsumers,
                getDataCallback
        );
    }
}
