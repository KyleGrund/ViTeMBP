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

import com.vitembp.embedded.data.Capture;
import com.vitembp.embedded.data.InMemoryCapture;
import com.vitembp.embedded.data.Sample;
import com.vitembp.embedded.hardware.AccelerometerMock;
import com.vitembp.embedded.hardware.Sensor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for the CaptureSession class.
 */
public class CaptureSessionTest {
    
    public CaptureSessionTest() {
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
     * Tests creating and then starting and stopping a capture session using
     * mock sensor objects.
     * @throws InterruptedException If the thread wait is interrupted.
     */
    @Test
    public void testStartAndStopSession() throws InterruptedException {
        CaptureSession toTest;        
        List<Sensor> sensors = new ArrayList<>();
        sensors.add(new AccelerometerMock("Accelerometer 1"));
        sensors.add(new AccelerometerMock("Accelerometer 2"));
        
        // fill a hashmap with sensor types for crating capture
        Map<String, UUID> sensorTypes = new HashMap<>();
        sensors.forEach((s) -> sensorTypes.put(s.getName(), s.getType()));
        
        Capture capturedData = new InMemoryCapture(sensorTypes);
        toTest = new CaptureSession(29.97, sensors, capturedData);
        
        // take test data for 1 second
        toTest.start();
        Thread.sleep(1000);
        toTest.stop();
        
        // we should have taken 30 samples
        int count = 0;
        for (Sample sam : capturedData.getSamples()) {
            count ++;
        }
        Assert.assertEquals(30, count);
        
        // check each sample's attributes
        capturedData.getSamples().forEach((Sample sample) -> {
            // there should be two sensors in each sample
            Assert.assertEquals(2, sample.getSensorData().size());
        });
    }
}
