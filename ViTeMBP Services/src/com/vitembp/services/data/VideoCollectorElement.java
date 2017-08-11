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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This element collects frames and composes them into an MP4.
 */
class VideoCollectorElement implements PipelineElement {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * The location on the state object of the output video file.
     */
    private final String outputFileBinding;
    
    /**
     * The location of the current file being processed on the state object.
     */
    private final String fileNameBinding;
    
    /**
     * Initializes a new instance of the FrameCollectorEelement.
     * @param nameGenerator The generator which creates the file names to be collected.
     * @param outputFile The name of the output video file.
     */
    VideoCollectorElement(String fileNameBinding, String outputFileBinding) {
        this.fileNameBinding = fileNameBinding;
        this.outputFileBinding = outputFileBinding;
    }
    
    @Override
    public Map<String, Object> accept(Map<String, Object> state) {
        // get current file name
        Path file = (Path)state.get(this.fileNameBinding);
        
        // there will only be a file if one was generated upstream in the pipe
        if (file != null)
        {
            // set the file to null so it won't be processed twice
            state.put(this.fileNameBinding, null);
            
            // get and verify the output location
            Path dest = (Path)state.get(this.outputFileBinding);
            if (dest == null) {
                throw new PipelineExecutionException("Could not find output file binding.", new NullPointerException());
            }
            
            // if the destination already exists concat the video to it,
            // otherwise just copy the new file to the destination
            if (Files.exists(dest)) {
                try {
                    // encode merge list to video file
                    Conversion.combineVideos(dest, file);
                } catch (IOException ex) {
                    LOGGER.error("Exception encoding frames." , ex);
                    throw new PipelineExecutionException("Exception encoding frames." , ex);
                }
            } else {
                try {
                    Files.move(file, dest);
                } catch (IOException ex) {
                    LOGGER.error("Exception moving video." , ex);
                    throw new PipelineExecutionException("Exception moving video.", ex);
                }
            }
        }
        
        return state;
    }
}
