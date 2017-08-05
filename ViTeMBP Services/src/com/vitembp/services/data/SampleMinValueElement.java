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
package com.vitembp.services.data;

import com.vitembp.embedded.data.Sample;
import java.util.Map;
import java.util.function.Function;

/**
 * Finds the minimum value of the elements that went through the pipeline.
 */
class SampleMinValueElement implements PipelineElement {
    /**
     * The function which parses values from the sample data.
     */
    private final Function<Sample, Double> parser;
    
    /**
     * The binding of the min value in the data collection.
     */
    private final String minBinding;
    
    /**
     * Initializes a new instance of the SamplePipelineMaxValue class.
     * @param parser The function which parses the value from the sample data.
     * @param minBinding The binding of the maximum value in the data collection.
     */
    SampleMinValueElement(Function<Sample, Double> parser, String minBinding) {
        this.parser = parser;
        this.minBinding = minBinding;
    }
    
    @Override
    public Map<String, Object> accept(Map<String, Object> data) {
        // get necessary values from the data object
        Sample toAccept = (Sample)data.get("sample");
        Double minValue = (Double)data.get(this.minBinding);
        Double value = parser.apply(toAccept);
        
        // the minValue will be null until the first sample is processed
        if (minValue == null) {
            minValue = Double.MAX_VALUE;
        }
        
        // the value may be null if a sample is not taken
        if (value != null) {
            if (value < minValue) {
                minValue = value;
            }
            
            data.put(this.minBinding, minValue);
        }
        
        return data;
    }
}
