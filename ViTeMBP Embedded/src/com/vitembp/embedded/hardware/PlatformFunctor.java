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

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An implementation of the Platform class using the functor pattern.
 */
public class PlatformFunctor extends Platform {
    /**
     * The function which sets the state of the synchronization light.
     */
    private final ConsumerIOException<Boolean> setSyncLightTarget;
    
    /**
     * The callback used to set the keypad key press consumer.
     */
    private final Consumer<Consumer<Character>> setKeypadCallback;
    
    /**
     * A supplier of Map of String to Sensor object for sensors for this system.
     */
    private final Supplier<Set<Sensor>> getSensorsTarget;
    
    /**
     * Initializes a new instance of the PlatformFunctor class.
     * @param setSyncLightTarget Callback that controls the synchronization light.
     * @param getSensorsTarget Callback that provides a list of sensors.
     * @param setKeypadCallback Callback to set a key press listener.
     */
    public PlatformFunctor(
            ConsumerIOException<Boolean> setSyncLightTarget,
            Consumer<Consumer<Character>> setKeypadCallback,
            Supplier<Set<Sensor>> getSensorsTarget) {
        this.setSyncLightTarget = setSyncLightTarget;
        this.setKeypadCallback = setKeypadCallback;
        this.getSensorsTarget = getSensorsTarget;
    }
    
    @Override
    public ConsumerIOException<Boolean> getSetSyncLightTarget() {
        return this.setSyncLightTarget;
    }

    @Override
    public void setKeypadCallback(Consumer<Character> callback) {
        this.setKeypadCallback.accept(callback);
    }

    @Override
    public Set<Sensor> getSensors() {
        return this.getSensorsTarget.get();
    }
}
