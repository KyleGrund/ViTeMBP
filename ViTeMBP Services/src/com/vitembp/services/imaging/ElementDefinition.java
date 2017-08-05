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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Defines an element in an overlay definition.
 */
class ElementDefinition {
    /**
     * The type of element being defined.
     */
    private final ElementType elementType;
    
    /**
     * The location of the element in the overlay.
     */
    private final ElementLocation location;
    
    /**
     * The sensors which are bound to this element.
     */
    private final List<SensorDefinition> sensorDefinitions;

    /**
     * Initializes a new instance of the ElementDefinition class.
     * @param item The XML node containing the data for this definition.
     */
    ElementDefinition(Node item) throws IOException {
        // check the proper node was passed
        if (item.getNodeType() != Node.ELEMENT_NODE ||
                !((Element)item).getTagName().equals("element")) {
            throw new IOException("The element node was not found.");
        }
        
        Element elem = (Element)item;
        
        // load type
        NodeList type = elem.getElementsByTagName("type");
        if (type == null || type.getLength() != 1) {
            throw new IOException("Element requires one type element.");
        }
        
        try {
            this.elementType = ElementType.valueOf(type.item(0).getTextContent());
        } catch (IllegalArgumentException ex) {
            throw new IOException("Error parsing element type.", ex);
        }
        
        // load location
        XPath xPath =  XPathFactory.newInstance().newXPath();
        try {
            NodeList locations = (NodeList)xPath.evaluate("location", item, XPathConstants.NODESET);
            if (locations == null || locations.getLength() != 1) {
                throw new IOException("Element requires one location element.");
            }
        
            this.location = ElementLocation.valueOf(locations.item(0).getTextContent());
        } catch (IllegalArgumentException | XPathExpressionException ex) {
            throw new IOException("Error parsing element location.", ex);
        }

        // load sensor bindings
        NodeList bindings = elem.getElementsByTagName("sensorbindings");
        if (bindings == null || bindings.getLength() != 1) {
            throw new IOException("Element requires one sensorbindings element.");
        }
        
        this.sensorDefinitions = new ArrayList<>();
        try {
            NodeList sensors = (NodeList)xPath.evaluate("sensor", bindings.item(0), XPathConstants.NODESET);
            for (int i = 0; i < sensors.getLength(); i++) {
                this.sensorDefinitions.add(new SensorDefinition(sensors.item(i)));
            }
        } catch (IllegalArgumentException | XPathExpressionException ex) {
            throw new IOException("Error parsing sensorbindings element.", ex);
        }
    }
    
    /**
     * Gets the type of the element this instance defines.
     * @return The type of the element this instance defines.
     */
    public ElementType getElementType() {
        return this.elementType;
    }
    
    /**
     * Gets the location of the element in the overlay.
     * @return The location of the element in the overlay.
     */
    public ElementLocation getLocation() {
        return this.location;
    }
    
    /**
     * Gets the sensors which are bound to this element.
     * @return The sensors which are bound to this element.
     */
    public List<SensorDefinition> getSensors() {
        return this.sensorDefinitions;
    }
}
