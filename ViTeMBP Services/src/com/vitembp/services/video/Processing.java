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
package com.vitembp.services.video;

import com.vitembp.services.imaging.SyncDiagFrameProcessor;
import com.vitembp.services.ApiFunctions;
import com.vitembp.services.FilenameGenerator;
import com.vitembp.services.imaging.Histogram;
import com.vitembp.services.imaging.HistogramList;
import com.vitembp.services.interfaces.AmazonSimpleStorageService;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Functions for processing of video files.
 */
public class Processing {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * The number of standard deviations out in brightness a color channel frame
     * must be to be considered an outlier and thus a synchronization frame.
     */
    private static final double OUTLIER_DEVIATIONS = 2;
    
    /**
     * Finds the frames which have an outlier brightness in the given color.
     * channel.
     * @param videoFile The file to examine.
     * @param channel The color channel to evaluate.
     * @param fileGenerator The filename generator which defines the names of
     * sequential files to use when processing frames.
     * @param overlayOutput The filename to output the video with overlay data.
     * @return The frames which have an outlier brightness in the given color.
     * @throws java.io.IOException If there is an IOException processing the
     * video file.
     */
    public static List<Integer> findChannelSyncFrames(String videoFile, ApiFunctions.COLOR_CHANNELS channel, FilenameGenerator fileGenerator, Path overlayOutput) throws IOException {
        // build a temporary directory for images
        Path tempDir = Files.createTempDirectory("vitempb");
        
        // creates the function which picks the color channel on the histograms
        // when processing to find the synchronization frames
        Function<Histogram, Double> selector;
        switch (channel) {
            case RED:
                selector = (histogram) -> histogram.getRedBrightness();
                break;
            case GREEN:
                selector = (histogram) -> histogram.getGreenBrightness();
                break;
            case BLUE:
                selector = (histogram) -> histogram.getBlueBrightness();
                break;
            default:
                throw new IllegalArgumentException("Unknown color channel specified.");
        }
        
        // crate images with ffmpeg
        Conversion.extractFrames(videoFile, tempDir, 0, 300, fileGenerator);
        
        // create histogram list from images
        HistogramList histograms = HistogramList.loadFromDirectory(tempDir, fileGenerator);
        
        // get stats for diagnostics
        double stdDev = histograms.getPosStdev(selector);
        double average = histograms.getAverage(selector);
        
        // return outliers which are the sync frames
        List<Integer> outliers = histograms.getPositiveOutliers(selector, OUTLIER_DEVIATIONS);
        
        // if diagnostic data requested build it now
        if (overlayOutput != null) {
            VideoFileInfo info = new VideoFileInfo(videoFile);
            Processing.buildDiagOverlay(histograms, (OUTLIER_DEVIATIONS * stdDev) + average, outliers, fileGenerator, tempDir, overlayOutput, info);
            // uploadPublic to S3
            AmazonSimpleStorageService s3 = new AmazonSimpleStorageService("www-vitembp-com");
            s3.uploadPublic(overlayOutput.toFile(), "debug/" + overlayOutput.getFileName());
        }
        
        deleteTree(tempDir);
        
        // return synchronization frames
        return outliers;
    }
    
    /**
     * Processes a video with a capture.
     * @param capture The capture containing data to overlay on the video.
     * @param videoFile The video file to overlay data on.
     * @throws java.io.IOException If there is an IOException processing the
     * video file.
     */
    public static void processVideo(UUID capture, String videoFile) throws IOException {
        LOGGER.info("Processing: " + videoFile);
        List<Integer> frames = Processing.findChannelSyncFrames(videoFile, ApiFunctions.COLOR_CHANNELS.GREEN, FilenameGenerator.PNG_NUMERIC_OUT);
        
        if (frames.isEmpty()) {
            throw new IOException("No sync frames detected.");
        }
        
        
    }

    /**
     * Deletes the directory including all subdirectories and files.
     * @param tempDir The directory to delete.
     * @throws IOException If an exception occurs during deletion.
     */
    private static void deleteTree(Path tempDir) throws IOException {
        // recursively delete all files in temp directory
        Files.walkFileTree(tempDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException
            {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e)
                    throws IOException
            {
                if (e == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    // directory iteration failed
                    throw e;
                }
            }
        });
    }
    
    /**
     * Finds the frames which have an outlier brightness in the given color.
     * channel.
     * @param videoFile The file to examine.
     * @param channel The color channel to evaluate.
     * @param fileGenerator The filename generator which defines the names of
     * sequential files to use when processing frames.
     * @return The frames which have an outlier brightness in the given color.
     * @throws java.io.IOException If there is an IOException processing the
     * video file.
     */
    public static List<Integer> findChannelSyncFrames(String videoFile, ApiFunctions.COLOR_CHANNELS channel, FilenameGenerator fileGenerator) throws IOException {
        return Processing.findChannelSyncFrames(videoFile, channel, fileGenerator, null);
    }

    /**
     * Builds a video from frames with overlaid histogram data.
     * @param histograms The histograms for the frames.
     * @param targetValue The target value of the channel to be considered a
     * sync frame.
     * @param syncFrames The list of sync frames that were found by the
     * algorithm.
     * @param fileGenerator The filename generator which defines the names of
     * sequential files to use when processing frames.
     * @param overlayOutput The output video file.
     */
    private static void buildDiagOverlay(HistogramList histograms, double tagetValue, List<Integer> syncFrames, FilenameGenerator fileGenerator, Path inputDir, Path overlayOutput, VideoFileInfo inputFileInfo) throws IOException {
        SyncDiagFrameProcessor.getFrameProcessor(syncFrames, tagetValue).buildDiagOverlay(histograms, fileGenerator, inputDir, inputFileInfo);
        
        // build the output video for the overlaid frames
        Conversion.assembleFrames(inputDir, overlayOutput, fileGenerator, inputFileInfo.getFrameRate());
    }
}
