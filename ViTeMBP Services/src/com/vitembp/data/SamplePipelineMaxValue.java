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
package com.vitembp.data;

import com.vitembp.embedded.data.Sample;
import java.util.Map;
import java.util.function.Function;

/**
 * Finds the maximum value of the elements that went through the pipeline.
 */
public class SamplePipelineMaxValue implements SamplePipeline {
    /**
     * The maximum value that has been seen.
     */
    private double maxValue = Double.MIN_VALUE;
    
    /**
     * The function which parses values from the sample data.
     */
    private final Function<Sample, Double> parser;
    
    /**
     * Initializes a new instance of the SamplePipelineMaxValue class.
     * @param parser The function which parses the value from the sample data.
     */
    public SamplePipelineMaxValue(Function<Sample, Double> parser) {
        this.parser = parser;
    }
    
    @Override
    public Sample accept(Sample toAccept) {
        Map<String, String> data = toAccept.getSensorData();
        Double value = parser.apply(toAccept);
        if (value != null) {
            if (value > this.maxValue) {
                this.maxValue = value;
            }
        }
        
        return toAccept;
    }
    
    /**
     * Gets the maximum value of the parsed data.
     * @return The maximum value of the parsed data.
     */
    public double getMaxValue() {
        return this.maxValue;
    }
}
