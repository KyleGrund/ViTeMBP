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

import java.util.List;

/**
 * Functions for processing audio.
 */
public class Processing {
    /**
     * Finds sync frames based on the audio signal.
     * @param sourceFile The source wave file.
     * @param frequency The frequency of the audio sync signal.
     * @param frameRate The frame rate of the video the audio was extracted from
     * to be used to calculate the frame location.
     * @return A list of sync frames where the audio signal was detected.
     */
//    public static List<Integer> findSyncFrames(String sourceFile, double frequency, double frameRate) {
//        
//    }
}