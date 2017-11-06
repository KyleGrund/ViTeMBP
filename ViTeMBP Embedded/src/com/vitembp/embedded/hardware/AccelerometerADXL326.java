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
import org.apache.logging.log4j.Logger;

/**
 * Sensor driver for the ADXL326 16G accelerometer.
 */
public class AccelerometerADXL326 extends Sensor {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * A UUID representing the type of this sensor.
     */
    public static final UUID TYPE_UUID = UUID.fromString("f06ee9e1-345a-490d-8b03-a736a5e5d7bf");
    
    /**
     * A UUID representing serial number for this sensor.
     */
    private static final UUID SERIAL_UUID = UUID.fromString("d9395313-35f5-4d55-9b52-9948d85e8f41");
    
    /**
     * The object used to communicate on the serial bus.
     */
    private final SerialBus bus;
    
    /**
     * Initializes a new instance of the AccelerometerADXL326 class.
     * @param bus The device object used to communicate on the serial bus.
     */
    AccelerometerADXL326(SerialBus bus) {
        this.bus = bus;
    }

    @Override
    public UUID getType() {
        return AccelerometerADXL326.TYPE_UUID;
    }

    @Override
    public void initialize() {
    }

    @Override
    public String readSample() {
        // read data (x, y, z) from sensor
        byte[] result;
        try {
            this.bus.writeBytes(new byte[] { (byte)'r' });
            //result = this.bus.readBytes(6);
        
        
        // parse out bytes to their individual axis values
        byte[] reading = this.bus.readBytes(6);
        
        // the 10-bit ADC values are packed into two bytes
        float x = (reading[0] << 8) | reading[1] & 0xff;
        float y = (reading[2] << 8) | reading[3] & 0xff;
        float z = (reading[4] << 8) | reading[5] & 0xff;
        
        // the sensor returns a value between 0 and 1023 inclusively
        // values below 1/2 of the range represent negative G measurements
        // values above represent positive measurements. The total scale is
        // 16G leadint to the formula: ((reading / 1023) + 0.5) * 16
        x = ((x / 1023.0f) - 0.5f) * 16.0f;
        y = ((y / 1023.0f) - 0.5f) * 16.0f;
        z = ((z / 1023.0f) - 0.5f) * 16.0f;
        
        // return values as a string
        return 
                "(" +
                Float.toString(x) +
                "," +
                Float.toString(y) +
                "," +
                Float.toString(z) +
                ")";
        } catch (IOException ex) {
            LOGGER.error("Error reading from accelerometer.", ex);
            return "";
        }
    }

    @Override
    public UUID getSerial() {
        return AccelerometerADXL326.SERIAL_UUID;
    }

    @Override
    public Calibrator getCalibrator() {
        List<String> userPrompts = Arrays.asList(new String[] {
            "To calibrate the accelerometer, slowly rotate the sensor in " +
                    "both a horizontal and a vertical circle so that all " + 
                    " edges face down one at a time."
        });
        
        // these will hold min minum and maximum values of the data readings
        final Map<String, Float> maximums = new HashMap<>();
        maximums.put("x", Float.MIN_VALUE);
        maximums.put("y", Float.MIN_VALUE);
        maximums.put("z", Float.MIN_VALUE);
        final Map<String, Float> minimums = new HashMap<>();
        minimums.put("x", Float.MAX_VALUE);
        minimums.put("y", Float.MAX_VALUE);
        minimums.put("z", Float.MAX_VALUE);
        
        // build up the data consumers
        List<Consumer<String>> sampleConsumers = new ArrayList<>();
        sampleConsumers.add(
            (String s) -> {
               // parse values
               String[] vals = s.replace("(", "").replace(")", "").split(",");
               float x = Float.parseFloat(vals[0]);
               float y = Float.parseFloat(vals[1]);
               float z = Float.parseFloat(vals[2]);
               
               // update maximum values
               maximums.put("x", Float.max(maximums.get("x"), x));
               maximums.put("y", Float.max(maximums.get("y"), y));
               maximums.put("z", Float.max(maximums.get("z"), z));
               
               // update minimum values
               minimums.put("x", Float.min(minimums.get("x"), x));
               minimums.put("y", Float.min(minimums.get("y"), y));
               minimums.put("z", Float.min(minimums.get("z"), z));
            });
        
        // formats and returns the calibration data
        Supplier<String> getDataCallback = () -> {
            return "[(" + Float.toString(minimums.get("x")) + "," + Float.toString(maximums.get("x")) + ")," +
                    "(" + Float.toString(minimums.get("y")) + "," + Float.toString(maximums.get("y")) + ")," +
                    "(" + Float.toString(minimums.get("z")) + "," + Float.toString(maximums.get("z")) + ")]";
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
