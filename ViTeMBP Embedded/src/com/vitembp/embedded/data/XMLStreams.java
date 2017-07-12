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
package com.vitembp.embedded.data;

import java.io.StringReader;
import java.io.Writer;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * This class provides static methods for working with XMLStreams.
 */
public class XMLStreams {
    /**
     * The factory used to build XMLStreamReaders.
     */
    private static XMLInputFactory inputFactory;
    
    /**
     * The factory used to build XMLStreamWriters.
     */
    private static XMLOutputFactory outputFactory;
    
    /**
     * Initializes static memeber variables.
     */
    static {
        // build and configure factories
        XMLStreams.inputFactory = XMLInputFactory.newFactory();
        XMLStreams.inputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
        
        XMLStreams.outputFactory = XMLOutputFactory.newFactory();
    }
    
    /**
     * Reads a single text element from XMLStreamReader.
     * @param name The name of the element.
     * @param toReadFrom The reader to read from.
     * @return The string read from the stream.
     * @throws XMLStreamException If an exception occurs reading from stream.
     */
    public static String readElement(String name, XMLStreamReader toReadFrom) throws XMLStreamException {
        // check starting element
        if (toReadFrom.getEventType()!= XMLStreamConstants.START_ELEMENT || !name.equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <" + name + "> not found.", toReadFrom.getLocation());
        }
        
        // read text data
        if (toReadFrom.next() != XMLStreamConstants.CHARACTERS) {
            throw new XMLStreamException("Expected " + name + " value not found.", toReadFrom.getLocation());
        }
        
        String value  = toReadFrom.getText();
        
        // read into close element
        if (toReadFrom.next() != XMLStreamConstants.END_ELEMENT || !name.equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </" + name + "> not found.", toReadFrom.getLocation());
        }
        
        // read past close element
        toReadFrom.next();
        
        return value;
    }
    
    /**
     * Creates a coalescing XMLStreamReader from a Reader.
     * @param from The String to parse XML from.
     * @return The configured XMLStreamReader.
     * @throws javax.xml.stream.XMLStreamException If an exception occurs while creating the reader.
     */
    public static XMLStreamReader createReader(String from) throws XMLStreamException {
        return XMLStreams.inputFactory.createXMLStreamReader(new StringReader(from));
    }
    
    /**
     * Creates an XMLStreamReader from a Writer.
     * @param to The writer to emit XML to.
     * @return The configured XMLStreamWriter.
     * @throws javax.xml.stream.XMLStreamException If an exception occurs while creating the writer.
     */
    public static XMLStreamWriter createWriter(Writer to) throws XMLStreamException {
        return XMLStreams.outputFactory.createXMLStreamWriter(to);
    }
}
