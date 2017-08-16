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
import com.vitembp.services.sensors.Sensor;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

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
     * @param elements Element definitions.
     */
    FourQuadrantOveraly(int width, int height, List<Sensor> sensors, Map<Sensor, Double> minimumValues, Map<Sensor, Double> maximumValues, List<ElementDefinition> elements) throws InstantiationException {
        // save elements
        this.width = width;
        this.height = height;
        
        // build elements
        if (elements.size() > 4) {
            throw new InstantiationException("FourQuadrantOverlay can only contain 4 elements.");
        }
        
        ElementDefinition topLeft = elements.stream()
                .filter(e -> e.getLocation() == ElementLocation.TopLeft)
                .findFirst()
                .get();
        ElementDefinition topRight = elements.stream()
                .filter(e -> e.getLocation() == ElementLocation.TopRight)
                .findFirst()
                .get();
        ElementDefinition bottomLeft = elements.stream()
                .filter(e -> e.getLocation() == ElementLocation.BottomLeft)
                .findFirst()
                .get();
        ElementDefinition bottomRight = elements.stream()
                .filter(e -> e.getLocation() == ElementLocation.BottomRight)
                .findFirst()
                .get();
        
        int centerXShort = width / 2;
        int centerXLong = (int)Math.ceil(((double)width) / 2.0d);
        int centerYShort = height / 2;
        int centerYLong = (int)Math.ceil(((double)height) / 2.0d);
        
        this.upperLeft = OverlayElementFactory.buildElement(
                topLeft,
                ElementLocation.TopLeft,
                sensors,
                minimumValues,
                maximumValues,
                0,
                0,
                centerXShort,
                centerYShort);
        
        this.upperRight = OverlayElementFactory.buildElement(
                topRight,
                ElementLocation.TopRight,
                sensors,
                minimumValues,
                maximumValues,
                centerXLong,
                0,
                width,
                centerYShort);
        
        this.lowerLeft = OverlayElementFactory.buildElement(
                bottomLeft,
                ElementLocation.BottomLeft,
                sensors,
                minimumValues,
                maximumValues,
                0,
                centerYLong,
                centerXShort,
                height);
        
        this.lowerRight = OverlayElementFactory.buildElement(
                bottomRight,
                ElementLocation.BottomRight,
                sensors,
                minimumValues,
                maximumValues,
                centerXLong,
                centerYLong,
                width,
                height);
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
}
