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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class UuidStringStoreCaptureTest {
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
     * Initializes a new instance of the UuidStringStoreCaptureTest class.
     */
    public UuidStringStoreCaptureTest() {
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
            Map<String, String> sensorData = new HashMap<>();
            sensorData.put(sensorOne.getBinding(), sensorOne.readSample());
            sensorData.put(sensorTwo.getBinding(), sensorTwo.readSample());
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
    private UuidStringStoreCapture createInstance() {
        // build a map of sensor types for the capture
        HashMap<String, UUID> sensorTypes = new HashMap<>();
        sensorTypes.put(SENSOR_NAMES[0], SENSOR_TYPE_UUID);
        sensorTypes.put(SENSOR_NAMES[1], SENSOR_TYPE_UUID);
        UuidStringStore hashMapStore = new UuidStringStore() {
            private final Map<UUID, String> dataStore = new HashMap<>();
            
            @Override
            public String read(UUID key) {
                return this.dataStore.get(key);
            }

            @Override
            public void write(UUID key, String value) {
                this.dataStore.put(key, value);
            }
        };
        UuidStringStoreCapture instance = new UuidStringStoreCapture(Instant.EPOCH, 29.97, hashMapStore, sensorTypes);
        this.data.forEach(instance::addSample);
        return instance;
    }

    /**
     * Test of getSamples method, of class UuidStringStoreCapture.
     */
    @Test
    public void testGetSamples() {
        System.out.println("getSamples");
        UuidStringStoreCapture instance = this.createInstance();
        CaptureTests.testGetSamples(instance, samples, org.junit.Assert::assertEquals, org.junit.Assert::assertTrue);
    }

    /**
     * Test of addSample method, of class UuidStringStoreCapture.
     */
    @Test
    public void testAddSample_Map() {
        System.out.println("addSample");
        UuidStringStoreCapture instance = this.createInstance();
        this.data.forEach(instance::addSample);
    }

    /**
     * Test of save method, of class UuidStringStoreCapture.
     */
    @Test
    public void testSave() throws Exception {
        System.out.println("save");
        UuidStringStoreCapture instance = this.createInstance();
        instance.save();
    }

    /**
     * Test of load method, of class UuidStringStoreCapture.
     */
    @Test
    public void testLoad() throws Exception {
        System.out.println("load");
        UuidStringStoreCapture instance = this.createInstance();
        instance.save();
        instance.load();
    }

    /**
     * Test of getSensorNames method, of class UuidStringStoreCapture.
     */
    @Test
    public void testGetSensorNames() {
        System.out.println("getSensorNames");
        UuidStringStoreCapture instance = this.createInstance();
        Set<String> expResult = new HashSet<>();
        expResult.add(UuidStringStoreCaptureTest.SENSOR_NAMES[0]);
        expResult.add(UuidStringStoreCaptureTest.SENSOR_NAMES[1]);
        Set<String> result = instance.getSensorNames();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSensorTypes method, of class UuidStringStoreCapture.
     */
    @Test
    public void testGetSensorTypes() {
        System.out.println("getSensorTypes");
        UuidStringStoreCapture instance = this.createInstance();
        Map<String, UUID> expResult = new HashMap<>();
        expResult.put(SENSOR_NAMES[0], SENSOR_TYPE_UUID);
        expResult.put(SENSOR_NAMES[1], SENSOR_TYPE_UUID);
        Map<String, UUID> result = instance.getSensorTypes();
        assertEquals(expResult, result);
    }

    /**
     * Test of addSample method, of class UuidStringStoreCapture.
     */
    @Test
    public void testAddSample_Sample() {
        System.out.println("addSample");
        Sample toAdd = this.samples.get(0);
        UuidStringStoreCapture instance = this.createInstance();
        instance.addSample(toAdd);
    }    
}
