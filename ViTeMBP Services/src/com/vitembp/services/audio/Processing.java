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

import com.vitembp.services.video.VideoFileInfo;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Functions for processing audio.
 */
public class Processing {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Finds sync frames based on the audio signal.
     * @param sourceFile The source wave file.
     * @param signalFrequency The sync signal frequency.
     * @return A list of sync frames where the audio signal was detected.
     * @throws java.io.IOException
     */
    public static List<Integer> findSyncFrames(String sourceFile, double signalFrequency) throws IOException {
        // create a temporary file for the output
        Path localTempOutput = Files.createTempFile("vitembp", ".wav");
        localTempOutput.toFile().delete();
        
        // extract audio
        com.vitembp.services.video.Conversion.extractWaveAudio(sourceFile, localTempOutput.toString());
        
        // get data frame chunks of data
        File fileIn = localTempOutput.toFile();
        
        // provides vidoe file information, such as frame rate
        VideoFileInfo videoInfo = new VideoFileInfo(new File(sourceFile));
        
        // the list of values for each frame
        List<Double> frameValues = new ArrayList<>();
        
        // the number of frames to process (limit to 30 seconds)
        int frameLimit = (int)Math.round(videoInfo.getFrameRate() * 30);
        
        // the lenght of the audio signal in frames
        double signalTimeInSeconds = 2.0;
        int signalFrameLength = (int)Math.round(videoInfo.getFrameRate() * signalTimeInSeconds);
        
        // somePathName is a pre-existing string whose value was
        // based on a user selection.
        try {
            // create audio streasm
            AudioInputStream audioInputStream = 
                    AudioSystem.getAudioInputStream(fileIn);
            
            // extract important information about audio format
            AudioFormat format = audioInputStream.getFormat();
            int bytesPerFrame = format.getFrameSize();
            int channels = format.getChannels();
            int bytesPerChannel = bytesPerFrame / channels;
            float audioSampleRate = format.getSampleRate();
            int sampleSizeBits = format.getSampleSizeInBits();
            boolean isBigEndian = format.isBigEndian();
            
            // calculate video/audio related values
            int samplesPerVideoFrame = (int)Math.round(audioSampleRate/videoInfo.getFrameRate());
            int audioBytesPerVideoFrame = samplesPerVideoFrame * bytesPerFrame;

            // find the fft bin to examine for the desired frequency
            int targetBin = samplesPerVideoFrame - (int)Math.round((signalFrequency / (audioSampleRate / samplesPerVideoFrame)) + 1);
            
            // build input buffer
            byte[] input = new byte[audioBytesPerVideoFrame];
            
            // holds the number of bytes read to check for partial data
            int bytesRead;

            // create FFT engine
            org.jtransforms.fft.DoubleFFT_1D fft = new org.jtransforms.fft.DoubleFFT_1D(samplesPerVideoFrame);

            // holds audio data for fft processing
            double[] fftData = new double[samplesPerVideoFrame*2];

            // continue if we read bytes from the file
            while ((bytesRead = audioInputStream.read(input)) != -1) {
                // do not process partial data sample
                if (bytesRead < audioBytesPerVideoFrame) {
                    break;
                }
                
                // seed fft data
                for (int i = 0; i < samplesPerVideoFrame; i++) {
                    fftData[i * 2] = getAudioSample(input, i, 0, bytesPerChannel, sampleSizeBits, channels, isBigEndian);
                    fftData[i * 2 + 1] = 0;
                }

                // perform the FFT
                fft.realForwardFull(fftData);

                // calculate magnitude of bin for frame
                double value2 = Math.sqrt(Math.pow(fftData[targetBin], 2) + Math.pow(fftData[targetBin + 1], 2));
                frameValues.add(value2);

                // only check the beginning of the audio file for signal
                if (frameValues.size() >= frameLimit) {
                    break;
                }
            }
        } catch (IOException | UnsupportedAudioFileException e) {
          LOGGER.error("Unexpected error performing FFT on video audio.", e);
        }
        
        // find the sync frames
        Set<Integer> syncFrames = findSyncFramesByRunLength(frameValues, signalFrameLength);
        
        // remove temp file
        localTempOutput.toFile().delete();
        
        // return frames
        return new ArrayList<>(syncFrames);
    }

