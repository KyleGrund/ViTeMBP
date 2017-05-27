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
package com.vitembp.embedded.hardware;

import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the AccelerometerMock class.
 */
public class MockAccelerometerTest {
    
    public MockAccelerometerTest() {
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
     * Test of getType method, of class AccelerometerMock.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        AccelerometerMock instance = new AccelerometerMock("Mock Acccelerometer");
        UUID expResult = UUID.fromString("3906c164-82c8-48f8-a154-a39a9d0269fa");
        UUID result = instance.getType();
        assertEquals(expResult, result);
    }

    /**
     * Test of initialize method, of class AccelerometerMock.
     */
    @Test
    public void testInitialize() {
        System.out.println("initialize");
        AccelerometerMock instance = new AccelerometerMock("Mock Acccelerometer");
        instance.initialize();
    }

    /**
     * Test of readSample method, of class AccelerometerMock.
     */
    @Test
    public void testReadSample() {
        System.out.println("readSample");
        AccelerometerMock instance = new AccelerometerMock("Mock Acccelerometer");
        String result = instance.readSample().replace("(", "").replace(")", "");
        String[] values = result.split(",");
        assertEquals(3, values.length);
        double item1 = Double.parseDouble(values[0]);
        double item2 = Double.parseDouble(values[1]);
        double item3 = Double.parseDouble(values[2]);
    }
}
