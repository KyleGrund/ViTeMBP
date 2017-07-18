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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
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
}
