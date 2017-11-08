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

import com.meapsoft.FFT;
import com.vitembp.services.video.VideoFileInfo;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * Functions for processing audio.
 */
public class Processing {
    /**
     * Finds sync frames based on the audio signal.
     * @param sourceFile The source wave file.
     * @param signalFrequency The sync signal frequency.
     * @return A list of sync frames where the audio signal was detected.
     * @throws java.io.IOException
     */
    public static List<Integer> findSyncFrames(String sourceFile, double signalFrequency) throws IOException {
        List<Integer> syncFrames = new ArrayList<>();
        
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
        List<Double> frameValues = new ArrayList<Double>();
        
        int frameCount = 0;
        
        // somePathName is a pre-existing string whose value was
        // based on a user selection.
        try {
            AudioInputStream audioInputStream = 
                    AudioSystem.getAudioInputStream(fileIn);
            AudioFormat format = audioInputStream.getFormat();
            int bytesPerFrame = format.getFrameSize();
            float frameRate = format.getFrameRate();
            int channels = format.getChannels();
            int bytesPerChannel = bytesPerFrame / channels;
            float audioSampleRate = format.getSampleRate();
            int sampleSizeBits = format.getSampleSizeInBits();
            boolean isBigEndian = format.isBigEndian();
            
            int samplesPerVideoFrame = (int)Math.round(audioSampleRate/videoInfo.getFrameRate());
            int audioBytesPerVideoFrame = samplesPerVideoFrame * bytesPerFrame;
            
            // 2 ^ floor(log base 2(samplesPerVideoFrame))
            int fftSize = (int)Math.pow(2, Math.floor(Math.log(samplesPerVideoFrame) / Math.log(2)));
            
            // find the fft bin to examine
            float binWidth = frameRate / 2;
            int targetBin = (int)Math.round(signalFrequency / binWidth);
            
            // build input buffer
            byte[] input = new byte[audioBytesPerVideoFrame];
            
            try {
                int bytesRead;
                FFT fft = new FFT(fftSize);
                double[] real = new double[fftSize];
                double[] imaginary = new double[fftSize];
                
                // continue if we read bytes from the file
                while ((bytesRead = audioInputStream.read(input)) != -1) {
                    frameCount++;
                    // seed fft data
                    // if we didn't read a full video frame of audio, handle
                    // the needed offset
                    int framesRead = bytesRead / bytesPerFrame;
                    int frameOffset = 0;
                    if (framesRead < fftSize) {
                        frameOffset = fftSize - framesRead;
                        for (int i = 0; i < frameOffset; i++) {
                            real[i] = 0;
                            imaginary[i] = 0;
                        }
                    }
                    
                    for (int i = frameOffset; i < fftSize; i++) {
                        real[i] = 0;
                        imaginary[i] = 0;
                        
                        // average data from channels
                        for (int j = 0; j < channels; j++) {
                            real[i] += getAudioSample(input, i - frameOffset, j, bytesPerChannel, sampleSizeBits, channels, isBigEndian) / channels;
                        }
                    }
                    
                    // perform the FFT
                    fft.fft(real, imaginary);
                    
                    // calculate magnitude of bin for frame
                    double value = Math.sqrt(Math.pow(real[targetBin], 2) + Math.pow(imaginary[targetBin], 2));
                    
                    // store the frame value for later analysis
                    frameValues.add(value);
                }
            } catch (Exception ex) { 
              // Handle the error...
            }
        } catch (Exception e) {
          // Handle the error...
        }
        
        // find some average, mean, median, or mode?
        
        // find above average by ammount?
        
        // find peak frame?
        int peak = 0;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < frameValues.size(); i++) {
            double val = frameValues.get(i);
            if (val > max) {
                max = val;
                peak = i;
            }
        }
        syncFrames.add(peak);
        
        // find first within %20 of peak
        double target = 0.8 * max;
        int firstFrame = -1;
        for (int i = 0; i < frameValues.size(); i++) {
            if (frameValues.get(i) >= target) {
                firstFrame = i;
                break;
            }
        }
        syncFrames.add(firstFrame);
        
        // remove temp file
        localTempOutput.toFile().delete();
        
        // return frames
        return syncFrames;
    }
    
    private static double getAudioSample(byte[] data, int offset, int channel, int bytesPerSample, int sampleSizeBits, int channelCount, boolean isBigEndian) {
        if (channel >= channelCount) {
            throw new IllegalArgumentException("Selected channel was greater than number of chanels in the data.");
        }
        
        int index = offset * bytesPerSample * channelCount + (channel * bytesPerSample);
        
        if (index + bytesPerSample >= data.length) {
            throw new IllegalArgumentException("Indexed sample is out of range.");
        }
        
        double value = 0;
        int lastByte = 0;
        if (isBigEndian) {
            for (int i = bytesPerSample - 1; i > 0; i--) {
                long sample = ((long)(data[index + i] & 0x00FF)) << (8 * i);
                value += sample;
            }
            lastByte = (int)(data[index] & 0x00FF);
        } else {
            for (int i = 0; i < bytesPerSample - 1; i++) {
                long sample = ((long)(data[index + i] & 0x00FF)) << (8 * i);
                value += sample;
            }
            lastByte = (int)(data[index + (bytesPerSample - 1)] & 0x00FF);
        }
        
        // handle last byte
        int shiftLen = ((sampleSizeBits - 1) % 8);
        int isNeg = lastByte >> shiftLen;
        int mask = 0x00FF;
        mask >>= (8 - shiftLen);
        long lastVal = lastByte & mask;
        lastVal <<= 8 * (bytesPerSample - 1);
        value += lastVal;
        if (isNeg == 1) {
            value *= -1;
        }
        
        return value;
    }
}
