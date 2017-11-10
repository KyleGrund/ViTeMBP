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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * This class contains algorithms for finding synchronization frames for FFT
 * amplitude data.
 */
class SyncDetectionAlgorithms {
    /**
     * Finds sync frames by finding the average value of a sliding window and
     * selecting the maximum average value.
     * @param frameValues The frame FFT values.
     * @param signalFrameLength The length of the signal to find.
     * @return A set containing the frame which was found, if any.
     */
    static Set<Integer> findSyncFramesByAveragingWindow(List<Double> frameValues, int signalFrameLength) {
        // holds the sync frame to return
        Set<Integer> syncFramesFound = new HashSet<>();
        
        // cannot operate if the data length is less than the window
        if (frameValues.size() < signalFrameLength) {
            return syncFramesFound;
        }
        
        // stores a sorted map of window average value to start frame
        TreeMap<Double, Integer> averages = new TreeMap<>(Double::compare);
        
        // step through all values
        for (int startFrame = 0; startFrame < frameValues.size() - signalFrameLength; startFrame++) {
            // average the window
            double average = 0.0;
            for (int offset = 0; offset < signalFrameLength; offset++) {
                average += frameValues.get(startFrame + offset) / signalFrameLength;
            }
            
            // save the value if it doesn't collide, this prefers earlier frames
            if (!averages.containsKey(average)) {
                averages.put(average, startFrame);
            }
        }
        
        // find the largest average
        syncFramesFound.add(averages.lastEntry().getValue());
        
        return syncFramesFound;
    }
    
    /**
     * Finds sync frames by finding the maximum value and then the longest run
     * within a percentage of this value of a reasonable length.
     * @param frameValues The frame FFT values.
     * @param signalFrameLength The length of the signal to find.
     * @return A set containing the frame which was found, if any.
     */
    static Set<Integer> findSyncFramesByRunLength(List<Double> frameValues, int signalFrameLength) {
        Set<Integer> syncFramesFound = new HashSet<>();
        
        // find peak FFT value
        double max = Double.MIN_VALUE;
        for (int i = 0; i < frameValues.size(); i++) {
            double val = frameValues.get(i);
            if (val > max) {
                max = val;
            }
        }

        // find first frame with the next 25% of pulse is within %20 of peak
        double target = 0.8 * max;
        
        // the target minimum run length, serves as a reality check
        int minRunLength = (int)Math.round(signalFrameLength * 0.25);
        
        // the target maximum run length, serves as a reality check
        int maxRunLength = (int)Math.round(signalFrameLength * 1.25);
        
        // stores a map of run length to first frame the length was found at
        TreeMap<Integer, Integer> values = new TreeMap<>(Integer::compare);
        
        // step through all values
        for (int startFrame = 0; startFrame < frameValues.size(); startFrame++) {
            // if the value is in the target check the run length
            if (frameValues.get(startFrame) >= target) {
                for (int runLength = 1; runLength < frameValues.size(); runLength++) {
                    if (frameValues.get(startFrame + runLength) < target) {
                        // record run length if not already found
                        // this prefers earlier frames of same length
                        if (!values.containsKey(runLength)) {
                            values.put(runLength, startFrame);
                        }
                        
                        // no need to check before this value
                        startFrame = startFrame + runLength + 1;
                        break;
                    }
                }
            }
        }
        
        // if we found any valid runs
        if (values.size() > 0) {
            // find the longest run, we only check the longest to be in range,
            // if it is not we likely either have no signal or our signal to
            // noise ratio is too low
            int largestRun = values.lastEntry().getValue();
            
            // if the run was within the sanity check range, record it
            if (largestRun > minRunLength && largestRun < maxRunLength) {
                syncFramesFound.add(values.get(largestRun));
            }
        }
        
        return syncFramesFound;
    }
    
    /**
     * Finds sync frames by finding the maximum value and then the first frame
     * within a percentage of this value.
     * @param frameValues The frame FFT values.
     * @return A set containing the frame which was found, if any.
     */
    static Set<Integer> findSyncFramesByFirstCloseToMax(List<Double> frameValues) {
        Set<Integer> syncFramesFound = new HashSet<>();
        
        // find peak FFT value
        double max = Double.MIN_VALUE;
        for (int i = 0; i < frameValues.size(); i++) {
            double val = frameValues.get(i);
            if (val > max) {
                max = val;
            }
        }
        
        // find first value within %10 of peak
        double target = 0.9 * max;
        int firstFrame = -1;
        for (int i = 0; i < frameValues.size(); i++) {
            if (frameValues.get(i) >= target) {
                firstFrame = i;
                break;
            }
        }
        
        // add this frame to the list of sync frames
        syncFramesFound.add(firstFrame);
        
        return syncFramesFound;
    }
}
