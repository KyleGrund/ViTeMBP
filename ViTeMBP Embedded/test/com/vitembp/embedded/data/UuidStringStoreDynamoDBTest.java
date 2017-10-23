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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;
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
public class UuidStringStoreDynamoDBTest {
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
    
    public UuidStringStoreDynamoDBTest() {
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
     * Test of instantiation of class UuidStringStoreDynamoDB.
     */
    @Test
    public void testInstantiate() throws InstantiationException {
        System.out.println("instantiate-DynamoDB");

        // instantiate the connector
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.AmazonDynamoDB);
    }
    
    /**
     * Test of read method, of class UuidStringStoreDynamoDB.
     */
    @Test
    public void testRead() throws IOException, InstantiationException {
        System.out.println("read-DynamoDB");

        // instantiate the connector
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.AmazonDynamoDB);

        UUID key = UUID.randomUUID();
        String expResult = "A test string.";
        instance.write(key, expResult);
        String result = instance.read(key);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of read method on empty location, of class UuidStringStoreDynamoDB.
     */
    @Test
    public void testReadEmpty() throws IOException, InstantiationException {
        System.out.println("read empty-DynamoDB");

        // instantiate the connector
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.AmazonDynamoDB);

        UUID key = UUID.randomUUID();
        String result = instance.read(key);
        assertNull(result);
    }

    /**
     * Test of write method, of class UuidStringStoreDynamoDB.
     * @throws java.lang.InstantiationException
     * @throws java.io.IOException
     */
    @Test
    public void testWrite() throws InstantiationException, IOException {
        System.out.println("write-DynamoDB");
        UUID key = UUID.randomUUID();
        String value = "A test string.";
        
        // instantiate the connector
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.AmazonDynamoDB);

        instance.write(key, value);
        
        key = UUID.randomUUID();
        value = "";

        instance.write(key, value);
    }
    
    /**
     * Test of read method, of class UuidStringStoreDynamoDB.
     * @throws java.lang.InstantiationException
     * @throws java.io.IOException
     */
    @Test
    public void testDelete() throws InstantiationException, IOException {
        System.out.println("delete-DynamoDB");
        // instantiate the connector
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.AmazonDynamoDB);

        UUID key = UUID.randomUUID();
        String expResult = "A test string.";
        instance.write(key, expResult);
        String result = instance.read(key);
        assertEquals(expResult, result);
        instance.delete(key);
        result = instance.read(key);
        assertNull(result);
    }
    
    /**
     * Test of write method, of class UuidStringStoreDynamoDB.
     */
    @Test
    public void testUpdate() throws InstantiationException, IOException {
        System.out.println("update-DynamoDB");
        UUID key = UUID.randomUUID();
        
        // instantiate the connector
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.AmazonDynamoDB);

        String expected = "A test string.";
        instance.write(key, expected);
        assertTrue(expected.equals(instance.read(key)));

        expected = "A different string.";
        instance.write(key, expected);
        assertTrue(expected.equals(instance.read(key)));
    }
    
    /**
     * Test of class UuidStringStoreDynamoDB through capture interface.
     * @throws java.lang.Exception If an Exception occurs during test.
     */
    @Test
    public void testGetCapture() throws Exception {
        // test using 
        Map<String, UUID> nameToIds = new HashMap<>();
        nameToIds.put(SENSOR_NAMES[0], SENSOR_TYPE_UUID);
        nameToIds.put(SENSOR_NAMES[1], SENSOR_TYPE_UUID);
        Capture toTest = CaptureFactory.buildCapture(CaptureTypes.AmazonDynamoDB, 29.9, nameToIds);
        CaptureTests.testWriteTo(toTest);
    }
    
    /**
     * Test of getKeys method, of class UuidStringStoreDynamoDB.
     */
    @Test
    public void testGetKeys() throws InstantiationException, IOException {
        System.out.println("getKeys-DynamoDB");        

        // instantiate the connector
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.AmazonDynamoDB);
        
        UUID key = UUID.randomUUID();
        String expResult = "A test string for ID scans.";
        instance.write(key, expResult);

        long keyCount = instance.getKeys().count();
        assertTrue(keyCount > 0);
    }
    
    /**
     * Test of read/write method, of class UuidStringStoreDynamoDB.
     * @throws java.lang.InstantiationException
     * @throws java.io.IOException
     */
    @Test
    public void testFuzzReadWrite() throws InstantiationException, IOException {
        System.out.println("read write fuzz-DynamoDB");        
        // instantiate the connector
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.AmazonDynamoDB);
        
        Random rnd = new Random();
        int iterations = rnd.nextInt(100) + 1;
        for (int i = 0; i < iterations; i++) {
            UUID key = UUID.randomUUID();
            StringBuilder sb = new StringBuilder();
            int len = rnd.nextInt(3000) + 1;
            while (sb.length() < len) {
                sb.append((char)rnd.nextInt(256));
            }
            String expResult = sb.toString();
            instance.write(key, expResult);
            String result = instance.read(key);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of getCaptureLocations method, of class UuidStringStoreDynamoDB.
     */
    @Test
    public void testGetCaptureLocations() throws Exception {
        System.out.println("getCaptureLocations DynamoDB");
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.AmazonDynamoDB);
        Map<String, UUID> names = new HashMap<>();
        names.put("Sensor 1", UUID.randomUUID());
        Double freq = new Random().nextDouble();
        Capture toAdd = CaptureFactory.buildCapture(CaptureTypes.AmazonDynamoDB, freq, names);
        Stream<CaptureDescription> result = instance.getCaptureLocations();
        assertTrue(result.anyMatch((cap) -> Math.abs(cap.getFrequency() - freq) < 0.00001));
    }

    /**
     * Test of addCaptureDescription method, of class UuidStringStoreDynamoDB.
     */
    @Test
    public void testAddCaptureDescription() throws Exception {
        System.out.println("addCaptureDescription DynamoDB");
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.AmazonDynamoDB);
        UUID locationID = UUID.randomUUID();
        Double freq = new Random().nextDouble();
        Map<String, UUID> names = new HashMap<>();
        names.put("Sensor 1", UUID.randomUUID());
        Capture toAdd = new UuidStringStoreCapture(
                () -> instance.removeCaptureDescription(locationID),
                freq,
                new UuidStringLocation(instance, locationID),
                names);
        instance.addCaptureDescription(new CaptureDescription(toAdd, locationID));
        Stream<CaptureDescription> result = instance.getCaptureLocations();
        assertTrue(result.anyMatch((cap) -> cap.getLocation().equals(locationID)));
        
    }

    /**
     * Test of removeCaptureDescription method, of class UuidStringStoreDynamoDB.
     */
    @Test
    public void testRemoveCaptureDescription() throws Exception {
        System.out.println("removeCaptureDescription DynamoDB");
        // add a capture description object
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.AmazonDynamoDB);
        UUID locationID = UUID.randomUUID();
        Double freq = new Random().nextDouble();
        Map<String, UUID> names = new HashMap<>();
        names.put("Sensor 1", UUID.randomUUID());
        Capture toAdd = new UuidStringStoreCapture(
                () -> instance.removeCaptureDescription(locationID),
                freq,
                new UuidStringLocation(instance, locationID),
                names);
        instance.addCaptureDescription(new CaptureDescription(toAdd, locationID));
        
        // make sure it was added
        Stream<CaptureDescription> result = instance.getCaptureLocations();
        CaptureDescription added = result
                .filter((cap) -> cap.getLocation().equals(locationID))
                .findFirst()
                .get();
        assertNotNull(added);
        
        // remove it
        instance.removeCaptureDescription(added.getLocation());
        
        // ensure it was removed
        assertFalse(instance.getCaptureLocations().anyMatch((cap) -> cap.getLocation().equals(locationID)));
    }
    
    /**
     * Test of getCaptureDescription method, of class UuidStringStoreDynamoDB.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetCaptureDescription() throws Exception {
        System.out.println("getCaptureDescription DynamoDB");
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.AmazonDynamoDB);
        UUID locationID = UUID.randomUUID();
        Double freq = new Random().nextDouble();
        Map<String, UUID> names = new HashMap<>();
        names.put("Sensor 1", UUID.randomUUID());
        Capture toAdd = new UuidStringStoreCapture(
                () -> instance.removeCaptureDescription(locationID),
                freq,
                new UuidStringLocation(instance, locationID),
                names);
        
        // make sure description is not present
        CaptureDescription desc = instance.getCaptureDescription(locationID);
        assertNull(desc);
        
        // add the capture and verify
        instance.addCaptureDescription(new CaptureDescription(toAdd, locationID));
        desc = instance.getCaptureDescription(locationID);
        assertNotNull(desc);
    }
}
