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

import com.vitembp.services.FilenameGenerator;
import com.vitembp.services.video.Conversion;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This element collects frames and composes them into an MP4.
 */
class FrameCollectorElement implements PipelineElement {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * A list of files that this element has collected.
     */
    private final List<Path> fileList = new ArrayList<>();
    
    /**
     * The filename generator used to generate the files to be collected.
     */
    private final FilenameGenerator nameGenerator;
    
    /**
     * The location on the state object of the output video file.
     */
    private final String videoFileBinding;
    
    /**
     * The location of the current file being processed on the state object.
     */
    private final String fileNameBinding;
    
    /**
     * The number of files to accumulate before appending them to the output video.
     */
    private final int batchSize;
    
    /**
     * The output video frame rate.
     */
    private final double frameRate;
    
    /**
     * The key of the final video output file path on the state object.
     */
    private final String finalOutputBinding;
    
    /**
     * Initializes a new instance of the FrameCollectorEelement.
     * @param nameGenerator The generator which creates the file names to be collected.
     * @param fileNameBinding The binding to the name of the current frame to collect.
     * @param outputVideoBinding The binding to output the generated video file to.
     * @param finalOutputBinding The binding to get the final output video location.
     * @param batchSize The number of frames to collect before putting them into a video.
     * @param frameRate The frame rate to encode the video at.
     */
    FrameCollectorElement(FilenameGenerator nameGenerator, String fileNameBinding, String outputVideoBinding, String finalOutputBinding, int batchSize, double frameRate) {
        this.nameGenerator = nameGenerator;
        this.fileNameBinding = fileNameBinding;
        this.videoFileBinding = outputVideoBinding;
        this.batchSize = batchSize;
        this.frameRate = frameRate;
        this.finalOutputBinding = finalOutputBinding;
    }
    
    @Override
    public Map<String, Object> accept(Map<String, Object> state) {
        // get current file name
        Path file = (Path)state.get(this.fileNameBinding);
        
        // add to merege list
        if (file != null) {
            if (Files.exists(file)) {
                this.fileList.add(file);
            }
            
            // clear off the state var to prevent repeat encodings
            state.put(this.fileNameBinding, null);
        }
        
        // if merge list is greater than batch size or pipeline is flushing
        if (this.fileList.size() >= this.batchSize || (state.containsKey("Flush") && this.fileList.size() > 0))
        {
            // get the video file extension
            Path dest = (Path)state.get(this.finalOutputBinding);
            if (dest == null) {
                throw new PipelineExecutionException("Could not find output file binding.", new NullPointerException());
            }
            String[] splitName = dest.toString().split("\\.");
            String extension = splitName[splitName.length - 1];
            
            // get a temp output file location
            Path vidOut = dest.getParent().resolve("out." + extension);
            for (int i = 0; Files.exists(vidOut); i++) {
                vidOut = dest.getParent().resolve(Integer.toString(i) + "out." + extension);
            }
            
            // put the location on the state variable for downstream elements
            state.put(this.videoFileBinding, vidOut);
            
            try {
                // get the target directory from the files to assemble
                Path targetDir = fileList.get(0).getParent();
                
                // encode merge list to video file
                Conversion.assembleFrames(
                        targetDir,
                        vidOut,
                        this.nameGenerator,
                        this.frameRate);
            } catch (IOException ex) {
                LOGGER.error("Exception encoding frames." , ex);
                throw new PipelineExecutionException("Exception encoding frames." , ex);
            }
            
            try {
                // delete files in merge list
                for (Path toDelete : this.fileList) {
                    Files.delete(toDelete);
                }
            } catch (IOException ex) {
                LOGGER.error("Exception deleting encoded frames." , ex);
                throw new PipelineExecutionException("Exception deleting encoded frames." , ex);
            }
                
            // clear merge list
            this.fileList.clear();
        }
        
        return state;
    }    
}
