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
import com.vitembp.services.imaging.HistogramList;
import java.io.IOException;
import java.nio.file.Path;

/**
 * This class provides an interface to allow groups of frames to be processed
 * in place.
 */
public abstract class FrameProcessor {
    /**
     * Builds a video from frames with overlaid histogram data.
     * @param histograms The histograms for the frames.
     * @param fileGenerator The filename generator which defines the names of
     * sequential files to use when processing frames.
     * @param inputDir The directory containing the frames to edit.
     * @param inputFileInfo Info about the original video file.
     * @throws java.io.IOException If an IO Exception occurs during processing.
     */
    public abstract void buildDiagOverlay(HistogramList histograms, FilenameGenerator fileGenerator, Path inputDir, VideoFileInfo inputFileInfo) throws IOException;
}
