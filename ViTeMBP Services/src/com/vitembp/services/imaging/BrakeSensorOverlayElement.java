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
import com.vitembp.services.sensors.RotarySensor;

/**
 *
 * @author Kyle
 */
class BrakeSensorOverlayElement extends OverlayElement {
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
     * @param minValue The minimum possible value of the sensor data in Gs.
     * @param maxValue The maximum possible value of the sensor data in Gs.
     * @param leftBrakeSensor The sensor which will read data from the sample.
     * @param rightBrakeSensor The sensor which will read data from the sample.
     */
    BrakeSensorOverlayElement(int upperLeftX, int upperLeftY, int lowerRightX, int lowerRightY, double leftMinValue, double leftMaxValue, RotarySensor leftBrakeSensor, double rightMinValue, double rightMaxValue, RotarySensor rightBrakeSensor) {
        super(upperLeftX, upperLeftY, lowerRightX, lowerRightY);
        this.leftMinValue = leftMinValue;
        this.leftMaxValue = leftMaxValue;
        this.rightMinValue = rightMinValue;
        this.rightMaxValue = rightMaxValue;
        this.leftSensor = leftBrakeSensor;
        this.rightSensor = rightBrakeSensor;
    }
    
    @Override
    public void apply(DataOverlayBuilder builder, Sample data) {
        double leftDegrees = this.leftSensor.getPositionDegrees(data).orElse(leftMinValue);
        double rightDegrees = this.rightSensor.getPositionDegrees(data).orElse(rightMinValue);
        
        double leftPercent = (leftDegrees - leftMinValue) / (leftMaxValue - leftMinValue);
        double rightPercent = (rightDegrees - rightMinValue) / (rightMaxValue - rightMinValue);
        
        builder.addText("L: " + Double.toString(leftPercent), this.upperLeftX + 4, this.upperLeftY + 4);
        builder.addText("R: " + Double.toString(rightPercent), this.upperLeftX + 4, this.upperLeftY + 24);
    }
}
