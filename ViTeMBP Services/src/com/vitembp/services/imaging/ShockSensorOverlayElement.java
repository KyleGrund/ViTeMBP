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

import com.vitembp.embedded.data.Sample;
import com.vitembp.services.data.PipelineExecutionException;
import com.vitembp.services.sensors.DistanceSensor;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Creates an overlay for a linear shock sensor.
 */
public class ShockSensorOverlayElement extends OverlayElement {
    /**
     * The total height of the rendered element.
     */
    private static final int TOTAL_HEIGHT = 400;
    
    /**
     * The total width of the rendered element.
     */
    private static final int TOTAL_WIDTH = 150;
    
    /**
     * The total height of the rendered text.
     */
    private static final int TEXT_HEIGHT = 20;
    
    /**
     * The distance to pad from the edges of the image.
     */
    private static final int BORDER_PAD = 20;
    
    /**
     * The number of data point to average to smooth noisy data.
     */
    private static final int SMOOTHING_DEPTH = 3;
    
    /**
     * The minimum possible value the sensor can read in Gs.
     */
    private final double minValue;
    
    /**
     * The maximum possible value the sensor can read in Gs.
     */
    private final double maxValue;
    
    /**
     * The sensor that will read data to overlay.
     */
    private final DistanceSensor sensor;
    
    /**
     * Previous data for averaging.
     */
    private final LinkedList<Double> previousValues = new LinkedList<>();
    
    /**
     * Used to format the text value display.
     */
    private final DecimalFormat formatter = new DecimalFormat("###");
        
    /**
     * Initializes a new instance of the ThreeAxisGOverlayElement class.
     * @param upperLeftX The X-coordinate of the upper left bounding point.
     * @param upperLeftY The Y-coordinate of the upper left bounding point.
     * @param lowerRightX The X-coordinate of the lower right bounding point.
     * @param lowerRightY The Y-coordinate of the lower right bounding point.
     * @param location The location to render the element if it does not fill
     * the bounding box.
     * @param minValue The minimum possible value of the sensor data in Gs.
     * @param maxValue The maximum possible value of the sensor data in Gs.
     * @param sensor The sensor which will read data from the sample.
     */
    ShockSensorOverlayElement(int upperLeftX, int upperLeftY, int lowerRightX, int lowerRightY, ElementLocation location, double minValue, double maxValue, DistanceSensor sensor) {
        super(upperLeftX, upperLeftY, lowerRightX, lowerRightY, location);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.sensor = sensor;
        
        // seed averaging data
        while (previousValues.size() < SMOOTHING_DEPTH) {
            previousValues.add(this.maxValue);
        }
    }
    
    @Override
    void apply(DataOverlayBuilder builder, Sample data) {
        // read in new data failing to an average of previous data if
        // none is available
        double percentage;
        Optional<Double> reading = this.sensor.getDistancePercent(data);
        if (reading.isPresent()) {
            percentage= reading.get();
        } else {
            percentage = this.average(this.previousValues);
        }

        // save new data and discard oldest to retain averaging window size
        this.previousValues.add(percentage);
        this.previousValues.pop();
        
        // re-calculate the percentage as the average of SMOOTHING_DEPTH points
        percentage = this.average(this.previousValues);
        
        // calculate the upper left origin point of the element
        int topLeftX, topLeftY;
        switch (this.location) {
            case TopLeft:
                topLeftX = this.upperLeftX + BORDER_PAD;
                topLeftY = this.upperLeftY + BORDER_PAD;
                break;
            case TopRight:
                topLeftX = this.lowerRightX - TOTAL_WIDTH - BORDER_PAD;
                topLeftY = this.upperLeftY + BORDER_PAD;
                break;
            case BottomLeft:
                topLeftX = this.upperLeftX + BORDER_PAD;
                topLeftY = (this.lowerRightY - TOTAL_HEIGHT) + BORDER_PAD;
                break;
            case BottomRight:
                topLeftX = this.lowerRightX - TOTAL_WIDTH - BORDER_PAD;
                topLeftY = (this.lowerRightY - TOTAL_HEIGHT) + BORDER_PAD;
                break;
            case Center:
                topLeftX = ((this.lowerRightX - this.upperLeftX) / 2) - (TOTAL_WIDTH / 2);
                topLeftY = ((this.lowerRightY - this.upperLeftY) / 2) - (TOTAL_HEIGHT / 2);
                break;
            default:
                throw new PipelineExecutionException("Unknown brake sensor overlay element rendering locaiton.");
        }
        
        // render graphic elements
        builder.addVerticalProgressBar((float)percentage, topLeftX, topLeftY, topLeftX + TOTAL_WIDTH, topLeftY + TOTAL_HEIGHT - (TEXT_HEIGHT * 2));
        builder.addText(
                this.sensor.getName() + ": " + this.formatter.format(percentage) + "%",
                topLeftX,
                topLeftY + TOTAL_HEIGHT - TEXT_HEIGHT);
    }

    /**
     * Averages the values in the list.
     * @param previousValues The values to average.
     * @return The average of the supplied values.
     */
    private double average(List<Double> previousValues) {
        double result = 0.0;
        for (double val : previousValues) {
            result += val / previousValues.size();
        }
        
        return result;
    }
}
