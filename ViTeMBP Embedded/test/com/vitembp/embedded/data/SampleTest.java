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
import java.io.StringWriter;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author kyle
 */
public class SampleTest {
    private final Map<String, String> sensorData;
    
    public SampleTest() {
        Map<String, String> data = new HashMap<>();
        data.put("Sensor 1", "Some data from sensor 1.");
        data.put("Sensor 2", "Some data from sensor 2.");
        data.put("Sensor 3", "Some data from sensor 3.");
        
        sensorData = Collections.unmodifiableMap(data);
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getIndex method, of class Sample.
     */
    @Test
    public void testGetIndex() {
        System.out.println("getIndex");
        Sample instance = new Sample(0, Instant.EPOCH, this.sensorData);
        int expResult = 0;
        int result = instance.getIndex();
        assertEquals(expResult, result);
    }

    /**
     * Test of getTime method, of class Sample.
     */
    @Test
    public void testGetTime() {
        System.out.println("getTime");
        Sample instance = new Sample(0, Instant.EPOCH, this.sensorData);
        Instant expResult = Instant.EPOCH;
        Instant result = instance.getTime();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSensorData method, of class Sample.
     */
    @Test
    public void testGetSensorData() {
        System.out.println("getSensorData");
        Sample instance = new Sample(0, Instant.EPOCH, this.sensorData);
        Map<String, String> expResult = this.sensorData;
        Map<String, String> result = instance.getSensorData();
        assertEquals(expResult, result);
    }

    /**
     * Test of toXmlFragment method, of class Sample.
     */
    @Test
    public void testToXmlFragment() {
        System.out.println("toXmlFragment");
        Sample instance = new Sample(0, Instant.EPOCH, this.sensorData);
        String expResult = "<sample><sensor name=\"Sensor 2\">Some data from sensor 2.</sensor><sensor name=\"Sensor 3\">Some data from sensor 3.</sensor><sensor name=\"Sensor 1\">Some data from sensor 1.</sensor></sample>";
        String result = instance.toXmlFragment();
        assertEquals(expResult, result);
    }

    /**
     * Test of writeTo method, of class Sample.
     */
    @Test
    public void testWriteTo() throws Exception {
        System.out.println("writeTo");
        StringWriter sw = new StringWriter();
        XMLStreamWriter toWriteTo = XMLOutputFactory.newFactory().createXMLStreamWriter(sw);
        Sample instance = new Sample(0, Instant.EPOCH, this.sensorData);
        instance.writeTo(toWriteTo);
        String expResult = "<sample><sensor name=\"Sensor 2\">Some data from sensor 2.</sensor><sensor name=\"Sensor 3\">Some data from sensor 3.</sensor><sensor name=\"Sensor 1\">Some data from sensor 1.</sensor></sample>";
        String result = sw.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of readFrom method, of class Sample.
     */
    @Test
    public void testReadFrom() throws Exception {
        System.out.println("readFrom");
        Sample instance = new Sample(0, Instant.EPOCH, this.sensorData);
        String sampleString = instance.toXmlFragment();
        XMLStreamReader toReadFrom = XMLInputFactory.newFactory().createXMLStreamReader(new StringReader(sampleString));
        toReadFrom.next();
        Sample expResult = new Sample(0, Instant.EPOCH, this.sensorData);
        Sample result = new Sample(0, Instant.EPOCH, toReadFrom);
        assertEquals(expResult.getIndex(), result.getIndex());
        assertTrue(expResult.getTime().equals(result.getTime()));
        assertEquals(expResult.getSensorData().size(), result.getSensorData().size());
        expResult.getSensorData().forEach((key, value) -> { assertTrue(expResult.getSensorData().get(key).equals(value)); });
    }
}
