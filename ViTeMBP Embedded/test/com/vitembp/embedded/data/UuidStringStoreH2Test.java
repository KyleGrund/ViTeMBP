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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
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
public class UuidStringStoreH2Test {
    
    public UuidStringStoreH2Test() {
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
     * Test of instantiation of class UuidStringStoreH2.
     * @throws java.io.IOException
     * @throws java.lang.InstantiationException
     */
    @Test
    public void testInstantiate() throws IOException, InstantiationException {
        System.out.println("instantiate");
        // instantiate the connector
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.EmbeddedH2);
        assertNotNull(instance);
    }
    
    /**
     * Test of read method, of class UuidStringStoreH2.
     * @throws java.lang.InstantiationException
     * @throws java.io.IOException
     */
    @Test
    public void testRead() throws InstantiationException, IOException {
        System.out.println("read");        
        // instantiate the connector
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.EmbeddedH2);

        UUID key = UUID.randomUUID();
        String expResult = "A test string.";
        instance.write(key, expResult);
        String result = instance.read(key);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of delete method, of class UuidStringStoreH2.
     * @throws java.lang.InstantiationException
     * @throws java.io.IOException
     */
    @Test
    public void testDelete() throws InstantiationException, IOException {
        System.out.println("delete");        
        // instantiate the connector
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.EmbeddedH2);

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
     * Test of write method, of class UuidStringStoreH2.
     * @throws java.lang.InstantiationException
     * @throws java.io.IOException
     */
    @Test
    public void testWrite() throws InstantiationException, IOException {
        System.out.println("write");
        UUID key = UUID.randomUUID();
        String value = "A test string.";
        // instantiate the connector
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.EmbeddedH2);

        instance.write(key, value);
    }
    
    /**
     * Test of write method, of class UuidStringStoreH2.
     * @throws java.lang.InstantiationException
     * @throws java.io.IOException
     */
    @Test
    public void testUpdate() throws InstantiationException, IOException {
        System.out.println("write");
        UUID key = UUID.randomUUID();

        // instantiate the connector
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.EmbeddedH2);

        String expected = "A test string.";
        instance.write(key, expected);
        assertTrue(expected.equals(instance.read(key)));

        expected = "A different string.";
        instance.write(key, expected);
        assertTrue(expected.equals(instance.read(key)));
    }
    
    /**
     * Test of getKeys method, of class UuidStringStoreH2.
     * @throws java.lang.InstantiationException
     * @throws java.io.IOException
     */
    @Test
    public void testGetKeys() throws InstantiationException, IOException {
        System.out.println("read");        
        // instantiate the connector
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.EmbeddedH2);
        // the count before the write
        long before = instance.getKeys().count();
        UUID key = UUID.randomUUID();
        String expResult = "A test string.";
        instance.write(key, expResult);

        // assert one entry was added
        assertEquals(1, instance.getKeys().count() - before);

        // assert the added entry has the right key
        assertEquals(1, instance.getKeys().filter((id) -> key.equals(id)).count());
    }
    
    /**
     * Test of read method, of class UuidStringStoreH2.
     */
    @Test
    public void testFuzzReadWrite() throws InstantiationException, IOException {
        System.out.println("read write fuzz");        
        // instantiate the connector
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.EmbeddedH2);
        
        Random rnd = new Random();
        int iterations = rnd.nextInt(10000) + 1;
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
     * Test of getCaptureLocations method, of class UuidStringStoreH2.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetCaptureLocations() throws Exception {
        System.out.println("getCaptureLocations");
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.EmbeddedH2);
        UUID id = UUID.randomUUID();
        UuidStringLocation loc = new UuidStringLocation(instance, id);
        UuidStringStorePagingCapture usspc = new UuidStringStorePagingCapture(29.9, loc, 299, new HashMap<>());
        instance.addCaptureDescription(usspc, id);
        assertTrue(instance.getCaptureLocations().anyMatch((uid) -> id.equals(uid.getLocation())));
    }

    /**
     * Test of addCaptureDescription method, of class UuidStringStoreH2.
     */
    @Test
    public void testAddCapture() throws Exception {
        System.out.println("addCapture");
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.EmbeddedH2);
        UUID id = UUID.randomUUID();
        UuidStringLocation loc = new UuidStringLocation(instance, id);
        UuidStringStorePagingCapture usspc = new UuidStringStorePagingCapture(29.9, loc, 299, new HashMap<>());
        instance.addCaptureDescription(usspc, id);
    }

    /**
     * Test of getHashes method, of class UuidStringStoreH2.
     */
    @Test
    public void testGetHashes() throws Exception {
        System.out.println("getHashes");
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.EmbeddedH2);
        UUID id = UUID.randomUUID();
        UuidStringLocation loc = new UuidStringLocation(instance, id);
        UuidStringStorePagingCapture usspc = new UuidStringStorePagingCapture(29.9, loc, 299, new HashMap<>());
        instance.addCaptureDescription(usspc, id);
        List<UUID> locations = new ArrayList<>();
        instance.getCaptureLocations().map((d) -> d.getLocation()).forEach(locations::add);
        Map<UUID, String> hashes = instance.getHashes(locations);
        Stream<UUID> found = StreamSupport.stream(hashes.keySet().spliterator(), false);
        assertTrue(found.anyMatch((uid) -> id.equals(uid)));
    }

    /**
     * Test of addCaptureDescription method, of class UuidStringStoreDynamoDB.
     */
    @Test
    public void testAddCaptureDescription() throws Exception {
        System.out.println("addCaptureDescription H2");
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.EmbeddedH2);
        UUID locationID = UUID.randomUUID();
        Double freq = new Random().nextDouble();
        Map<String, UUID> names = new HashMap<>();
        names.put("Sensor 1", UUID.randomUUID());
        Capture toAdd = new UuidStringStoreCapture(
                freq,
                new UuidStringLocation(instance, locationID),
                names);
        instance.addCaptureDescription(toAdd, locationID);
        Stream<CaptureDescription> result = instance.getCaptureLocations();
        assertTrue(result.anyMatch((cap) -> cap.getLocation().equals(locationID)));
        
    }

    /**
     * Test of removeCaptureDescription method, of class UuidStringStoreDynamoDB.
     */
    @Test
    public void testRemoveCaptureDescription() throws Exception {
        System.out.println("removeCaptureDescription H2");
        // add a capture description object
        UuidStringStore instance = UuidStringStoreFactory.build(CaptureTypes.EmbeddedH2);
        UUID locationID = UUID.randomUUID();
        Double freq = new Random().nextDouble();
        Map<String, UUID> names = new HashMap<>();
        names.put("Sensor 1", UUID.randomUUID());
        Capture toAdd = new UuidStringStoreCapture(
                freq,
                new UuidStringLocation(instance, locationID),
                names);
        instance.addCaptureDescription(toAdd, locationID);
        
        // make sure it was added
        Stream<CaptureDescription> result = instance.getCaptureLocations();
        CaptureDescription added = result
                .filter((cap) -> cap.getLocation().equals(locationID))
                .findFirst()
                .get();
        assertNotNull(added);
        
        // remove it
        instance.removeCaptureDescription(added);
        
        // ensure it was removed
        assertFalse(instance.getCaptureLocations().anyMatch((cap) -> cap.getLocation().equals(locationID)));
    }
}
