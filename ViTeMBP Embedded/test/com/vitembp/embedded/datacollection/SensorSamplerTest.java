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
package com.vitembp.embedded.datacollection;

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
                Arrays.asList(Platform.getPlatform().getSensors().toArray(new Sensor[] {})),
                (data) -> { counter.put("samples", counter.get("samples") + 1); });
        instance.start();
        Thread.sleep(1000);
        instance.stop();
        System.out.println("Got " + counter.get("samples") + " samples.");
        Assert.assertEquals(30l, (long)counter.get("samples"));
    }
}
