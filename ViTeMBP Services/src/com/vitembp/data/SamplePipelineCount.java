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

/**
 * Counts the elements that went through the pipeline.
 */
public class SamplePipelineCount implements SamplePipeline {
    /**
     * The number of data samples that have been processed.
     */
    private long count = 0;
    
    @Override
    public Sample accept(Sample toAccept) {
        this.count++;
        return toAccept;
    }
    
    /**
     * Gets the number of elements that went through the pipeline.
     * @return The number of elements that went through the pipeline.
     */
    public long getCount() {
        return this.count;
    }
}
