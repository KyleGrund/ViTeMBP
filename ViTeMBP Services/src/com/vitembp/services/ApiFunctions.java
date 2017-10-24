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
package com.vitembp.services;

import com.vitembp.services.data.CaptureCalculations;
import com.vitembp.services.data.CaptureOperations;
import com.vitembp.services.video.Processing;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class contains functions which act as the primary interface to
 * the functionality provided by the service classes.
 */
public final class ApiFunctions {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * The possible color channels to perform operations with.
     */
    public static enum COLOR_CHANNELS {
        RED,
        GREEN,
        BLUE
    };
    
    /**
     * The file name generator which provides cross context names.
     */
    private final FilenameGenerator fileGenerator;
    
    /**
     * Initializes a new instance of the ApiFunctions class.
     * @param fileGenerator The file name generator which provides cross context
     * names.
     */
    public ApiFunctions(FilenameGenerator fileGenerator) {
        this.fileGenerator = fileGenerator;
    }
    
    /**
     * Finds the frames which have an outlier brightness in the given color.
     * channel.
     * @param videoFile The file to examine.
     * @param channel The color channel to evaluate.
     * @return The frames which have an outlier brightness in the given color.
     * @throws java.io.IOException If there is an IOException processing the
     * video file.
     */
    public List<Integer> findChannelSyncFrames(String videoFile, COLOR_CHANNELS channel) throws IOException {
        return Processing.findChannelSyncFrames(videoFile, channel, this.fileGenerator);
    }
    
    /**
     * Creates a video from the input with diagnostics data about the find
     * synchronization frames algorithm superimposed on top.
     * @param videoFile The input file.
     * @param channel The color channel to evaluate.
     * @param outputFile The output file.
     * @return The frames which have an outlier brightness in the given color.
     * @throws IOException If there is an IOException while processing the
     * video file.
     */
    public List<Integer> findChannelSyncFramesDiag(String videoFile, COLOR_CHANNELS channel, Path outputFile) throws IOException {
        return Processing.findChannelSyncFrames(videoFile, channel, fileGenerator, outputFile);
    }
    
    /**
     * Triggers a video to be processed by putting the command message on a queue.
     * @param capture The capture to process.
     * @param videoFile The associated video file.
     * @param detinationBucket The bucket to place the output file.
     * @param destinationFilename The file to upload the output to in the target bucket.
     * @throws java.io.IOException If there is an IOException processing the
     * video file.
     */
    public void processCaptureVideo(UUID capture, String videoFile, String detinationBucket, String destinationFilename) throws IOException {
        // this is a long running task, so enqueue it for async processing
        Processing.processVideo(capture, videoFile, detinationBucket, destinationFilename);
    }
    
    /**
     * Deletes a capture and all of its associated data and index entries.
     * @param toDelete The UUID of the capture to delete.
     * @return A message indicating the disposition of the request.
     * @throws IOException If there is an error deleting form the data store.
     */
    public String deleteCapture(UUID toDelete) throws IOException {
        return CaptureOperations.delete(toDelete);
    }
    
    /**
     * Builds and returns summary data for the capture.
     * @param toSummarize The UUID of the capture to summarize.
     * @return A JSON string of summary data.
     * @throws IOException If there is an error reading from the data store.
     */
    public String summarizeCapture(UUID toSummarize) throws IOException {
        return CaptureCalculations.getSummaryData(toSummarize);
    }
    
    /**
     * Returns a set of hashes of database entries to use when syncing.
     * @param toHash The entries to hash.
     * @return A Map of UUID keys to hash values.
     */
    public Map<UUID, String> getDataStoreHashes(List<UUID> toHash) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }
    
    /**
     * Writes or updates a value to the data store.
     * @param key The UUID key of the entry to write.
     * @param value The data to store at the UUID key location.
     */
    public void writeDataStoreValue(UUID key, String value) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }
}
