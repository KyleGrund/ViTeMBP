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

import com.vitembp.embedded.hardware.AccelerometerMock;
import com.vitembp.embedded.hardware.Sensor;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
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
public class InMemoryCaptureTest {
    /**
     * The number of data samples to create for the tests in this class.
     */
    private static final int NUM_OF_SAMPLES = 10;
    
    /**
     * An array containing the names of the mock sensors we are generating data
     * for.
     */
    private static final String[] SENSOR_NAMES =
            new String[] { "Sensor One", "Sensor Two" };
    
    /**
     * A UUID representing the type of the mock sensor.
     */
    private static final UUID SENSOR_TYPE_UUID = UUID.fromString("3906c164-82c8-48f8-a154-a39a9d0269fa");
    
    /**
     * A sensor to data map to use during testing.
     */
    private final List<Sample> samples = new ArrayList<>();
    
    /**
     * Data to use when creating Capture instances.
     */
    private final List<Map<String, String>> data = new ArrayList<>();
    
    /**
     * Initializes a new instance of the InMemoryCaptureTest class.
     */
    public InMemoryCaptureTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        // create some sensor data to use during tests
        Sensor sensorOne = new AccelerometerMock();
        Sensor sensorTwo = new AccelerometerMock();
        
        // add some sensor data
        for (int i = 0; i < NUM_OF_SAMPLES; i++) {
            // create some sensor data
            Map<String, String> sensorData = new HashMap<>();
            sensorData.put(sensorOne.getSerial().toString(), sensorOne.readSample());
            sensorData.put(sensorTwo.getSerial().toString(), sensorTwo.readSample());
            data.add(sensorData);
            
            // create sample from data and add to samples
            samples.add(new Sample(i, Instant.now(), sensorData));
        }
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Builds an instance to test.
     * @return An instance to test.
     */
    private Capture createInstance() throws InstantiationException {
        // build a map of sensor types for the capture
        Map<String, String> nameToCal = new HashMap<>();
        nameToCal.put(SENSOR_NAMES[0], "(0)");
        nameToCal.put(SENSOR_NAMES[1], "(1)");
        HashMap<String, UUID> sensorTypes = new HashMap<>();
        sensorTypes.put(SENSOR_NAMES[0], SENSOR_TYPE_UUID);
        sensorTypes.put(SENSOR_NAMES[1], SENSOR_TYPE_UUID);
        Capture instance = CaptureFactory.buildCapture(
                CaptureTypes.InMemory, 
                29.97,
                sensorTypes,
                nameToCal);
        this.data.forEach(instance::addSample);
        return instance;
    }
    
    /**
     * Test of getSamples method, of class InMemoryCapture.
     */
    @Test
    public void testGetSamples() throws InstantiationException {
        System.out.println("getSamples");
        
        // build a map of sensor types for the capture
        Capture instance = createInstance();

        CaptureTests.testGetSamples(instance, this.samples, org.junit.Assert::assertEquals, org.junit.Assert::assertTrue);
    }

    /**
     * Test of addSample method, of class InMemoryCapture.
     */
    @Test
    public void testAddSample() throws InstantiationException {
        System.out.println("addSample");
        
        // build a map of sensor types for the capture
        Capture instance = createInstance();
        
        // count samples to make sure all data were added
        List<Sample> samples = new ArrayList<>();
        instance.getSamples().forEach(samples::add);
        assertEquals(data.size(), samples.size());
    }

    /**
     * Test of save method, of class InMemoryCapture.
     */
    @Test
    public void testSave() throws InstantiationException, IOException {
        System.out.println("save");
        
        // build a map of sensor types for the capture
        Capture instance = createInstance();
        
        instance.save();
    }

    /**
     * Test of load method, of class InMemoryCapture.
     */
    @Test
    public void testLoad() throws InstantiationException, IOException {
        System.out.println("load");
        
        // build a map of sensor types for the capture
        HashMap<String, UUID> sensorTypes = new HashMap<>();
        sensorTypes.put(SENSOR_NAMES[0], SENSOR_TYPE_UUID);
        sensorTypes.put(SENSOR_NAMES[1], SENSOR_TYPE_UUID);
        
        Capture instance = this.createInstance();
        instance.save();
        instance.load();
        
        this.data.forEach(instance::addSample);
        instance.save();
        instance.load();
    }
    
    /**
     * Test of load method, of class InMemoryCapture.
     */
    @Test
    public void testToXml() throws InstantiationException {
        System.out.println("toXml");
        
        Capture instance = createInstance();
        
        CaptureTests.testToXml(instance);
    }

    /**
     * Test of writeTo method, of class InMemoryCapture.
     */
    @Test
    public void testWriteTo() throws FactoryConfigurationError, XMLStreamException, InstantiationException {
        System.out.println("toXml");

        // build a map of sensor types for the capture
        Capture instance = createInstance();

        CaptureTests.testWriteTo(instance);
    }
}
