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
package com.vitembp.sensors;

import com.vitembp.data.CaptureProcessor;
import com.vitembp.data.SamplePipeline;
import com.vitembp.data.SamplePipelineCount;
import com.vitembp.data.SamplePipelineMaxValue;
import com.vitembp.embedded.data.Capture;
import com.vitembp.embedded.data.CaptureFactory;
import com.vitembp.embedded.data.CaptureTypes;
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
        Capture source = createCapture();
        
        for (int i = 0; i <= 127; i++) {
            Map<String, String> toAdd = new HashMap<>();
            toAdd.put("Front Brake", Integer.toString(i));
            toAdd.put("Rear Brake", Integer.toString(i));
            source.addSample(toAdd);
        }
        
        List<SamplePipeline> pipe = new ArrayList<>();
        pipe.add(new SamplePipelineCount());
        for (String name : source.getSensorNames()) {
            pipe.add(new SamplePipelineMaxValue(((RotarySensor)SensorFactory.getSensor(name, source.getSensorTypes().get(name)))::getPositionDegrees));
        }
        
        SamplePipeline[] pipeline = pipe.toArray(new SamplePipeline[] {});
        CaptureProcessor.process(source, pipeline);
        
        assertEquals(128, ((SamplePipelineCount)pipeline[0]).getCount());
        assertEquals(365d, ((SamplePipelineMaxValue)pipeline[1]).getMaxValue(), 0.001);
        assertEquals(365d, ((SamplePipelineMaxValue)pipeline[1]).getMaxValue(), 0.001);
    }
    
    
    private Capture createCapture() throws InstantiationException {
        Map<String, UUID> nameToIds = new HashMap<>();
        nameToIds.put("Front Brake", com.vitembp.sensors.RotaryEncoderEAW0J.TYPE_UUID);
        nameToIds.put("Rear Brake", com.vitembp.sensors.RotaryEncoderEAW0J.TYPE_UUID);
        return CaptureFactory.buildCapture(CaptureTypes.InMemory, 29.9, nameToIds);
    }
}
