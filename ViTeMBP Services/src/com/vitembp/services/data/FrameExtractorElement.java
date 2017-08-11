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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * This element extracts frames from a video file in batches.
 */
class FrameExtractorElement implements PipelineElement {
    /**
     * The video file to extract frames from.
     */
    private final Path inputFile;
    
    /**
     * The temporary directory to put extracted frames into.
     */
    private final Path outputDir;
    
    /**
     * The generator for frame filenames.
     */
    private final FilenameGenerator nameGenerator;
    
    /**
     * The number of frames to extract in a single extraction process as they
     * are expensive in time and disk space.
     */
    private final int frameBatchSize;
    
    /**
     * The key of the output frame filename stored to the state object.
     */
    private final String frameNameBinding;
    
    /**
     * The key of the sample count on the state object.
     */
    private final String countBinding;
    
    /**
     * Initializes a new instance of the FrameExtractorElement class.
     * @param inputFile The video file to extract frames from.
     * @param outputDir The temporary directory to put extracted frames into.
     * @param nameGenerator The generator for frame filenames.
     * @param frameBatchSize The number of frames to extract in a single extraction process as they
     * are expensive in time and disk space.
     */
    FrameExtractorElement(Path inputFile, Path outputDir, FilenameGenerator nameGenerator, int frameBatchSize, String frameNameBinding, String countBinding) {
        this.inputFile = inputFile;
        this.outputDir = outputDir;
        this.nameGenerator = nameGenerator;
        this.frameBatchSize = frameBatchSize;
        this.frameNameBinding = frameNameBinding;
        this.countBinding = countBinding;
    }
    
    @Override
    public Map<String, Object> accept(Map<String, Object> state) throws PipelineExecutionException {
        // do not process data if the pipeline is flushing
        if (state.containsKey("Flush")) {
            return state;
        }
        
        // get current frame number
        long frame = (long)state.get(this.countBinding);
        
        long expectedFileNum = frame;
        while (expectedFileNum > this.frameBatchSize) {
            expectedFileNum -= this.frameBatchSize;
        }
        
        // get expected file name
        Path expectedFile = this.outputDir.resolve(this.nameGenerator.getPath((int)expectedFileNum));
        
        // if the current file doesn't exist extract the next batch
        if (!Files.exists(expectedFile))
        {
            try {
                // extract the next group
                com.vitembp.services.video.Conversion.extractFrames(
                        inputFile.toString(),
                        outputDir,
                        (int)frame - 1,
                        frameBatchSize,
                        nameGenerator);
            } catch (IOException ex) {
                throw new PipelineExecutionException("IOException while extracting frames.", ex);
            }
        }
        
        // if the file wasn't extracted indicate to the rest of the pipeline
        // that it should flush and clean up
        if (!Files.exists(expectedFile)) {
            state.put("Flush", Boolean.TRUE);
        } else {
            // output frame filename to data object
            state.put(this.frameNameBinding, expectedFile);
        }
        
        return state;
    }
    
}
