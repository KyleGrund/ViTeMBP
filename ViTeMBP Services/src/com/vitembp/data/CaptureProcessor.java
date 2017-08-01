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

import com.vitembp.embedded.data.Capture;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides an interface to process data in a capture.
 */
public class CaptureProcessor {
    /**
     * Processes the data in the capture using the pipeline.
     * @param source The capture containing the data to process.
     * @param pipeline The pipeline of elements used to process the data.
     * @return The results of the pipeline application.
     */
    public static Map<String, Object> process(Capture source, final List<SamplePipeline> pipeline) {
        Map<String, Object> toReturn = new HashMap<>();
        
        // processing an empty pipeline does nothing
        if (pipeline.isEmpty()) {
            return toReturn;
        }
        
        // go through samples, and for each one process it with each pipeline
        // element in order
        source.getSamples().forEach((toAccept) -> 
                pipeline.forEach((element) -> 
                        element.accept(toAccept, toReturn)));

        return toReturn;
    }
    
    
}
