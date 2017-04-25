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

import com.vitembp.services.ApiFunctions;
import com.vitembp.services.FilenameGenerator;
import com.vitembp.services.imaging.DataOverlayBuilder;
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
        Conversion.extractFrames(videoFile, tempDir, 1, 300, fileGenerator);
        
        // create histogram list from images
        HistogramList histograms = HistogramList.loadFromDirectory(tempDir, fileGenerator);
        
        
        // get stats for diagnostics
        double maxDev = histograms.getMaxDev(selector);
        double stdDev = histograms.getPosStdev(selector);
        
        System.out.println("Std deviation: " + Double.toString(stdDev));
        System.out.println("Max deviation: " + Double.toString(maxDev));
        
        // return outliers which are the sync frames
        List<Integer> outliers = histograms.getPositiveOutliers(selector, OUTLIER_DEVIATIONS);
        
        // if diagnostic data requested build it now
        if (overlayOutput != null) {
            VideoFileInfo info = new VideoFileInfo(videoFile);
            Processing.buildDiagOverlay(histograms, fileGenerator, tempDir, overlayOutput, info.getFrameRate());
            // uploadPublic to S3
            AmazonSimpleStorageService s3 = new AmazonSimpleStorageService("vitembp.kylegrund.com");
            s3.uploadPublic(overlayOutput.toFile(), "debug/" + overlayOutput.getFileName());
        }
        
        deleteTree(tempDir);
        
        // return synchronization frames
        return outliers;
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
     * @param fileGenerator The filename generator which defines the names of
     * sequential files to use when processing frames.
     * @param overlayOutput The output video file.
     */
    private static void buildDiagOverlay(HistogramList histograms, FilenameGenerator fileGenerator, Path inputDir, Path overlayOutput,  double frameRate) throws IOException {
        // build a temporary directory for images
        Path tempDir = Files.createTempDirectory("vitempb");
        
        for (int item = 0; item < histograms.size(); item++){
            Histogram histogram = histograms.get(item);
            Path file = fileGenerator.getPath(item + 1);
            DataOverlayBuilder builder = new DataOverlayBuilder(inputDir.resolve(file));
            
            // add text of the format: "R: ### G: ### B: ###"
            StringBuilder rgbData = new StringBuilder();
            rgbData.append("R: ");
            rgbData.append(Math.round(histogram.getRedBrightness()));
            rgbData.append(" G: ");
            rgbData.append(Math.round(histogram.getGreenBrightness()));
            rgbData.append(" B: ");
            rgbData.append(Math.round(histogram.getBlueBrightness()));
            builder.addText(rgbData.toString(), 5, 20);
            
            // save file
            builder.saveImage(tempDir.resolve(file).toFile());
        }
        
        // build the output video for the overlaid frames
        Conversion.assembleFrames(tempDir, overlayOutput, fileGenerator, frameRate);
        
        // delete temporary files
        Processing.deleteTree(tempDir);
    }
}
