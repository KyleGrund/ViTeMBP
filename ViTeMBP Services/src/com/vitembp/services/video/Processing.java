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
import com.vitembp.services.imaging.Histogram;
import com.vitembp.services.imaging.HistogramList;
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
    private static final double OUTLIER_DEVIATIONS = 5;
    
    /**
     * Finds the frames which have an outlier brightness in the given color.
     * channel.
     * @param videoFile The file to examine.
     * @param channel The color channel to evaluate.
     * @return The frames which have an outlier brightness in the given color.
     * @throws java.io.IOException If there is an IOException processing the
     * video file.
     */
    public static List<Integer> findChannelSyncFrames(Path videoFile, ApiFunctions.COLOR_CHANNELS channel) throws IOException {       
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
        Conversion.extractFrames(videoFile, tempDir, 1, 300, FilenameGenerator.PNG_NUMERIC_OUT);
        
        // create histogram list from images
        HistogramList histograms = HistogramList.loadFromDirectory(tempDir, FilenameGenerator.PNG_NUMERIC_OUT);
        
        // return outliers which are the sync frames
        List<Integer> outliers = histograms.getPositiveOutliers(selector, OUTLIER_DEVIATIONS);
        
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
        
        // return synchronization frames
        return outliers;
    }
}
