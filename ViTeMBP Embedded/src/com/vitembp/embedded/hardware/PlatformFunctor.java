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

import com.vitembp.embedded.data.BiConsumerIOException;
import com.vitembp.embedded.data.ConsumerIOException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An implementation of the Platform class using the functor pattern.
 */
class PlatformFunctor extends Platform {
    /**
     * The function which sets the state of the synchronization light.
     */
    private final ConsumerIOException<Boolean> setSyncLightTarget;
    
    /**
     * The function which sets the state of the buzzer.
     */
    private final ConsumerIOException<Boolean> setBuzzerTarget;
    
    /**
     * The callback used to set the keypad key press consumer.
     */
    private final Consumer<Consumer<Character>> setKeypadCallback;
    
    /**
     * A supplier of Map of String to Sensor object for sensors for this system.
     */
    private final Supplier<Set<Sensor>> getSensorsTarget;
    
    /**
     * A supplier of a Path to the default configuration for the platform.
     */
    private final Supplier<Path> getDefaultConfigPath;
    
    /**
     * The callback which initializes hardware at system startup.
     */
    private final Runnable initializeCallback;
    
    /**
     * The callback which will set the metric of the interface described.
     */
    private final BiConsumerIOException<String, Integer> setInterfaceMetric;
    
    /**
     * Initializes a new instance of the PlatformFunctor class.
     * @param setSyncLightTarget Callback that controls the synchronization light.
     * @param getSensorsTarget Callback that provides a list of sensors.
     * @param setKeypadCallback Callback to set a key press listener.
     */
    public PlatformFunctor(
            ConsumerIOException<Boolean> setSyncLightTarget,
            ConsumerIOException<Boolean> setBuzzerTarget,
            Consumer<Consumer<Character>> setKeypadCallback,
            Supplier<Set<Sensor>> getSensorsTarget,
            Supplier<Path> getDefaultConfigPath,
            Runnable initializeCallback,
            BiConsumerIOException<String, Integer> setInterfaceMetric) {
        this.setSyncLightTarget = setSyncLightTarget;
        this.setBuzzerTarget = setBuzzerTarget;
        this.setKeypadCallback = setKeypadCallback;
        this.getSensorsTarget = getSensorsTarget;
        this.getDefaultConfigPath = getDefaultConfigPath;
        this.initializeCallback = initializeCallback;
        this.setInterfaceMetric = setInterfaceMetric;
    }
    
    @Override
    public ConsumerIOException<Boolean> getSetSyncLightTarget() {
        return this.setSyncLightTarget;
    }

    @Override
    public ConsumerIOException<Boolean> getBuzzerTarget() {
        return this.setBuzzerTarget;
    }
    
    @Override
    public void setKeypadCallback(Consumer<Character> callback) {
        this.setKeypadCallback.accept(callback);
    }

    @Override
    public Set<Sensor> getSensors() {
        return this.getSensorsTarget.get();
    }

    @Override
    Path getDefaultConfigPath() {
        return this.getDefaultConfigPath.get();
    }

    @Override
    void initialize() {
        this.initializeCallback.run();
    }

    @Override
    void setWiredEthernetMetric(int metric) throws IOException {
        this.setInterfaceMetric.accept("eth0", metric);
    }

    @Override
    void setWirelessEthernetMetric(int metric) throws IOException {
        this.setInterfaceMetric.accept("wlan0", metric);
    }

    @Override
    void setBluetoothMetric(int metric) throws IOException {
        this.setInterfaceMetric.accept("bnep0", metric);
    }
}
