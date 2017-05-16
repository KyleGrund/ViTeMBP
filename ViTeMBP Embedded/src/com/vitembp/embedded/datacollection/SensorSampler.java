/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    private final Consumer<Map<String, String>> sampleCallback;
    
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
    public SensorSampler(double frequency, List<Sensor> sensors, Consumer<Map<String,String>> callback) {
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
            HashMap<String, String> data = new HashMap<>();
            for (Sensor sensor : this.sensors) {
                String sample = sensor.readSample();
                String name = sensor.getName();
                data.put(name, sample);
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
