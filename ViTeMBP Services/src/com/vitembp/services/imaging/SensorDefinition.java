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

import java.io.IOException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Defines a sensor binding to an element.
 */
class SensorDefinition {
    /**
     * The location of the sensor in the display element.
     */
    private final ElementLocation location;
    
    /**
     * The name of the sensor being bound to.
     */
    private final String name;

    /**
     * Initializes a new instance of the SensorDefinition class.
     * @param item The node to load the sensor definition from.
     */
    SensorDefinition(Node item) throws IOException {
        // check the proper node was passed
        if (item.getNodeType() != Node.ELEMENT_NODE ||
                !((Element)item).getTagName().equals("sensor")) {
            throw new IOException("The sensor element node was not found.");
        }
        
        Element elem = (Element)item;
        
        // load location
        NodeList locaitons = elem.getElementsByTagName("location");
        if (locaitons == null || locaitons.getLength() != 1) {
            throw new IOException("Element requires one location element.");
        }
        
        try {
            this.location = ElementLocation.valueOf(locaitons.item(0).getTextContent());
        } catch (IllegalArgumentException ex) {
            throw new IOException("Error parsing element location.", ex);
        }

        // load type
        NodeList name = elem.getElementsByTagName("name");
        if (name == null || name.getLength() != 1) {
            throw new IOException("Element requires one name element.");
        }
        
        try {
            this.name = name.item(0).getTextContent();
        } catch (IllegalArgumentException ex) {
            throw new IOException("Error parsing name element.", ex);
        }
    }
    
    /**
     * Gets the location of the sensor in the display element.
     * @return The location of the sensor in the display element.
     */
    public ElementLocation getLocation() {
        return this.location;
    }
    
    /**
     * Gets the name of the sensor being bound to.
     * @return The name of the sensor being bound to.
     */
    public String getName() {
        return this.name;
    }
}
