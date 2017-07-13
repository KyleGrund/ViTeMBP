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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
public class UuidStringStorePagingCaptureTest {
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
    
    public UuidStringStorePagingCaptureTest() {
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

    private UuidStringStorePagingCapture buildCapture() throws InstantiationException {
        double frequency = 29.9;
        UuidStringStoreHashMap memStore = (UuidStringStoreHashMap)UuidStringStoreFactory.build(CaptureTypes.InMemory);
        UuidStringLocation store = new UuidStringLocation(memStore, UUID.randomUUID());
        // model one page every 10sec
        int pageSize = 299;
        // build a map of sensor types for the capture
        HashMap<String, UUID> nameToIds = new HashMap<>();
        nameToIds.put(SENSOR_NAMES[0], SENSOR_TYPE_UUID);
        nameToIds.put(SENSOR_NAMES[1], SENSOR_TYPE_UUID);
        
        return new UuidStringStorePagingCapture(frequency, store, pageSize, nameToIds);
    }
    
    /**
     * Generates data and adds it to the capture.
     * @param instance The capture to add.
     * @return The data which was added.
     */
    private List<Map<String, String>> seedData(Capture instance, int samples) {
        List<Map<String, String>> dataToAdd = new ArrayList<>();
        for (int i = 0; i < samples; i++) {
            Map<String, String> toAdd = new HashMap<>();
            toAdd.put(SENSOR_NAMES[0], "Sensor 0 index " + Integer.toString(i));
            toAdd.put(SENSOR_NAMES[1], "Sensor 1 index " + Integer.toString(i));
            dataToAdd.add(toAdd);
        }
        
        // add the data to the capture
        dataToAdd.forEach(instance::addSample);
        return dataToAdd;
    }
    
    /**
     * Test of getSamples method, of class UuidStringStorePagingCapture, when
     * it contains no samples.
     */
    @Test
    public void testGetZeroSamples() throws InstantiationException {
        System.out.println("getSamples");
        UuidStringStorePagingCapture instance = buildCapture();
        Iterator<Sample> result = instance.getSamples().iterator();
        assertFalse(result.hasNext());
    }
    
    /**
     * Test of getSamples method, of class UuidStringStorePagingCapture.
     */
    @Test
    public void testGetSamples() throws InstantiationException {
        System.out.println("getSamples");
        UuidStringStorePagingCapture instance = buildCapture();
        
        // create some data to add
        List<Map<String, String>> dataToAdd = seedData(instance, 3000);
        
        // verify the data was added
        Iterator<Map<String, String>> expResult = dataToAdd.iterator();
        Iterator<Sample> result = instance.getSamples().iterator();
        while (expResult.hasNext()) {
            Map<String, String> exp = expResult.next();
            Map<String, String> res = result.next().getSensorData();
            assertEquals(res.size(), exp.size());
            exp.keySet().forEach((key) -> assertEquals(exp.get(key), res.get(key)));
        }
        
        // verify both iteraters do not have more data
        assertFalse(expResult.hasNext());
        assertFalse(result.hasNext());
    }

    /**
     * Test of save method, of class UuidStringStorePagingCapture.
     */
    @Test
    public void testSave() throws Exception {
        System.out.println("save");
        UuidStringStorePagingCapture instance = this.buildCapture();
        this.seedData(instance, 3000);
        instance.save();
    }

    /**
     * Test of load method, of class UuidStringStorePagingCapture.
     */
    @Test
    public void testLoad() throws Exception {
        System.out.println("load");
        double frequency = 29.9;
        UuidStringStoreHashMap memStore = (UuidStringStoreHashMap)UuidStringStoreFactory.build(CaptureTypes.InMemory);
        UuidStringLocation store = new UuidStringLocation(memStore, UUID.randomUUID());
        // model one page every 10sec
        int pageSize = 299;
        // build a map of sensor types for the capture
        HashMap<String, UUID> nameToIds = new HashMap<>();
        nameToIds.put(SENSOR_NAMES[0], SENSOR_TYPE_UUID);
        nameToIds.put(SENSOR_NAMES[1], SENSOR_TYPE_UUID);
        
        UuidStringStorePagingCapture instance = new UuidStringStorePagingCapture(frequency, store, pageSize, nameToIds);
        List<Map<String, String>> addedData = this.seedData(instance, 3000);
        instance.save();
        
        instance = new UuidStringStorePagingCapture(frequency, store, pageSize, nameToIds);
        assertEquals(0, instance.getSampleCount());
        
        instance.load();
        assertEquals(3000, instance.getSampleCount());
        
        // verify the data was added
        Iterator<Map<String, String>> expResult = addedData.iterator();
        Iterator<Sample> result = instance.getSamples().iterator();
        while (expResult.hasNext()) {
            Map<String, String> exp = expResult.next();
            Map<String, String> res = result.next().getSensorData();
            assertEquals(res.size(), exp.size());
            exp.keySet().forEach((key) -> assertEquals(exp.get(key), res.get(key)));
        }
        
        // verify both iteraters do not have more data
        assertFalse(expResult.hasNext());
        assertFalse(result.hasNext());
    }

    /**
     * Test of getSensorNames method, of class UuidStringStorePagingCapture.
     */
    @Test
    public void testGetSensorNames() throws InstantiationException {
        System.out.println("getSensorNames");
        UuidStringStorePagingCapture instance = this.buildCapture();
        Set<String> expResult = new HashSet<>();
        expResult.add(SENSOR_NAMES[0]);
        expResult.add(SENSOR_NAMES[1]);
        Set<String> result = instance.getSensorNames();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSensorTypes method, of class UuidStringStorePagingCapture.
     */
    @Test
    public void testGetSensorTypes() throws InstantiationException {
        System.out.println("getSensorTypes");
        UuidStringStorePagingCapture instance = this.buildCapture();
        Map<String, UUID> expResult = new HashMap<>();
        expResult.put(SENSOR_NAMES[0], SENSOR_TYPE_UUID);
        expResult.put(SENSOR_NAMES[1], SENSOR_TYPE_UUID);
        Map<String, UUID> result = instance.getSensorTypes();
        assertEquals(expResult, result);
    }    
}
