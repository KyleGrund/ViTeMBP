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
import com.vitembp.services.sensors.AccelerometerThreeAxis;
import com.vitembp.services.sensors.DistanceSensor;
import com.vitembp.services.sensors.RotarySensor;
import com.vitembp.services.sensors.Sensor;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * An OverlayFrameCreator which organizes a four quadrant layout.
 */
class FourQuadrantOveraly implements Overlay {
    /**
     * The upper-left overlay element.
     */
    private final OverlayElement upperLeft;
    
    /**
     * The upper-right overlay element.
     */
    private final OverlayElement upperRight;
    
    /**
     * The lower-left overlay element.
     */
    private final OverlayElement lowerLeft;
    
    /**
     * The lower-right overlay element.
     */
    private final OverlayElement lowerRight;
    
    /**
     * The height of the overlay.
     */
    private final int height;
    
    /**
     * The width of the overlay.
     */
    private final int width;
    
    /**
     * Initializes a new instance of the FourQuadrantOveralyCreator class.
     * @param width The width of the overlay.
     * @param height The height of the overlay.
     * @param upperLeft The upper-left overlay element.
     * @param upperRight The upper-right overlay element.
     * @param lowerLeft The lower-left overlay element.
     * @param lowerRight The lower-right overlay element.
     */
    FourQuadrantOveraly(int width, int height, List<Sensor> upperLeft, List<Sensor> upperRight, List<Sensor> lowerLeft, List<Sensor> lowerRight) {
        // save elements
        this.width = width;
        this.height = height;
        
        // build elements for sensors
        this.upperLeft = getElementForSensors(upperLeft);
        this.upperRight = getElementForSensors(upperLeft);
        this.lowerLeft = getElementForSensors(upperLeft);
        this.lowerRight = getElementForSensors(upperLeft);
    }
            
    @Override
    public void addOverlay(Path inputImage, Sample data) throws IOException {
        DataOverlayBuilder builder = new DataOverlayBuilder(inputImage);
        
        if (upperLeft != null) {
            upperLeft.apply(builder, data);
        }
        
        if (upperRight != null) {
            upperRight.apply(builder, data);
        }
        
        if (lowerLeft != null) {
            lowerLeft.apply(builder, data);
        }
        
        if (lowerRight != null) {
            lowerRight.apply(builder, data);
        }
        
        builder.saveImage(inputImage.toFile());
    }

    private OverlayElement getElementForSensors(List<Sensor> sensors) {
        if (sensors == null || sensors.isEmpty()) {
            return null;
        }
        
        Sensor first = sensors.get(0);
        if (first instanceof AccelerometerThreeAxis) {
            //return new ThreeAxisGOverlayElement(first);
        } else if (first instanceof DistanceSensor) {
            //return new ShockOverlayElement(first);
        } else if (first instanceof RotarySensor) {
            
        }
        
        return null;
    }
    
}
