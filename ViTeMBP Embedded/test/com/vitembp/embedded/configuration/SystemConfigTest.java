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
package com.vitembp.embedded.configuration;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Set;
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
 * @author Kyle
 */
public class SystemConfigTest {
    /**
     * An XML String when loaded sets the configuration to default values.
     */
    private static final String DEFAULT = "<?xml version=\"1.0\" ?><configuration><systemname></systemname><systemid>2ae1239a-3389-4580-b704-ff5c7b4dd3ee</systemid><samplingfrequency>29.97</samplingfrequency><sensornames></sensornames><sensorbindingsites></sensorbindingsites><sensorbindings></sensorbindings><sensorcalibrations></sensorcalibrations><capturetype>EmbeddedH2</capturetype><enablecompression>true</enablecompression><cloud><uploadtocloud>true</uploadtocloud><deleteonuploadtocloud>true</deleteonuploadtocloud></cloud><networkinterfaces><wiredethernet><metric>0</metric></wiredethernet><wirelessethernet><metric>0</metric></wirelessethernet><bluetooth><metric>0</metric></bluetooth></networkinterfaces></configuration>";
    
    /**
     * An XML String when loaded sets the configuration to have two sensor names and 30 samples per second.
     */
    private static final String TWO_NAMES_30SPS = "<?xml version=\"1.0\" ?><configuration><systemname></systemname><systemid>2ae1239a-3389-4580-b704-ff5c7b4dd3ee</systemid><samplingfrequency>30</samplingfrequency><sensornames><name>Name 1</name><name>Name 2</name></sensornames><sensorbindingsites></sensorbindingsites><sensorbindings></sensorbindings><sensorcalibrations></sensorcalibrations><capturetype>EmbeddedH2</capturetype><enablecompression>true</enablecompression><cloud><uploadtocloud>true</uploadtocloud><deleteonuploadtocloud>true</deleteonuploadtocloud></cloud><networkinterfaces><wiredethernet><metric>0</metric></wiredethernet><wirelessethernet><metric>0</metric></wirelessethernet><bluetooth><metric>0</metric></bluetooth></networkinterfaces></configuration>";
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // this will ensure that the DEFAULT actually reflects the XML output
        // for the default values of the class.
        StringWriter sw = new StringWriter();
        XMLStreamWriter toWriteTo = XMLOutputFactory.newFactory().createXMLStreamWriter(sw);
        SystemConfig instance = SystemConfig.getConfig();
        instance.writeTo(toWriteTo);
        String output = sw.toString();
        String expected = DEFAULT;
        assertTrue(expected.equals(output));
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws Exception {
        // loads the default configuration
        StringReader sr = new StringReader(DEFAULT);
        XMLStreamReader toReadFrom = XMLInputFactory.newFactory().createXMLStreamReader(sr);
        SystemConfig.getConfig().readFrom(toReadFrom);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getSamplingFrequency method, of class SystemConfig.
     */
    @Test
    public void testGetSamplingFrequency() {
        System.out.println("getSamplingFrequency");
        SystemConfig instance = SystemConfig.getConfig();
        double expResult = 29.97;
        double result = instance.getSamplingFrequency();
        assertEquals(expResult, result, 0.0001);
    }

    /**
     * Test of getSensorNames method, of class SystemConfig.
     */
    @Test
    public void testGetSensorNames() throws Exception{
        System.out.println("getSensorNames");
        SystemConfig instance = SystemConfig.getConfig();
        Set<String> result = instance.getSensorNames();
        assertEquals(0, result.size());
        
        StringReader sr = new StringReader(TWO_NAMES_30SPS);
        XMLStreamReader toReadFrom = XMLInputFactory.newFactory().createXMLStreamReader(sr);
        instance.readFrom(toReadFrom);
        assertEquals(2, instance.getSensorNames().size());
        assertTrue(instance.getSensorNames().contains("Name 1"));
        assertTrue(instance.getSensorNames().contains("Name 2"));
    }

    /**
     * Test of getConfig method, of class SystemConfig.
     */
    @Test
    public void testGetConfig() {
        System.out.println("getConfig");
        assertNotNull(SystemConfig.getConfig());
    }

    /**
     * Test of writeTo method, of class SystemConfig.
     */
    @Test
    public void testWriteTo() throws Exception {
        System.out.println("writeTo");
        StringWriter sw = new StringWriter();
        XMLStreamWriter toWriteTo = XMLOutputFactory.newFactory().createXMLStreamWriter(sw);
        SystemConfig instance = SystemConfig.getConfig();
        instance.writeTo(toWriteTo);
        String output = sw.toString();
        String expected = DEFAULT;
        assertTrue(expected.equals(output));
    }

    /**
     * Test of readFrom method, of class SystemConfig.
     */
    @Test
    public void testReadFrom() throws Exception {
        System.out.println("readFrom");
        SystemConfig instance = SystemConfig.getConfig();
        assertEquals(29.97, instance.getSamplingFrequency(), 0.0001);
        assertEquals(0, instance.getSensorNames().size());
        
        StringReader sr = new StringReader(TWO_NAMES_30SPS);
        XMLStreamReader toReadFrom = XMLInputFactory.newFactory().createXMLStreamReader(sr);
        instance.readFrom(toReadFrom);
        assertEquals(30, instance.getSamplingFrequency(), 0.0001);
        assertEquals(2, instance.getSensorNames().size());
        assertTrue(instance.getSensorNames().contains("Name 1"));
        assertTrue(instance.getSensorNames().contains("Name 2"));
    }
}