    /**
     * Finds sync frames by finding the maximum value and then the longest run
     * within a percentage of this value of a reasonable length.
     * @param frameValues The frame FFT values.
     * @param signalFrameLength The length of the signal to find.
     * @return A set containing the frame which was found, if any.
     */
    private static Set<Integer> findSyncFramesByRunLength(List<Double> frameValues, int signalFrameLength) {
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
        Map<Integer, Integer> values = new HashMap<>();
        
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
            int largestRun = values.keySet().stream().sorted(Integer::max).findFirst().get();
            
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
    private static Set<Integer> findSyncFramesByFirstCloseToMax(List<Double> frameValues) {
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
    
    /**
     * Calculates the value of the selected audio sample from the give data.
     * @param data The sampled audio data.
     * @param offset The offset in audio frames.
     * @param channel The channel in the wave data to calculate.
     * @param bytesPerSample The number of bytes per data sample.
     * @param sampleSizeBits The size of the samples in bits.
     * @param channelCount The total number of audio channels.
     * @param isBigEndian Indicates whether the data is stored big endian.
     * @return The calculated audio sample value.
     */
    private static double getAudioSample(byte[] data, int offset, int channel, int bytesPerSample, int sampleSizeBits, int channelCount, boolean isBigEndian) {
        // check the audio channel is in range
        if (channel >= channelCount) {
            throw new IllegalArgumentException("Selected channel was greater than number of chanels in the data.");
        }
        
        // calculate the index of the selected sample
        int index = offset * bytesPerSample * channelCount + (channel * bytesPerSample);
        
        // check the sample is contained in the data
        if (index + bytesPerSample >= data.length) {
            throw new IllegalArgumentException("Indexed sample is out of range.");
        }
        
        // holds the final calculated audio sample value
        double value = 0;
        
        // the last(end) byte which must be processed seperately to handle the
        // sign-bit if 
        int lastByte = 0;
        
        // single byte (8-bit) audio sampling is stored as an unsigned byte
        if (bytesPerSample == 1) {
            return data[index] & 0x00FF;
        }
        
        // all other audio is stored as a signed value, so the most significant
        // byte must be processed to interpret the MSB as a sign bit
        // handle endianess
        if (isBigEndian) {
            // handle non MSBs as unsigned data
            for (int i = bytesPerSample - 1; i > 0; i--) {
                long sample = ((long)(data[index + i] & 0x00FF)) << (8 * i);
                value += sample;
            }
            
            // save MSB for processing below
            lastByte = (int)(data[index] & 0x00FF);
        } else {
            // handle non MSBs as unsigned data
            for (int i = 0; i < bytesPerSample - 1; i++) {
                long sample = ((long)(data[index + i] & 0x00FF)) << (8 * i);
                value += sample;
            }
            
            // save MSB for processing below
            lastByte = (int)(data[index + (bytesPerSample - 1)] & 0x00FF);
        }
        
        // handle last byte may not be 'full' if the sample size is not
        // evenly divisible by 8bits
        // shift the MSB to the lowest bit (ones place)
        int shiftLen = ((sampleSizeBits - 1) % 8);
        int isNeg = lastByte >> shiftLen;
        
        // create a mask to extract all non MSB bits
        int mask = 0x00FF;
        mask >>= (8 - shiftLen);
        
        // get the value without the sign bit using the mask
        long lastVal = lastByte & mask;
        
        // shift the value to the appropriate bit in the long
        lastVal <<= 8 * (bytesPerSample - 1);
        
        // add the value to the audio sample data
        value += lastVal;
        
        // negate if the sign bit was a 1
        if (isNeg == 1) {
            value *= -1;
        }
        
        // return the final extracted value
        return value;
    }
}
