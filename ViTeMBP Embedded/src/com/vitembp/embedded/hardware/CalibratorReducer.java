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

import com.vitembp.embedded.datacollection.SensorSampler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A calibrator which reduces sensor readings to a calibration string.
 */
class CalibratorReducer extends Calibrator {
    /**
     * The key used in the hash table for the sensor sampler to identify this
     * sensor.
     */
    private static final String SENSOR_HASH_TABLE_KEY = "Sensor";
    
    /**
     * The sensor to calibrate.
     */
    private final Sensor toCalibrate;
    
    /**
     * The sensor sampling frequency;
     */
    private final float sampleFrequency;
    
    /**
     * The list of prompts for the user.
     */
    private final List<String> userPrompts;
    
    /**
     * The list of consumers for the sample results during the calibration.
     */
    private final List<Consumer<String>> readingConsumers;
    
    /**
     * The callback used to retrieve the results.
     */
    private final Supplier<String> resultCallback;
    
    /**
     * The last step for this calibrator.
     */
    private final int lastStep;
    
    /**
     * The current step being executed.
     */
    private int currentStep = -1;
    
    /**
     * The sample consumer currently in use.
     */
    private Consumer<String> currentSampleConsumer;
    
    /**
     * The sampler used to generate sensor samples for calibration.
     */
    private SensorSampler sampler;
    
    /**
     * Initializes a new instance of the CalibratorReducer class.
     * @param toCalibrate The sensor to calibrate.
     * @param sampleFrequency The sensor sampling frequency.
     * @param userPrompts The list of prompts for the user.
     * @param readingConsumers The list of consumers for the sample results during the calibration.
     * @param resultCallback The callback used to retrieve the results.
     */
    CalibratorReducer(Sensor toCalibrate, float sampleFrequency, List<String> userPrompts, List<Consumer<String>> readingConsumers, Supplier<String> resultCallback) {
        // check and save parameters
        if (userPrompts.size() != readingConsumers.size()) {
            throw new IllegalArgumentException("User prompt and reading consumer lists must be the same size.");
        }
        this.toCalibrate = toCalibrate;
        this.sampleFrequency = sampleFrequency;
        this.userPrompts = userPrompts;
        this.readingConsumers = readingConsumers;
        this.resultCallback = resultCallback;
        
        // calculates the last step
        this.lastStep = userPrompts.size() - 1;
        
        // build the sensor sampler
        Map<String, Sensor> sensorMap = new HashMap<>();
        sensorMap.put("Sensor", this.toCalibrate);
        this.sampler = new SensorSampler(this.sampleFrequency, sensorMap, this::processSample);

        // start the first calibration step
        this.beginNextStep();
        
        // start the sampler
        this.sampler.start();
    }
    
    @Override
    public boolean isCalibrating() {
        return this.currentStep <= this.lastStep;
    }

    @Override
    public String getStepPrompt() {
        return this.userPrompts.get(this.currentStep);
    }

    @Override
    public void nextStep() {
        this.beginNextStep();
    }

    @Override
    public String getCalibrationData() {
        return this.resultCallback.get();
    }

    /**
     * Proceeds to the next calibration step.
     */
    private void beginNextStep() {
        this.currentStep++;
        if (!this.isCalibrating()) {
            this.sampler.stop();
        } else {
            this.currentSampleConsumer = this.readingConsumers.get(this.currentStep);
        }
    }
    
    /**
     * Dispatches the sample data to the appropriate listener.
     * @param toProcess The string to dispatch to the appropriate listener.
     */
    private void processSample(Map<String, String> toProcess) {
        this.currentSampleConsumer.accept(toProcess.get(SENSOR_HASH_TABLE_KEY));
    }
}
