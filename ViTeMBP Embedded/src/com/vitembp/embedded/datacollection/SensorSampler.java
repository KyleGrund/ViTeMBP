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
package com.vitembp.embedded.datacollection;

import com.vitembp.embedded.hardware.Sensor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class provides data logging for sensors.
 */
public class SensorSampler {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * The frequency to take samples in hertz.
     */
    private final double sampleFrequency;
    
    /**
     * The interval between successive readings in nanoseconds.
     */
    private final long nanoSecondInterval;
    
    /**
     * The sensors to collect data from.
     */
    private final List<Sensor> sensors;
    
    /**
     * This function is called after a sample has been taken.
     */
    private final Consumer<Map<Sensor, String>> sampleCallback;
    
    /**
     * Boolean value indicating  whether the data logger is running.
     */
    private boolean isRunning = false;
    
    /**
     * The thread that will be used for logging.
     */
    private Thread loggingThread;
    
    /**
     * Initializes a new instance of the DataLogger class.
     * @param frequency The frequency to take samples at.
     * @param sensors The sensors to collect data from.
     * @param callback The 
     */
    public SensorSampler(double frequency, List<Sensor> sensors, Consumer<Map<Sensor, String>> callback) {
        this.sampleFrequency = frequency;
        this.sensors = sensors;
        this.sampleCallback  = callback;
        this.nanoSecondInterval = Math.round((1.0d / frequency) * Math.pow(10.0d, 9.0d));
    }
    
    /**
     * Start data logging.
     */
    public void start() {
        LOGGER.debug("Starting data logging.");
        this.isRunning = true;
        this.loggingThread = new Thread(() -> { collectData(); });
        this.loggingThread.setName("DataLogger");
        this.loggingThread.start();
    }
    
    /**
     * Stop data logging.
     */
    public void stop() {
        LOGGER.debug("Stopping data logging.");
        this.isRunning = false;
    }
    
    /**
     * The function which is run by the thread which collects data from the  sensors.
     */
    private void collectData() {
        // initialize sensors
        for (Sensor sensor : this.sensors) {
            sensor.initialize();
        }
        
        Long nextStart = System.nanoTime();
        
        // collect data
        while (this.isRunning) {
            // calculate the time which the next data collection interval
            // should start
            nextStart += this.nanoSecondInterval;
            
            // take data
            HashMap<Sensor, String> data = new HashMap<>();
            for (Sensor sensor : this.sensors) {
                String sample = sensor.readSample();
                data.put(sensor, sample);
                LOGGER.debug("Sensor " + sensor.getName() + ": " + sample);
            }
            
            // notify listeners
            this.sampleCallback.accept(data);
            
            // wait for next data collection interval
            Long toWait = nextStart - System.nanoTime();
            if (toWait > 0) {
                Long millis = toWait / 1000000;
                int nanos = (int)(toWait % 1000000);
                try {
                    LOGGER.debug("Waiting " + millis + "ms, " + nanos + "ns for next sample.");
                    Thread.sleep(millis, nanos);
                } catch (InterruptedException ex) {
                    LOGGER.error("Thread sleep was interrupted.", ex);
                }
            } else {
                LOGGER.error("Sample time missed.");
            }
        }
    }
}
