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
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class implementing a simple polling event generator for GPIO devices.
 */
public class GPIOPolledEvent {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * The interval between polling in nanoseconds.
     */
    private long millisecondInterval;
    
    /**
     * The port to monitor.
     */
    private final GPIOPort port;
    
    /**
     * The last state read from the GPIO port.
     */
    private boolean lastState = false;
    
    /**
     * A boolean value indicating whether the polling should exit.
     */
    private boolean isRunning = true;
    
    /**
     * The thread which performs the polling.
     */
    private final Thread pollingThread;
    
    /**
     * The callback function accepting notification of GPIO port state changes.
     */
    private final Consumer<Boolean> stateChangedListener;
    
    /**
     * Initializes a new instance of the GPIOPolledEvent class.
     * @param toMonitor The GPIO port to monitor.
     * @param pollingFrequency The polling frequency in hertz.
     * @param stateChangedListener The callback function accepting notification of GPIO port state changes.
     */
    public GPIOPolledEvent(GPIOPort toMonitor, double pollingFrequency, Consumer<Boolean> stateChangedListener) {
        this.port = toMonitor;
        this.stateChangedListener = stateChangedListener;
        
        // calculate polling interval in milliseconds (1/f)*10^3
        this.millisecondInterval = Math.round((1.0d / pollingFrequency) * Math.pow(10.0d, 3.0d));
        
        // create polling thread
        this.pollingThread = new Thread(this::pollingThread);
    }
    
    /**
     * Starts the GPIO polling event generator.
     */
    public void start() {
        this.pollingThread.start();
    }
    
    /**
     * Stops the GPIO polling event generator.
     */
    public void stop() {
        this.isRunning = false;
    }
    
    /**
     * The function run on the polling thread which polls the GPIO state.
     */
    private void pollingThread() {
        try {
            this.lastState = this.port.getValue();
        } catch (IOException ex) {
            LOGGER.error("Error reading initial GPIO port state.", ex);
        }
        
        boolean currentValue = this.lastState;
        while (this.isRunning) {
            try {
                // get current value
                currentValue = this.port.getValue();
                
                // if the current value doesn't match the last check
                if (currentValue != this.lastState) {
                    // notify listeners
                    this.stateChangedListener.accept(currentValue);
                }
                
                // store the new value
                this.lastState = currentValue;
            } catch (IOException ex) {
                LOGGER.error("Error reading initial GPIO port state.", ex);
            }
            
            try {
                // wait for next interval
                Thread.sleep(this.millisecondInterval);
            } catch (InterruptedException ex) {
                LOGGER.error("Thread sleep interrupted waiting for next polling interval.", ex);
            }
        }
    }
}