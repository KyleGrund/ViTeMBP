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
import com.vitembp.embedded.data.Sample;
import java.util.function.Function;

/**
 * Provides an interface to process data in a capture.
 */
public class CaptureProcessor {
    /**
     * Processes the data in the capture using the pipeline.
     * @param source The capture containing the data to process.
     * @param pipeline The pipeline of elements used to process the data.
     */
    public static void process(Capture source, final SamplePipeline[] pipeline) {
        // processing an empty pipeline does nothing
        if (pipeline.length < 1) {
            return;
        }
        
        // compose a pipeline by combining all the elements in the array
        Function<Sample, Sample> composed = s -> pipeline[0].accept(s);
        
        for (int i = 1; i < pipeline.length; i++) {
            final int index = i;
            Function<Sample, Sample> prev = composed;
            composed = s -> prev.apply(pipeline[index].accept(s));
        }
        
        final Function<Sample, Sample> pipe = composed;
        
        // put all samples through the pipeline
        source.getSamples().forEach(pipe::apply);
    }
}
