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
 * Class providing interface for an NXP FXOS8700CQ chip.
 */
class AccelerometerFXOS8700CQ extends Sensor {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * A UUID representing the type of this sensor.
     */
    private static final UUID TYPE_UUID = UUID.fromString("032477c2-d0c2-4a2f-8b83-2a772a348644");
    
    /**
     * The device object used to communicate on the I2C bus.
     */
    private final I2CDevice device;
    
    /**
     * The UUID representing the serial number of this sensor.
     */
    private final UUID serial;
    
    /**
     * Initializes a new instance of the AccelerometerFXOS8700CQ class.
     * @param name The name of the sensor as used in the system.
     * @param device The device object used to communicate on the I2C bus.
     */
    public AccelerometerFXOS8700CQ(UUID serial, I2CDevice device) {
        this.device = device;
        this.serial = serial;
    }

    @Override
    public UUID getType() {
        return AccelerometerFXOS8700CQ.TYPE_UUID;
    }

    @Override
    public void initialize() {
        // initialize the accelerometer
        // set chip to standby write 0x00 to 0x2a
        this.device.write(new int[] { 0x2a, 0x00 });
        
        // set mode to 8g scale write 0x02 to 0x0e
        this.device.write(new int[] { 0x0e, 0x02 });
        
        // set chip to ready mode write 0x01 to 0x2a
        this.device.write(new int[] { 0x2a, 0x01 });
        
        // enable accel/magnatometer write 0x03 to 0x5b
        this.device.write(new int[] { 0x5b, 0x03 });
    }

    @Override
    public String readSample() {
        // read data (x, y, z) from sensor
        int[] xd = this.device.writeRead(new int[] { 0x01 }, 2);
        int[] yd = this.device.writeRead(new int[] { 0x03 }, 2);
        int[] zd = this.device.writeRead(new int[] { 0x05 }, 2);
        int xh = xd[0];
        int xl = xd[1];
        int yh = yd[0];
        int yl = yd[1];
        int zh = zd[0];
        int zl = zd[1];
        
        // interpret bytes as signed 14bit values
        int signx = (xh & 0x20) == 0x20 ? -1 : 1;
        int signy = (yh & 0x20) == 0x20 ? -1 : 1;
        int signz = (zh & 0x20) == 0x20 ? -1 : 1;
        int x = signx * (((xh & 0x1F) << 8) | xl) / 4;
        int y = signy * (((yh & 0x1F) << 8) | yl) / 4;
        int z = signz * (((zh & 0x1F) << 8) | zl) / 4;
        
        // return values as a string
        return 
                "(" +
                Integer.toString(x) +
                "," +
                Integer.toString(y) +
                "," +
                Integer.toString(z) +
                ")";
    }

    @Override
    public UUID getSerial() {
        return this.serial;
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
               float y = Float.parseFloat(vals[0]);
               float z = Float.parseFloat(vals[0]);
               
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
