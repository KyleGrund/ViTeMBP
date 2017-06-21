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
package com.vitembp.embedded.controller;

import com.vitembp.embedded.hardware.Platform;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;

/**
 * Controller state machine implementation.
 */
public class StateMachine {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * The thread the state machine executes on.
     */
    private final Thread executionThread;
    
    /**
     * The state machine execution context.
     */
    private final ExecutionContext context;
    
    /**
     * The state instances for the state machine.
     */
    private final Map<Class, ControllerState> states;
    
    /**
     * A boolean value indicating whether the machine is running.
     */
    private boolean isRunning = true;
    
    /**
     * Initializes a new instance of the StateMachine class.
     * @param toControl The platform object interfacing with the hardware.
     */
    public StateMachine(Platform toControl) {
        this.executionThread = new Thread(this::executeMachine);
        
        // create execution context
        this.context = new ExecutionContext(toControl);
        
        // build states
        this.states = new HashMap<>();
        this.states.put(EndCapture.class, new EndCapture());
        this.states.put(New.class, new New());
        this.states.put(StartCapture.class, new StartCapture());
        this.states.put(WaitForEnd.class, new WaitForEnd());
        this.states.put(WaitForStart.class, new WaitForStart());
    }
    
    /**
     * Starts the execution of the state machine.
     * @throws IllegalThreadStateException if the state machine has already been started.
     */
    public void start() throws IllegalThreadStateException {
        this.executionThread.start();
    }
    
    /**
     * Stops the execution of the state machine.
     */
    public void stop() {
        this.isRunning = false;
    }
    
    /**
     * Machine executor function.
     */
    private void executeMachine() {
        Class nextState = New.class;
        
        while (this.isRunning) {
            LOGGER.info("Executing: " + nextState.getSimpleName());
            nextState = this.states.get(nextState).execute(this.context);
        }
    }
}
