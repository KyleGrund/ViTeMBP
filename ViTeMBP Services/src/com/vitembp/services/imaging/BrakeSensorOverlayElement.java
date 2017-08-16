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
import com.vitembp.services.sensors.RotarySensor;

/**
 * An element that generates a data overlay for two brake sensors.
 */
class BrakeSensorOverlayElement extends OverlayElement {
    /**
     * The height of a single line of data.
     */
    private static final int LINE_HEIGHT = 20;
    
    /**
     * The total height of the element.
     */
    private static final int TOTAL_HEIGHT = 100;
    
    /**
     * The total width of the element.
     */
    private static final int TOTAL_WIDTH = 400;
    
    /**
     * The minimum possible value the sensor can read.
     */
    private final double leftMinValue;
    
    /**
     * The maximum possible value the sensor can read.
     */
    private final double leftMaxValue;
    
    /**
     * The minimum possible value the sensor can read.
     */
    private final double rightMinValue;
    
    /**
     * The maximum possible value the sensor can read.
     */
    private final double rightMaxValue;
    
    /**
     * The sensor that will read data to overlay.
     */
    private final RotarySensor leftSensor;
    
    /**
     * The sensor that will read data to overlay.
     */
    private final RotarySensor rightSensor;
    
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
     * @param leftBrakeSensor The sensor which will read data from the sample.
     * @param rightBrakeSensor The sensor which will read data from the sample.
     */
    BrakeSensorOverlayElement(int upperLeftX, int upperLeftY, int lowerRightX, int lowerRightY, ElementLocation location, double leftMinValue, double leftMaxValue, RotarySensor leftBrakeSensor, double rightMinValue, double rightMaxValue, RotarySensor rightBrakeSensor) {
        super(upperLeftX, upperLeftY, lowerRightX, lowerRightY, location);
        this.leftMinValue = leftMinValue;
        this.leftMaxValue = leftMaxValue;
        this.rightMinValue = rightMinValue;
        this.rightMaxValue = rightMaxValue;
        this.leftSensor = leftBrakeSensor;
        this.rightSensor = rightBrakeSensor;
    }
    
    @Override
    public void apply(DataOverlayBuilder builder, Sample data) {
        // get the data to render
        double leftDegrees = this.leftSensor.getPositionDegrees(data).orElse(leftMinValue);
        double rightDegrees = this.rightSensor.getPositionDegrees(data).orElse(rightMinValue);
        
        double leftPercent = (leftDegrees - leftMinValue) / (leftMaxValue - leftMinValue);
        double rightPercent = (rightDegrees - rightMinValue) / (rightMaxValue - rightMinValue);
        
        // calculate the upper left origin point of the element
        int topLeftX, topLeftY;
        switch (this.location) {
            case TopLeft:
                topLeftX = this.upperLeftX;
                topLeftY = this.upperLeftY;
                break;
            case TopRight:
                topLeftX = this.lowerRightX - TOTAL_WIDTH;
                topLeftY = this.upperLeftY;
                break;
            case BottomLeft:
                topLeftX = this.upperLeftX;
                topLeftY = this.lowerRightY - TOTAL_HEIGHT;
                break;
            case BottomRight:
                topLeftX = this.lowerRightX - TOTAL_WIDTH;
                topLeftY = this.lowerRightY - TOTAL_HEIGHT;
                break;
            case Center:
                topLeftX = ((this.lowerRightX - this.upperLeftX) / 2) - (TOTAL_WIDTH / 2);
                topLeftY = ((this.lowerRightY - this.upperLeftY) / 2) - (TOTAL_HEIGHT / 2);
                break;
            default:
                throw new PipelineExecutionException("Unknown brake sensor overlay element rendering locaiton.");
        }
        
        // render the element to the overlay
        builder.addText(this.leftSensor.getName() + ": " + Double.toString(leftPercent), topLeftX + (LINE_HEIGHT * 1), topLeftY + (LINE_HEIGHT * 0));
        builder.addHorizontalProgressBar((float)leftPercent, topLeftX + (LINE_HEIGHT * 1), topLeftY + (LINE_HEIGHT * 1), topLeftX - (LINE_HEIGHT * 1) + TOTAL_WIDTH, topLeftY + (LINE_HEIGHT * 2));
        builder.addHorizontalProgressBar((float)rightPercent, topLeftX + (LINE_HEIGHT * 1), topLeftY + (LINE_HEIGHT * 2), topLeftX - (LINE_HEIGHT * 1) + TOTAL_WIDTH, topLeftY + (LINE_HEIGHT * 3));
        builder.addText(this.rightSensor.getName() + ": " + Double.toString(rightPercent), topLeftX + (LINE_HEIGHT * 1), topLeftY + (LINE_HEIGHT * 4));
    }
}
