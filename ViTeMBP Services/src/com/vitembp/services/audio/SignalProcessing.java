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
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jtransforms.fft.DoubleFFT_1D;

/**
 * Class providing SignalProcessing functionality for WAV audio data.
 */
class SignalProcessing {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Averages the data by applying a sliding window of the specified width.
     * The last values will have a collapsing window instead of shortening the
     * returned data by the window width.
     * @param data The data to average.
     * @param  windowSize The size of the averaging window to apply.
     * @return The averaged data.
     */
    static List<Double> appplyAveragingWindow(List<Double> data, int windowSize) {
        // the list to hold the averaged data
        List<Double> averaged = new ArrayList<>(data.size());
        
        // apply averaging window
        for (int i = 0; i < data.size(); i++) {
            double value = 0.0;
            
            // calculate the end element and number of elements to collapse the
            // window at the final values
            int end = Math.min(i + windowSize, data.size());
            int elements = end - i;
            
            // perform the averaging
            for (int j = i; j < end; j++) {
                value += data.get(j) / elements;
            }
            averaged.add(value);
        }
        
        // return data
        return averaged;
    }
    
    /**
     * Gets the SignalProcessing data for each frame of the video.
     * @param fileIn The source video file.
     * @param videoInfo The VideoFileInfo object for the source video file.
     * @param signalFrequency The target frequency to find.
     * @return A list of the SignalProcessing data for each frame in the video.
     */
    static List<Double> getFrameFFTValues(File fileIn, VideoFileInfo videoInfo, double signalFrequency) {
        // the list of values for each frame
        List<Double> frameValues = new ArrayList<>();
        // the number of frames to process (limit to 30 seconds)
        int frameLimit = (int)Math.round(videoInfo.getFrameRate() * 30);
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

            // create SignalProcessing engine
            DoubleFFT_1D fft = new DoubleFFT_1D(samplesPerVideoFrame);

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

                // perform the SignalProcessing
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
        return frameValues;
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
        int lastByte;
        
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
