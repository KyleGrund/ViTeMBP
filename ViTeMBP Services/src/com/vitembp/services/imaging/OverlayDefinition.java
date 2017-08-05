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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.codec.Charsets;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Defines a data overlay.
 */
class OverlayDefinition {
    /**
     * The type of overlay this instance represents.
     */
    private final OverlayType overlayType;
    
    /**
     * The element definitions for this overlay.
     */
    private final List<ElementDefinition> elementDefinitions;
    
    /**
     * Initializes a new instance of the OverlayDefinition class.
     * @param toBuildFrom 
     */
    private OverlayDefinition(String toBuildFrom) throws IOException {
        // load the definition from XML using xpath methods
        // first parse the document
        Document def;
        try {
            def = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(toBuildFrom.getBytes(Charsets.UTF_8)));
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new IOException("Could not parse XML input.", ex);
        }
        // prevents issues caused by parser returning multiple text elements for
        // a single text area
        def.getDocumentElement().normalize();
        
        // get the overlay type
        XPath xPath =  XPathFactory.newInstance().newXPath();
        try {
            Node type = (Node)xPath.evaluate("/overlay/type", def, XPathConstants.NODE);
            this.overlayType = OverlayType.valueOf(type.getTextContent());
        } catch (XPathExpressionException | IllegalArgumentException ex) {
            throw new IOException("Exception parsing type.", ex);
        }
        
        // get element definitions
        try {
            NodeList elements = (NodeList)xPath.evaluate("/overlay/elements/element", def, XPathConstants.NODESET);

            this.elementDefinitions = new ArrayList<>();
            for (int i = 0; i < elements.getLength(); i++) {
                this.elementDefinitions.add(new ElementDefinition(elements.item(i)));
            }
        } catch (XPathExpressionException | IllegalArgumentException ex) {
            throw new IOException("Exception parsing elements.", ex);
        }
    }
    
    /**
     * Gets the type of overlay this instance represents.
     * @return The type of overlay this instance represents.
     */
    public OverlayType getOverlayType() {
        return this.overlayType;
    }
    
    /**
     * Gets the element definitions for this overlay.
     * @return The element definitions for this overlay.
     */
    public List<ElementDefinition> getElementDefinitions() {
        return this.elementDefinitions;
    }
    
    /**
     * Builds and returns an OverlayDefinition from the supplied XML fragment.
     * @param xmlDefinition The XML containing the definition to load.
     * @return An OverlayDefinition from the supplied XML fragment.
     */
    public static OverlayDefinition getDefinition(String xmlDefinition) throws IOException {
        return new OverlayDefinition(xmlDefinition);
    }
}
