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

/**
 * An element that can be overlaid on a image.
 */
abstract class OverlayElement {
    /**
     * The location of the X-coordinate of the upper left bounding point.
     */
    protected final int upperLeftX;
    
    /**
     * The location of the Y-coordinate of the upper left bounding point.
     */
    protected final int upperLeftY;
    
    /**
     * The location of the X-coordinate of the lower right bounding point.
     */
    protected final int lowerRightX;
    
    /**
     * The location of the Y-coordinate of the lower right bounding point.
     */
    protected final int lowerRightY;
    
    /**
     * The location to render the element if it does not fill the bounding box.
     */
    protected final ElementLocation location;
    
    /**
     * Initializes a new instance of the OverlayElement class.
     * @param upperLeftX The X-coordinate of the upper left bounding point.
     * @param upperLeftY The Y-coordinate of the upper left bounding point.
     * @param lowerRightX The X-coordinate of the lower right bounding point.
     * @param lowerRightY The Y-coordinate of the lower right bounding point.
     * @param location The location to render the element if it does not fill
     * the bounding box.
     */
    OverlayElement(int upperLeftX, int upperLeftY, int lowerRightX, int lowerRightY, ElementLocation location) {
        this.upperLeftX = upperLeftX;
        this.upperLeftY = upperLeftY;
        this.lowerRightX = lowerRightX;
        this.lowerRightY = lowerRightY;
        this.location = location;
    }
    
    /**
     * Applies the overlay using the supplied builder.
     * @param builder The build to use to construct the overlay.
     * @param data The data to use to build the element.
     */
    public abstract void apply(DataOverlayBuilder builder, Sample data);
}
