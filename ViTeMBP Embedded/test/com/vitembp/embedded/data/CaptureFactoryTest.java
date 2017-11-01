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
 * @author Kyle
 */
public class CaptureFactoryTest {
    
    public CaptureFactoryTest() {
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
     * Test of buildCapture method, of class CaptureFactory.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildCaptureH2() throws Exception {
        System.out.println("buildCaptureH2");
        CaptureTypes type = CaptureTypes.EmbeddedH2;
        double frequency = 29.9;
        Map<String, UUID> nameToIds = new HashMap<>();
        Map<String, String> nameToCal = new HashMap<>();
        nameToIds.put("Sensor 1", UUID.randomUUID());
        Capture result = CaptureFactory.buildCapture(type, frequency, nameToIds, nameToCal);
        assertNotNull(result);
    }
    
    /**
     * Test of buildCapture method, of class CaptureFactory.
     * @throws java.lang.Exception
     */
    @Test
    public void testBuildCaptureDynamoDB() throws Exception {
        System.out.println("buildCaptureDynamoDB");
        CaptureTypes type = CaptureTypes.AmazonDynamoDB;
        double frequency = 29.9;
        Map<String, UUID> nameToIds = new HashMap<>();
        Map<String, String> nameToCal = new HashMap<>();
        nameToIds.put("Sensor 1", UUID.randomUUID());
        Capture result = CaptureFactory.buildCapture(type, frequency, nameToIds, nameToCal);
        assertNotNull(result);
    }

    /**
     * Test of getCaptures method, of class CaptureFactory.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetCapturesH2() throws Exception {
        System.out.println("getCaptures H2");
        CaptureTypes type = CaptureTypes.EmbeddedH2;
        double frequency = 29.9;
        Map<String, UUID> nameToIds = new HashMap<>();
        Map<String, String> nameToCal = new HashMap<>();
        nameToIds.put("Sensor 1", UUID.randomUUID());
        Capture result = CaptureFactory.buildCapture(type, frequency, nameToIds, nameToCal);
        result.save();
        Iterable<Capture> captures = CaptureFactory.getCaptures(CaptureTypes.EmbeddedH2);
        List<Capture> loaded = new ArrayList<>();
        captures.forEach(loaded::add);
        assertTrue(loaded.size() > 0);
    }
    
    /**
     * Test of getCaptures method, of class CaptureFactory.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetCapturesDynamoDB() throws Exception {
        System.out.println("getCaptures DynamoDB");
        CaptureTypes type = CaptureTypes.AmazonDynamoDB;
        double frequency = 29.9;
        Map<String, UUID> nameToIds = new HashMap<>();
        Map<String, String> nameToCal = new HashMap<>();
        Capture result = CaptureFactory.buildCapture(type, frequency, nameToIds, nameToCal);
        result.save();
        Iterable<Capture> captures = CaptureFactory.getCaptures(CaptureTypes.AmazonDynamoDB);
        List<Capture> loaded = new ArrayList<>();
        captures.forEach(loaded::add);
        assertTrue(loaded.size() > 0);
    }
}
