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
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
     * Extracts waveform audio from a video file.
     * @param source The video from which to extract frames.
     * @param destination The destination file for the extracted audio.
     * @throws java.io.IOException If there is an IOException processing the
     * video file.
     */
    public static void extractWaveAudio(String source, String destination) throws IOException {
        // build the FFmpeg process that will extract the audio        
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i",
                source,
                "-vn",
                "-f",
                "wav",
                "-v",
                "quiet",
                destination
        );
        
        LOGGER.info("Executing command: " + Arrays.toString(pb.command().toArray()));
        
        // execute the command
        Process proc = pb.start();

        try {
            // execute and wait for the command
            int result = proc.waitFor();
            if (result != 0) {
                // result is exit level, log anything > 0 as an error
                LOGGER.error("Waveform audio extraction completed with exit level: " + Integer.toString(result));
                BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                String line = br.readLine();
                while (line != null) {
                    LOGGER.error(line);
                    line = br.readLine();
                }
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted while waiting for waveform audio extraction process completion.", ex);
        }
    }
    
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
        VideoFileInfo info = new VideoFileInfo(Paths.get(source).toFile());
        
        double startTime = ((double)start) / info.getFrameRate();
        
        // build the FFmpeg process that will extract the frames        
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-ss",
                Double.toString(startTime),
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
     * @throws java.io.IOException If an exception occurs reading or writing to
     * files while assembly the frames.
     */
    public static void assembleFrames(Path source, Path destination, FilenameGenerator nameGenerator, double framerate) throws IOException {
        // check that the output file doesn't already exist
        if (destination.toFile().exists()) {
            throw new IOException("Output file already exists: " + destination.toString());
        }
        
        // build the FFmpeg process that will assemble the frames        
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-framerate",
                Double.toString(framerate),
                "-i",
                source.toString() + File.separator + nameGenerator.getFFmpegString(),
                "-vf",
                "format=yuvj420p",
                destination.toString());
        
        LOGGER.info("Executing command: " + Arrays.toString(pb.command().toArray()));
        
        // execute the FFmpeg program
        Process proc = pb.start();

        try {
            // execute and wait for the command
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line = br.readLine();
            while (proc.isAlive() && line != null) {
                LOGGER.trace(line);
                line = br.readLine();
            }
            
            int result = proc.waitFor();
            if (result != 0) {
                // result is exit level, log anything > 0 as an error
                LOGGER.error("Frame assembly completed with exit level: " + Integer.toString(result));
                BufferedReader er = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                String erline = er.readLine();
                while (line != null) {
                    LOGGER.error(erline);
                    line = er.readLine();
                }
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted while waiting for frame assembly process completion.", ex);
        }
    }
    
    /**
     * This method concatenates the two specified videos.
     * @param destination The video to append to.
     * @param toAppend The video to append.
     */
    public static void combineVideos(Path destination, Path toAppend) throws IOException {
        // get two temp files
        Path outputPath = destination.getParent();

        Path fileList = outputPath.resolve("files.txt");
        for (int i = 0; Files.exists(fileList); i++) {
            fileList = outputPath.resolve(Integer.toString(i) + "files.txt");
        }
        
        // get the video file extension
        String[] splitName = destination.toString().split("\\.");
        String extension = splitName[splitName.length - 1];
        
        Path vidOut = outputPath.resolve("out." + extension);
        for (int i = 0; Files.exists(vidOut); i++) {
            vidOut = outputPath.resolve(Integer.toString(i) + "out." + extension);
        }
        
        // write files list
        try (BufferedWriter writer = Files.newBufferedWriter(fileList)) {
            writer.write("file '" + destination.toString() + "'");
            writer.newLine();
            writer.write("file '" + toAppend.toString() + "'");
            writer.newLine();
        }
        
        // build the FFmpeg process that will assemble the frames        
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-f",
                "concat",
                "-safe",
                "0",
                "-i",
                fileList.toString(),
                "-c",
                "copy",
                vidOut.toString());
        
        LOGGER.info("Executing command: " + Arrays.toString(pb.command().toArray()));
        
        // execute the FFmpeg program
        Process proc = pb.start();

        try {
            // execute and wait for the command
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line = br.readLine();
            while (proc.isAlive() && line != null) {
                LOGGER.trace(line);
                line = br.readLine();
            }
            
            int result = proc.waitFor();
            if (result != 0) {
                // result is exit level, log anything > 0 as an error
                LOGGER.error("Video concatenation completed with exit level: " + Integer.toString(result));
                BufferedReader er = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                String erline = er.readLine();
                while (line != null) {
                    LOGGER.error(erline);
                    line = er.readLine();
                }
                return;
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted while waiting for video concatenation process completion.", ex);
            return;
        }
        
        // delete original files and rename output to destination
        Files.delete(destination);
        Files.delete(toAppend);
        Files.move(vidOut, destination);
        
        // delete files list
        Files.delete(fileList);
    }

    public static void copyAudio(Path sourceFile, Path destFile) throws IOException {
        // get two temp files
        Path outputPath = destFile.getParent();
        
        // get the video file extension
        String[] splitName = destFile.toString().split("\\.");
        String extension = splitName[splitName.length - 1];
        
        Path vidOut = outputPath.resolve("out." + extension);
        for (int i = 0; Files.exists(vidOut); i++) {
            vidOut = outputPath.resolve(Integer.toString(i) + "out." + extension);
        }
        
        // build the FFmpeg process that will assemble the frames        
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i",
                destFile.toString(),
                "-i",
                sourceFile.toString(),
                "-c",
                "copy",
                "-map",
                "0:v:0",
                "-map",
                "1:a:0",
                vidOut.toString());
        
        LOGGER.info("Executing command: " + Arrays.toString(pb.command().toArray()));
        
        // execute the FFmpeg program
        Process proc = pb.start();

        try {
            // execute and wait for the command
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line = br.readLine();
            while (proc.isAlive() && line != null) {
                LOGGER.trace(line);
                line = br.readLine();
            }
            
            int result = proc.waitFor();
            if (result != 0) {
                // result is exit level, log anything > 0 as an error
                LOGGER.error("Video audio copy completed with exit level: " + Integer.toString(result));
                BufferedReader er = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                String erline = er.readLine();
                while (line != null) {
                    LOGGER.error(erline);
                    line = er.readLine();
                }
                return;
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted while waiting for video audio copy process completion.", ex);
            return;
        }
        
        // delete the original without audio and rename output to destination
        Files.delete(destFile);
        Files.move(vidOut, destFile);
    }
}
