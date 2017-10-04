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

import com.vitembp.services.video.Conversion;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * This element applies audio from one video file to another.
 */
class CopyAudioElement implements PipelineElement {
    /**
     * The key of the source video path on the state object.
     */
    private final String sourceVideoBinding;
    
    /**
     * The key of the source video path on the state object.
     */
    private final String destVideoBinding;
    
    /**
     * Keeps track of whether the audio has been copied for this pipeline run.
     */
    private final String copiedBinding;
    
    /**
     * Initializes a new instance of the CopyAudioElement class.
     * @param sourceVideoBinding The key of the source video path on the state object.
     * @param destVideoBinding The key of the source video path on the state object.
     * @param copiedBinding The key of the boolean value indicating whether the
     * audio has already been copied.
     */
    CopyAudioElement(String sourceVideoBinding, String destVideoBinding, String copiedBinding) {
        this.sourceVideoBinding = sourceVideoBinding;
        this.destVideoBinding = destVideoBinding;
        this.copiedBinding = copiedBinding;
    }
    
    @Override
    public Map<String, Object> accept(Map<String, Object> state) {
        // if the pipeline is flushing
        if (state.containsKey("Flush")) {
            // if the copied binding has not been set or it is set to false
            if (!state.containsKey(this.copiedBinding) || !(boolean)state.get(this.copiedBinding)) {
                // get original video  file
                Path sourceFile = (Path)state.get(this.sourceVideoBinding);

                // get output video file
                Path destFile = (Path)state.get(this.destVideoBinding);

                try {
                    // copy audio from original to output
                    Conversion.copyAudio(sourceFile, destFile);
                } catch (IOException ex) {
                    throw new PipelineExecutionException("IOException while coping video file audio.", ex);
                }
                
                // mark as copied so as to not repeat the copy
                state.put(this.copiedBinding, true);
            }
        }
        return state;
    }
    
}
