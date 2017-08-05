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
package com.vitembp.services.imaging;

import com.vitembp.services.FilenameGenerator;
import com.vitembp.services.video.FrameProcessor;
import com.vitembp.services.video.VideoFileInfo;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.apache.logging.log4j.LogManager;

/**
 * Provides frame processors for synchronization frame diagnostics output.
 */
public class SyncDiagFrameProcessor {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * Returns a frame processor which annotates video frames with diagnostic
     * details about the sync frame finding algorithm.
     * @param syncFrames The list of synchronization frames to return.
     * @param targetValue The target value of the channel to be considered a
     * sync frame.
     * @return A FrameProcessor instance that will annotate frames with
     * diagnostics information from the sync frame finding algorithm.
     */
    public static FrameProcessor getFrameProcessor(final List<Integer> syncFrames, final double targetValue) {
        return new FrameProcessor() {
            @Override
            public void buildDiagOverlay(HistogramList histograms, FilenameGenerator fileGenerator, Path inputDir, VideoFileInfo inputFileInfo) throws IOException {
                // go through histograms and annotate the corrosponding frames
                for (int item = 0; item < histograms.size(); item++){
                    // get current histogram and load in associated frame
                    Histogram histogram = histograms.get(item);
                    Path file = inputDir.resolve(fileGenerator.getPath(item + 1));
                    DataOverlayBuilder builder = new DataOverlayBuilder(file);

                    // add text of the format: "R: ### G: ### B: ###"
                    StringBuilder rgbData = new StringBuilder();
                    rgbData.append("R: ");
                    rgbData.append(Math.round(histogram.getRedBrightness()));
                    rgbData.append(" G: ");
                    rgbData.append(Math.round(histogram.getGreenBrightness()));
                    rgbData.append(" B: ");
                    rgbData.append(Math.round(histogram.getBlueBrightness()));
                    builder.addText(rgbData.toString(), 5, 20);

                    // dispaly the channel value threshold to be considered
                    // a sync frame
                    builder.addText("Frame threshold: " + Math.round(targetValue), 5, 40);
                    
                    // display if this was determined to be a sync frame
                    if (syncFrames.contains(item)) {
                        builder.addText("Sync frame: Yes", 5, 60);
                    } else {
                        builder.addText("Sync frame: No", 5, 60);
                    }
                    
                    // display bar graphs for the sync frame values
                    builder.addVerticalProgressBar(
                            Math.round(histogram.getRedBrightness()) / 255.0f,
                            5,
                            80,
                            25,
                            140);
                    
                    builder.addVerticalProgressBar(
                            Math.round(histogram.getGreenBrightness()) / 255.0f,
                            30,
                            80,
                            50,
                            140);
                    
                    builder.addVerticalProgressBar(
                            Math.round(histogram.getBlueBrightness()) / 255.0f,
                            55,
                            80,
                            75,
                            140);
                    
                    // label bars
                    builder.addText("R", 7, 160);
                    builder.addText("G", 32, 160);
                    builder.addText("B", 58, 160);
                    
                    // save file, deleting existing first
                    File saveLocation = file.toFile();
                    if (saveLocation.exists()) {
                        saveLocation.delete();
                    } else {
                        LOGGER.error(
                                "Frame file " + 
                                saveLocation.toString() + 
                                " did not exist trying to overwrite in place.");
                    }
                    builder.saveImage(saveLocation);
                }
            }
        };
    }
}
