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
public class SampleAverageElement implements PipelineElement {
    /**
     * The function which parses values from the sample data.
     */
    private final Function<Sample, Double> parser;
    
    /**
     * The binding of the element count in the data collection.
     */
    private final String elementCountBinding;
    
    /**
     * The binding of the output of the average in the data collection.
     */
    private final String averageOutputBinding;
       
    /**
     * Initializes a new instance of the SamplePipelineAverage class.
     * @param parser The function which parses the value from the sample data.
     * @param elementCount The binding for input of the count of elements.
     * @param averageOutput The binding for the averaged output.
     */
    public SampleAverageElement(Function<Sample, Double> parser, String elementCount, String averageOutput) {
        this.parser = parser;
        this.elementCountBinding = elementCount;
        this.averageOutputBinding = averageOutput;
    }
    
    @Override
    public Map<String, Object> accept(Map<String, Object> data) {
        // get necessary values from the data object
        Sample toAccept = (Sample)data.get("sample");
        Double value = parser.apply(toAccept);
        Double average = (Double)data.get(this.averageOutputBinding);
        Long count = (Long)data.get(this.elementCountBinding);
        
        // the item count is a required input
        if (count == null) {
            throw new IllegalStateException("Average function cannot find count in data variable. Check pipeline composition.");
        }
        
        // the average will be missing if this is the first sample application
        if (average == null) {
            average = 0.0;
        }
        
        // the value will be null if no data was taken for a particular sample
        if (value == null) {
            value = 0.0d;
        }
        
        // perform the running average calculation
        average = (average * (count - 1) + value) / count;
        
        data.put(this.averageOutputBinding, average);
        
        return data;
    }
}
