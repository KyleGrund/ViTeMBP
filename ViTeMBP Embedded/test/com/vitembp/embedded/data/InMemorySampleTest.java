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

import com.vitembp.embedded.hardware.MockAccelerometer;
import com.vitembp.embedded.hardware.Sensor;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
 * Unit tests for InMemorySample class.
 */
public class InMemorySampleTest {
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
    private final Map<Sensor, String> sensorData = new HashMap<>();
    
    public InMemorySampleTest() {
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
        Sensor sensorOne = new MockAccelerometer(SENSOR_NAMES[0]);
        Sensor sensorTwo = new MockAccelerometer(SENSOR_NAMES[1]);
        this.sensorData.put(sensorOne, sensorOne.readSample());
        this.sensorData.put(sensorTwo, sensorTwo.readSample());
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getIndex method, of class InMemorySample.
     */
    @Test
    public void testGetIndex() {
        System.out.println("getIndex");
        InMemorySample instance = new InMemorySample(1, Instant.EPOCH, this.sensorData);
        int expResult = 1;
        int result = instance.getIndex();
        assertEquals(expResult, result);
    }

    /**
     * Test of getTime method, of class InMemorySample.
     */
    @Test
    public void testGetTime() {
        System.out.println("getTime");
        InMemorySample instance = new InMemorySample(1, Instant.EPOCH, this.sensorData);
        Instant expResult = Instant.EPOCH;
        Instant result = instance.getTime();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSensorNames method, of class InMemorySample.
     */
    @Test
    public void testGetSensorNames() {
        System.out.println("getSensorNames");
        InMemorySample instance = new InMemorySample(1, Instant.EPOCH, this.sensorData);
        Set<String> expResult = new HashSet<>(Arrays.asList(SENSOR_NAMES));
        Set<String> result = instance.getSensorNames();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSensorTypes method, of class InMemorySample.
     */
    @Test
    public void testGetSensorTypes() {
        System.out.println("getSensorTypes");
        InMemorySample instance = new InMemorySample(1, Instant.EPOCH, this.sensorData);
        Map<String, UUID> expResult = new HashMap<>();
        expResult.put(SENSOR_NAMES[0], SENSOR_TYPE_UUID);
        expResult.put(SENSOR_NAMES[1], SENSOR_TYPE_UUID);
        Map<String, UUID> result = instance.getSensorTypes();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSensorData method, of class InMemorySample.
     */
    @Test
    public void testGetSensorData() {
        System.out.println("getSensorData");
        InMemorySample instance = new InMemorySample(1, Instant.EPOCH, this.sensorData);
        Map<String, String> expResult = new HashMap<>();
        this.sensorData.forEach((sensor, data) -> { expResult.put(sensor.getName(), data); });
        Map<String, String> result = instance.getSensorData();
        assertEquals(expResult, result);
    }
}
