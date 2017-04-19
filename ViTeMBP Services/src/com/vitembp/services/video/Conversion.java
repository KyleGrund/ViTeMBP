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

import com.vitembp.services.FilenameGenerator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class providing functions to convert necessary formats. 
 */
public class Conversion {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Extracts frames from a video file to a destination directory.
     * @param source The video from which to extract frames.
     * @param destination The destination directory for the extracted frames.
     * @param start The starting frame to extract.
     * @param count The number of frames to extract.
     * @param nameGenerator A function which will generate a file name for a
     * given frame number.
     * @throws java.io.IOException If there is an IOException processing the
     * video file.
     */
    public static void extractFrames(String source, Path destination, int start, int count, FilenameGenerator nameGenerator) throws IOException {
        // build the FFmpeg process that will extract the frames        
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i",
                source,
                "-vframes",
                Integer.toString(count),
                "-v",
                "quiet",
                nameGenerator.getFFmpegString()
        );
        
        pb.directory(destination.toFile());
        
        LOGGER.info("Executing command: " + Arrays.toString(pb.command().toArray()));
        
        // execute the command
        Process proc = pb.start();

        try {
            // execute and wait for the command
            int result = proc.waitFor();
            if (result != 0) {
                // result is exit level, log anything > 0 as an error
                LOGGER.error("Frame extraction completed with exit level: " + Integer.toString(result));
                BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                String line = br.readLine();
                while (line != null) {
                    LOGGER.error(line);
                    line = br.readLine();
                }
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted while waiting for frame extraction process completion.", ex);
        }
    }
    
    /**
     * Creates a video from a series of images in a directory.
     * @param source The directory containing source images.
     * @param destination The destination file for the composed video.
     * @param nameGenerator A function which will generate a file name for a
     * given frame number.
     * @param framerate The frame rate of the target in frames per second.
     */
    public static void assembleFrames(Path source, Path destination, FilenameGenerator nameGenerator, double framerate) throws IOException {
        // build the FFmpeg process that will assemble the frames        
        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg",
            "-framerate",
            Double.toString(framerate),
            "-i",
            source.toString() + "\\" + nameGenerator.getFFmpegString(),
            destination.toString());
        
        LOGGER.info("Executing command: " + Arrays.toString(pb.command().toArray()));
        
        // execute the FFmpeg program
        Process proc = pb.start();

        try {
            // execute and wait for the command
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line = br.readLine();
            while (line != null) {
                LOGGER.error(line);
                line = br.readLine();
            }
            
            int result = proc.waitFor();
            if (result != 0) {
                // result is exit level, log anything > 0 as an error
                LOGGER.error("Frame assembly completed with exit level: " + Integer.toString(result));
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted while waiting for frame assembly process completion.", ex);
        }
    }
}
