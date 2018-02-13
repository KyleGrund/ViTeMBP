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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Information class contains functions which will provide data on a 
 * video file.
 */
public class VideoFileInfo {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * The frame rate of the video in frames per second.
     */
    private double frameRate;
    
    /**
     * The duration of the video in seconds.
     */
    private double duration;
    
    /**
     * The horizontal resolution of the video in pixels.
     */
    private int horizontalResolution;
    
    /**
     * The vertical resolution of the video in pixels.
     */
    private int verticalResolution;
    
    /**
     * Initializes a new instance of the VideoFileInfo class.
     * @param file The file instance pointing to the video file to examine.
     */
    public VideoFileInfo(File file) {
        this(file.getAbsolutePath());
    }
    
    /**
     * Initializes a new instance of the VideoFileInfo class.
     * @param file The file instance pointing to the video file to examine.
     */
    public VideoFileInfo(String file) {
        // build the FFmpeg process that will examine the file       
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                file
        );
        
        List<String> response = new ArrayList<>();

        try {
            // build a process that will execute the FFmpeg command
            Process proc = pb.start();
        
            // execute and wait for the command
            int result = proc.waitFor();
            if (result != 0) {
                // result is exit level, log anything > 0 as an error
                LOGGER.error("File probe completed with exit level: " + Integer.toString(result));
                BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                String line = br.readLine();
                while (line != null) {
                    LOGGER.error(line);
                    response.add(line);
                    line = br.readLine();
                }
            } else {
                // read results from the process
                BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                    String line = br.readLine();
                    while (line != null) {
                        response.add(line);
                        line = br.readLine();
                    }
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted while waiting for process completion.", ex);
        } catch (IOException ex) {
            LOGGER.error("IO exception while creating process.", ex);
        }
        
        // parse and save response
        this.parseResponse(response);
    }
    
    /**
     * Gets the frame rate of the video in frames per second.
     * @return he frame rate of the video in frames per second. 
    */
    public double getFrameRate() {
        return this.frameRate;
    }
    
    /**
     * Gets the duration of the video in seconds.
     * @return The duration of the video in seconds.
     */
    public double getDuration() {
        return this.duration;
    }
    
    /**
     * Gets the horizontal resolution of the video in pixels.
     * @return The horizontal resolution of the video in pixels.
     */
    public int getHorizontalResolution() {
        return this.horizontalResolution;
    }
    
    /**
     * Gets the vertical resolution of the video in pixels.
     * @return The vertical resolution of the video in pixels.
     */
    public int getVerticalResolution() {
        return this.verticalResolution;
    }
    
    /**
     * Parses and saves data from the ffprobe output to member variables.
     * @param output The output from the ffprobe program.
     */
    private void parseResponse(List<String> output) {
        Iterator<String> lines = output.iterator();
        
        // find line staring with "Input #" which should represent the video
        // stream
        String line;
        do {
            if (!lines.hasNext()) {
                LOGGER.error("Unexpected end of output parsing stream input.");
                output.forEach((str) -> LOGGER.error(str));
                return;
            }
            
            line = lines.next();
        } while (!line.startsWith("Input #"));
        
        // find duration
        do {
            if (!lines.hasNext()) {
                LOGGER.error("Unexpected end of output parsing duration.");
                output.forEach((str) -> LOGGER.error(str));
                return;
            }
            
            line = lines.next();
        } while (!line.startsWith("  Duration"));
        
        // split the time in the format "HH:MM:SS.SS" about the ":"
        String[] times = line.split(",")[0].trim().split(" ")[1].split(":");        
        
        // parse the seconds, minutes, and hours to total seconds
        this.duration = Double.parseDouble(times[2]);
        this.duration += Double.parseDouble(times[1]) * 60.0;
        this.duration += Double.parseDouble(times[0]) * 60.0 * 60.0;
        
        // read main stream data from the next line
        if (!lines.hasNext()) {
            LOGGER.error("Unexpected end of output parsing video mode.");
            output.forEach((str) -> LOGGER.error(str));
            return;
        }
        line = lines.next();
        
        // split up the video data entry around commas 
        String[] values = line.split("Video:")[1].split(",");
        
        boolean foundResolution = false;
        
        Pattern findRes = Pattern.compile("^\\s\\d+x\\d+$");
        // find the entry with resolution
        for (String val : values) {
            if (val.contains("SAR") || findRes.matcher(val).find()) {
                String[] res = val.trim().split(" ")[0].split("x");
                this.horizontalResolution = Integer.parseInt(res[0]);
                this.verticalResolution = Integer.parseInt(res[1]);
                foundResolution = true;
                break;
            }
        }
        
        if (!foundResolution) {
            LOGGER.error("Resolution not found.");
            return;
        }
        
        boolean foundFPS = false;
        
        // find framerate
        for (String val : values) {
            if (val.contains("fps")) {
                this.frameRate = Double.parseDouble(val.trim().split(" ")[0]);
                foundFPS = true;
                break;
            }
        }
        
        if (!foundFPS) {
            LOGGER.error("Frame rate not found.");
        }
    }
}
