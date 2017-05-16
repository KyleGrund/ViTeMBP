/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vitembp.embedded.data;

import com.vitembp.embedded.data.SensorSampler;
import com.vitembp.embedded.hardware.Platform;
import com.vitembp.embedded.hardware.Sensor;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author kyle
 */
public class SensorSamplerTest {
    
    public SensorSamplerTest() {
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
     * Test of start and stop methods, of class SensorSampler.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testStartStop() throws InterruptedException {
        System.out.println("start");
        final HashMap<String, Integer> counter = new HashMap<>();
        counter.put("samples", 0);
        
        SensorSampler instance = new SensorSampler(
                29.97,
                Arrays.asList(Platform.getPlatform().getSensorMap().values().toArray(new Sensor[] {})),
                (data) -> { counter.put("samples", counter.get("samples") + 1); });
        instance.start();
        Thread.sleep(1000);
        instance.stop();
        System.out.println("Got " + counter.get("samples") + " samples.");
        Assert.assertEquals(30l, (long)counter.get("samples"));
    }
}
