/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author kyle
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
     * Test of getType method, of class MockAccelerometer.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        MockAccelerometer instance = new MockAccelerometer("Mock Acccelerometer");
        UUID expResult = UUID.fromString("3906c164-82c8-48f8-a154-a39a9d0269fa");
        UUID result = instance.getType();
        assertEquals(expResult, result);
    }

    /**
     * Test of initialize method, of class MockAccelerometer.
     */
    @Test
    public void testInitialize() {
        System.out.println("initialize");
        MockAccelerometer instance = new MockAccelerometer("Mock Acccelerometer");
        instance.initialize();
    }

    /**
     * Test of readSample method, of class MockAccelerometer.
     */
    @Test
    public void testReadSample() {
        System.out.println("readSample");
        MockAccelerometer instance = new MockAccelerometer("Mock Acccelerometer");
        String result = instance.readSample().replace("(", "").replace(")", "");
        String[] values = result.split(",");
        assertEquals(3, values.length);
        double item1 = Double.parseDouble(values[0]);
        double item2 = Double.parseDouble(values[1]);
        double item3 = Double.parseDouble(values[2]);
    }
    
}
