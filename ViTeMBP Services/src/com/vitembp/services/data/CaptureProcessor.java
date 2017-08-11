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

import com.vitembp.embedded.data.Capture;
import com.vitembp.embedded.data.Sample;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

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
    public static Map<String, Object> process(Capture source, Pipeline pipeline) {
        Map<String, Object> toReturn = new HashMap<>();
        Iterator<Sample> samples = source.getSamples().iterator();
        pipeline.execute(() -> {
            if (samples.hasNext()) {
                toReturn.put("sample", samples.next());
                return toReturn;
            } else {
                return null;
            }
        });
        
        return toReturn;
    }
    
    /**
     * Processes the data in the capture using the pipeline and then puts empty
     * data through until a flush condition is found.
     * @param source The capture containing the data to process.
     * @param pipeline The pipeline of elements used to process the data.
     * @return The results of the pipeline application.
     */
    public static Map<String, Object> processUntilFlush(Capture source, Pipeline pipeline) {
        Map<String, Object> toReturn = new HashMap<>();
        Iterator<Sample> samples = source.getSamples().iterator();
        pipeline.execute(() -> {
            if (samples.hasNext()) {
                toReturn.put("sample", samples.next());
                return toReturn;
            } else {
                return null;
            }
        });
        
        // feed empty data until pipeline detects end of video frames and
        // flushes itself
        pipeline.execute(new Supplier<Map<String, Object>>() {
            @Override
            public Map<String, Object> get() {
                if (!toReturn.containsKey("Flush")) {
                    return toReturn;
                } else {
                    return null;
                }
            }
        });
        
        return toReturn;
    }
}