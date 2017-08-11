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
import com.vitembp.services.sensors.Sensor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Finds the maximum value of the elements that went through the pipeline.
 */
class SampleAverageElement implements PipelineElement {
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
    private final String outputBinding;
    
    /**
     * The sensor we are collecting data for.
     */
    private final Sensor sensorBinding;
       
    /**
     * Initializes a new instance of the SamplePipelineAverage class.
     * @param parser The function which parses the value from the sample data.
     * @param elementCount The binding for input of the count of elements.
     * @param outputBinding The binding for the averaged output.
     * @param sensorBinding The sensor this element is bound to.
     */
    SampleAverageElement(Function<Sample, Double> parser, String elementCount, String outputBinding, Sensor sensorBinding) {
        this.parser = parser;
        this.elementCountBinding = elementCount;
        this.outputBinding = outputBinding;
        this.sensorBinding = sensorBinding;
    }
    
    @Override
    public Map<String, Object> accept(Map<String, Object> state) {
        // do not process data if the pipeline is flushing
        if (state.containsKey("Flush")) {
            return state;
        }
        
        // get necessary values from the data object
        Sample toAccept = (Sample)state.get("sample");
        Double value = parser.apply(toAccept);
        
        // the first time this is executed there will be no map on the data element
        if (!state.containsKey(this.outputBinding)) {
            state.put(this.outputBinding, new HashMap<>());
        }
        
        // get the average for our sensor from the averages collection
        Map<Sensor, Double> averages = (Map<Sensor, Double>)state.get(this.outputBinding);
        Double average = averages.get(this.sensorBinding);
        Long count = (Long)state.get(this.elementCountBinding);
        
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
        
        // save the average back to the data binding
        averages.put(sensorBinding, average);
        state.put(this.outputBinding, averages);
        
        return state;
    }
}
