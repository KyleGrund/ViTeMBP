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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides information about a series of histograms.
 */
public class HistogramList extends ArrayList<Histogram> {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Gets the average value of the double selected from the Histograms in
     * the collection by the selector function provided.
     * @param selector A function which selects the value from the Histogram
     * to be averaged.
     * @return the average of the values selected from the Histograms in
     * the collection by the selector function.
     */
    public double getAverage(Function<Histogram, Double> selector) {
        // accumulator for average calculation
        double average = 0.0;
        
        // keeps track of number of elements in the average
        int count = 1;
        
        // calculate in a 
        for (Histogram element : this) {
            average += (selector.apply(element) - average) / count;
            count++;
        }
        
        return average;
    }
    
    /**
     * Gets the standard deviation of the double selected from the
     * Histograms in the collection by the selector function provided.
     * @param selector A function which selects the value from the Histogram
     * to be evaluated.
     * @return The standard deviation of the double selected from the
     * Histograms in the collection by the selector function provided.
     */
    public double getStdev(Function<Histogram, Double> selector) {
        // average value of the elements
        double average = getAverage(selector);
        
        // accumulator for the stdev
        double stdev = 0.0;
        
        // keeps track of number of elements in the average
        int count = 1;
        
        for (Histogram element : this) {
            stdev += (Math.abs(selector.apply(element) - average) - stdev) / count;
            count++;
        }
        
        return stdev;
    }
    
    /**
     * Gets a list of indexes of the outliers of the double selected from the
     * Histograms in the collection by the selector function provided. Outlier
     * is defined as having a value more standard deviations above the average
     * that is specified by the deviations parameter.
     * @param selector A function which selects the value from the Histogram
     * to be evaluated.
     * @param deviations The number of deviations away from the average which
     * would indicate a value is an outlier.
     * @return A list of indexes of the outliers of the double selected from the
     * Histograms in the collection by the selector function provided.
     */
    public List<Integer> getPositiveOutliers(Function<Histogram, Double> selector, double deviations) {
        // list of outlier histograms that have been found
        List<Integer> outliers = new ArrayList<>();
        
        // calculate the target value defining an outlier as
        double average = this.getAverage(selector);
        double stdev = this.getStdev(selector);
        double target = (deviations * stdev) + average;
        
        // find all outlier histograms
        for (int i = 0; i < this.size(); i++) {
            if(selector.apply(this.get(i)) > target) {
                outliers.add(i);
            }
        }
        
        return outliers;
    }
    
    /**
     * Loads sequentially named image files from a directory into a new
     * HistogramList instance using the supplied name generation function.
     * @param directory The Path object pointing to the directory containing
     * the images to load.
     * @param nameGenerator The function that will be used by the function to
     * generate the sequential file names to be loaded when called with the
     * integer parameter starting at 1 and increased by 1 in each subsequent
     * call.
     * @return A new HistogramList instance representing sequentially named
     * files in the supplied directory.
     */
    public static HistogramList loadFromDirectory(Path directory, FilenameGenerator nameGenerator) {
        LOGGER.info("Building histograms from: " + directory.toString());
        
        HistogramList histograms = new HistogramList();
        boolean cont = true;
        int fileCount = 1;
        
        // loop until we no longer detect image files to open
        while (cont) {
            // creates a path the the next file to open
            Path file = directory.resolve(nameGenerator.getPath(fileCount));
            fileCount++;
            
            // load the file if it exits, otherwise set flag to exit the loop
            if (Files.exists(file)){
                try {
                    Histogram toAdd = new Histogram(file);
                    histograms.add(toAdd);
                } catch (IOException ex) {
                    LOGGER.info("Failed to open image: " + file.toString(), ex);
                }
            } else {
                cont = false;
            }
        }
        
        LOGGER.info("Loaded " + Integer.toString(histograms.size()) + " files.");
        
        return histograms;
    }
}
