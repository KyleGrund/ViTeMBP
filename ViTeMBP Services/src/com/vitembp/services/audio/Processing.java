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
package com.vitembp.services.audio;

import com.vitembp.services.video.VideoFileInfo;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Functions for processing audio.
 */
public class Processing {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Finds sync frames based on the audio signal.
     * @param sourceFile The source wave file.
     * @param signalFrequency The sync signal frequency.
     * @return A list of sync frames where the audio signal was detected.
     * @throws java.io.IOException If there is an exception processing the video
     * file.
     */
    public static List<Integer> findSyncFrames(String sourceFile, double signalFrequency) throws IOException {
        // create a temporary file for the output
        Path localTempOutput = Files.createTempFile("vitembp", ".wav");
        localTempOutput.toFile().delete();
        
        // extract audio
        com.vitembp.services.video.Conversion.extractWaveAudio(sourceFile, localTempOutput.toString());
        
        // get data frame chunks of data
        File fileIn = localTempOutput.toFile();
        
        // provides video file information, such as frame rate
        VideoFileInfo videoInfo = new VideoFileInfo(new File(sourceFile));
        
        // performs an SignalProcessing on the audio of each video frame and gets the value
        // at the target signal frequency
        List<Double> frameValues = SignalProcessing.getFrameFFTValues(fileIn, videoInfo, signalFrequency);
        
        // perform an averaging window to remove noise
        List<Double> averagedValues = SignalProcessing.appplyAveragingWindow(frameValues, 6);
        
        // calculate the length of the audio signal in frames
        double signalTimeInSeconds = 2.0;
        int signalFrameLength = (int)Math.round(videoInfo.getFrameRate() * signalTimeInSeconds);
        
        // find the sync frames
        Set<Integer> syncFramesAvg = SyncDetectionAlgorithms.findSyncFramesByAveragingWindow(averagedValues, signalFrameLength);
        
        // remove temp file
        localTempOutput.toFile().delete();
        
        // return frames
        return new ArrayList<>(syncFramesAvg);
    }
}
