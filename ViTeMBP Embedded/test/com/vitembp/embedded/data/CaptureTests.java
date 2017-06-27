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

import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests that can be run on any Capture implementation.
 */
public class CaptureTests {
    public static void testToXml(Capture instance) {
        String xmlValue = instance.toXml();
    }
    
    public static void testWriteTo(Capture instance) throws FactoryConfigurationError, XMLStreamException {
        StringWriter sw = new StringWriter();
        XMLStreamWriter toWriteTo = XMLOutputFactory.newFactory().createXMLStreamWriter(sw);
        
        instance.writeTo(toWriteTo);
        
        String xmlValue = sw.toString();
    }
    
    public static void testGetSamples(Capture instance, List<Sample> expected, BiConsumer<Object, Object> assertEquals, Consumer<Boolean> assertTrue) {
        // convert samples to list
        List<Sample> found = new ArrayList<>();
        instance.getSamples().forEach(found::add);
        assertEquals(found.size(), expected.size());

        // check each element matches
        for (int i = 0; i < expected.size(); i++) {
            Sample a = found.get(i);
            Sample b = expected.get(i);
            assertEquals(i, a.getIndex());
            assertEquals(a.getIndex(), b.getIndex());
            assertEquals(a.getSensorData(), b.getSensorData());
            
            // sanity check times are within a second of the current time
            long aDiff = Instant.now().getEpochSecond() - a.getTime().getEpochSecond();
            long bDiff = Instant.now().getEpochSecond() - b.getTime().getEpochSecond();
            assertTrue(aDiff <= 1 && aDiff >= 0);
            assertTrue(bDiff <= 1 && bDiff >= 0);
        }
    }
}
