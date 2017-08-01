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

/**
 * Counts the elements that went through the pipeline.
 */
public class CountElement implements PipelineElement {
    /**
     * The number of data samples that have been processed.
     */
    private final String countBinding;
    
    /**
     * Initializes a new instance of the SamplePipelineCount class.
     * @param countBinding The binding for the sample count in the data collection.
     */
    public CountElement(String countBinding) {
        this.countBinding = countBinding;
    }
    
    @Override
    public Map<String, Object> accept(Map<String, Object> data) {
        // get necessary values from the data object
        Long count = (Long)data.get(this.countBinding);
        
        // the count will be missing on the first applicaiton
        if (count == null) {
            count = 0L;
        }
        
        // increment the count, then save it
        count++;
        data.put(this.countBinding, count);
        
        return data;
    }
}
