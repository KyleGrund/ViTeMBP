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
package com.vitembp.services.data;

import com.vitembp.embedded.data.Capture;
import com.vitembp.services.sensors.Captures;
import com.vitembp.services.sensors.RotarySensor;
import com.vitembp.services.sensors.Sensor;
import com.vitembp.services.sensors.SensorFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for CaptureProcessor class.
 */
public class CaptureProcessorTest {
    
    public CaptureProcessorTest() {
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
     * Test of process method, of class CaptureProcessor.
     * @throws java.lang.InstantiationException
     */
    @Test
    public void testProcess() throws InstantiationException {
        System.out.println("process");
        Capture source = Captures.createCapture();
        
        // add some unique data
        for (int i = 0; i <= 127; i++) {
            Map<String, String> toAdd = new HashMap<>();
            toAdd.put("Front Brake", Integer.toString(i % 128));
            toAdd.put("Rear Brake", Integer.toString(i % 128));
            toAdd.put("Front Shock", Integer.toString(i % 151));
            toAdd.put("Rear Shock", Integer.toString(i % 81));
            toAdd.put("Frame Accelerometer",
                    "(" + Integer.toString(i % 5) + "," +
                    Integer.toString((i + 2) % 5) + "," +
                    Integer.toString((i + 4) % 5) + ")");
            source.addSample(toAdd);
        }
        
        List<PipelineElement> pipe = new ArrayList<>();
        pipe.add(new CountElement("count"));
        RotarySensor brakeSensor = (RotarySensor)SensorFactory.getSensor("Front Brake", source.getSensorTypes().get("Front Brake"), "(12,20,31)");
        pipe.add(new SampleMaxValueElement(brakeSensor::getPositionDegrees, "max pos", brakeSensor));
        
        Map<String, Object> result = CaptureProcessor.process(source.getSamples(), new Pipeline(pipe));
        
        assertEquals(128L, (long)result.get("count"));
        assertEquals(595.5, (double)((Map<Sensor, Double>)result.get("max pos")).get(brakeSensor), 0.1);
    }
}
