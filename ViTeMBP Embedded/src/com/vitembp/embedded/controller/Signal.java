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

import java.util.function.Consumer;

/**
 * Base class that can signal an external event.
 */
public abstract class Signal {
    /**
     * The callback to send results to the signaler.
     */
    private final Consumer<String> resultCallback;
    
    /**
     * Flag used to prevent multiple result responses.
     */
    private boolean hasResponded = false;
    
    /**
     * Initializes a new instance of the Signal class.
     * @param resultCallback The 
     */
    public Signal(Consumer<String> resultCallback) {
        this.resultCallback = resultCallback;
    }
    
    /**
     * Call to return the result of this command to the source.
     * @param result The string containing the result of the command.
     */
    synchronized void returnResult(String result) {
        if (hasResponded) {
            throw new IllegalStateException("Cannot call returnResult multiple times.");
        } else {
            hasResponded = true;
            this.resultCallback.accept(result);
        }
    }
}
