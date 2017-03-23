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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Class which creates a histogram for a bitmap.
 */
public class Histogram {
    /**
     * Histogram data for the blue channel.
     */
    private List<Double> blueValues;
    
    /**
     * Histogram data for the green channel.
     */
    private List<Double> greenValues;
    
    /**
     * Histogram data for the red channel.
     */
    private List<Double> redValues;
    
    /**
     * Histogram data for the alpha channel.
     */
    private List<Double> alphaValues;
        
    /**
     * Initializes a new instance of the Histogram class from a file.
     * @param bitmap The Path of the file to load bitmap data from.
     * @throws IOException If the file cannot be loaded.
     */
    public Histogram(Path bitmap) throws IOException {
        // load the bitmap data
        BufferedImage bi = ImageIO.read(bitmap.toFile());
        
        // calculate the histogram data
        buildHistogram(bi);
    }
    
    /**
     * Initializes a new instance of the Histogram class from a stream.
     * @param bitmap The InputStream to load bitmap data from.
     * @throws IOException If the stream cannot be accessed.
     */
    public Histogram(InputStream bitmap) throws IOException {
        // load the bitmap data
        BufferedImage bi = ImageIO.read(bitmap);
        
        buildHistogram(bi);
    }
    
    /**
     * Gets the immutable normalized histogram of the blue channel.
     * @return The immutable normalized histogram of the blue channel.
     */
    public List<Double> getBlueValues() {
        return Collections.unmodifiableList(this.blueValues);
    }
    
    /**
     * Gets the immutable normalized histogram of the green channel.
     * @return The immutable normalized histogram of the green channel.
     */
    public List<Double> getGreenValues() {
        return Collections.unmodifiableList(this.greenValues);
    }
    
    /**
     * Gets the immutable normalized histogram of the red channel.
     * @return The immutable normalized histogram of the red channel.
     */
    public List<Double> getRedValues() {
        return Collections.unmodifiableList(this.redValues);
    }
    
    /**
     * Gets the immutable normalized histogram of the alpha channel.
     * @return The immutable normalized histogram of the alpha channel.
     */
    public List<Double> getAlphaValues() {
        return Collections.unmodifiableList(this.alphaValues);
    }
    
    /**
     * Gets the average brightness of the blue channel.
     * @return The average brightness of the blue channel.
     */
    public Double getBlueBrightness() {
        return this.ExpectedValue(this.blueValues);
    }
    
    /**
     * Gets the average brightness of the green channel.
     * @return The average brightness of the green channel.
     */
    public Double getGreenBrightness() {
        return this.ExpectedValue(this.greenValues);
    }

    /**
     * Gets the average brightness of the red channel.
     * @return The average brightness of the red channel.
     */
    public Double getRedBrightness() {
        return this.ExpectedValue(this.redValues);
    }
    
    /**
     * Gets the average brightness of the alpha channel.
     * @return The average brightness of the alpha channel.
     */
    public Double getAlphaBrightness() {
        return this.ExpectedValue(this.alphaValues);
    }
    
    /**
     * Calculates and returns the expected value of the index given a list of
     * probabilities for each index.
     * When applied to normalized channel data this value indicates the average
     * brightness of the channel because the index represents the color value.
     * @param values The probabilities of each index.
     * @return The expected value of the index.
     */
    private double ExpectedValue(List<Double> values) {
        double value = 0.0;
        for (int i = 1; i < values.size(); i++) {
            value += i * values.get(i);
        }
        return value;
    }

    /**
     * Builds the histogram data for the given image.
     * @param image The image to build histogram data for.
     */
    private void buildHistogram(BufferedImage image) {
        // get the image height and width in pixels
        int height = image.getHeight();
        int width = image.getWidth();
        
        // counter arrays for the times the channel value has been seen
        int[] blues = new int[256];
        int[] greens = new int[256];
        int[] reds = new int[256];
        int[] alphas = new int[256];

        // process each pixel in the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // get the value of the pixel in 8 bit channels: ARGB
                int value = image.getRGB(x, y);
                
                // increment the appropriate histogram counter
                blues[value & 0xff]++;
                greens[(value >> 8) & 0xff]++;
                reds[(value >> 16) & 0xff]++;
                alphas[(value >> 24) & 0xff]++;
            }
        }
        
        // normalize the data by dividing the counts by the number of pixels
        Double factor = 1 / ((double)width * (double)height);
        this.blueValues = ToNormalizedList(blues, factor);
        this.greenValues = ToNormalizedList(greens, factor);
        this.redValues = ToNormalizedList(reds, factor);
        this.alphaValues = ToNormalizedList(alphas, factor);
    }

    /**
     * Creates a List of normalized double values from an array of integers and
     * a normalizing factor.
     * @param values The values to normalize.
     * @param normFactor The factor which normalizes the values.
     * @return A List of normalized double values.
     */
    private List<Double> ToNormalizedList(int[] values, double normFactor) {
       Double[] toInit = new Double[values.length];
        
        for (int i = 0; i < values.length; i++) {
            toInit[i] = values[i] * normFactor;
        }
        
        return Arrays.asList(toInit);
    }
}
