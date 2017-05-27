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
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    
    private final List<Map<Sensor, String>> data = new ArrayList<>();
    
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
        Sensor sensorOne = new AccelerometerMock(SENSOR_NAMES[0]);
        Sensor sensorTwo = new AccelerometerMock(SENSOR_NAMES[1]);
        
        // add some sensor data
        for (int i = 0; i < NUM_OF_SAMPLES; i++) {
            // create some sensor data
            Map<Sensor, String> sensorData = new HashMap<>();
            sensorData.put(sensorOne, sensorOne.readSample());
            sensorData.put(sensorTwo, sensorTwo.readSample());
            data.add(sensorData);
            
            // create sample from data and add to samples
            samples.add(new InMemorySample(i, Instant.now(), sensorData));
        }
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getSamples method, of class InMemoryCapture.
     */
    @Test
    public void testGetSamples() {
        System.out.println("getSamples");
        InMemoryCapture instance = new InMemoryCapture();
        this.data.forEach(instance::addSample);

        // convert samples to list
        List<Sample> found = new ArrayList();
        instance.getSamples().forEach(found::add);
        
        // check each element matches
        for (int i = 0; i < NUM_OF_SAMPLES; i++) {
            Sample a = found.get(i);
            Sample b = this.samples.get(i);
            assertEquals(i, a.getIndex());
            assertEquals(a.getIndex(), b.getIndex());
            assertEquals(a.getSensorData(), b.getSensorData());
            assertEquals(a.getSensorNames(), b.getSensorNames());
            assertEquals(a.getSensorTypes(), b.getSensorTypes());
            
            // sanity check times are within a second of the current time
            long aDiff = Instant.now().getEpochSecond() - a.getTime().getEpochSecond();
            long bDiff = Instant.now().getEpochSecond() - b.getTime().getEpochSecond();
            assertTrue(aDiff <= 1 && aDiff >= 0);
            assertTrue(bDiff <= 1 && bDiff >= 0);
        }
    }

    /**
     * Test of addSample method, of class InMemoryCapture.
     */
    @Test
    public void testAddSample() {
        System.out.println("addSample");
        InMemoryCapture instance = new InMemoryCapture();
        data.forEach(instance::addSample);
        
        // count samples to make sure all data were added
        List<Sample> samples = new ArrayList<>();
        instance.getSamples().forEach(samples::add);
        assertEquals(data.size(), samples.size());
    }

    /**
     * Test of save method, of class InMemoryCapture.
     */
    @Test
    public void testSave() {
        System.out.println("save");
        InMemoryCapture instance = new InMemoryCapture();
        instance.save();
        
        this.data.forEach(instance::addSample);
        instance.save();
    }

    /**
     * Test of load method, of class InMemoryCapture.
     */
    @Test
    public void testLoad() {
        System.out.println("load");
        InMemoryCapture instance = new InMemoryCapture();
        instance.load();
        
        this.data.forEach(instance::addSample);
        instance.load();
    }
    
}
