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

import com.vitembp.services.data.Pipeline;
import com.vitembp.services.data.StandardPipelines;
import com.vitembp.services.data.CaptureProcessor;
import com.vitembp.embedded.data.Capture;
import com.vitembp.services.sensors.Captures;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
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
public class StandardPipelinesTest {
    
    public StandardPipelinesTest() {
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
     * Test of getVideoCaptureProcessingPipeline method, of class StandardPipelines.
     * @throws java.lang.InstantiationException If a capture cannot be created.
     */
    @Test
    public void testCaptureStatisticsPipeline() throws InstantiationException {
        System.out.println("getVideoCaptureProcessingPipeline");
        
        Capture source = Captures.createCapture();
        
        // add some unique data
        for (int i = 0; i <= 365; i++) {
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
        
        Function<Capture, Map<String, Object>> pipe = (cap) -> 
                CaptureProcessor.process(cap, StandardPipelines.captureStatisticsPipeline(source));
        Map<String, Object> results = pipe.apply(source);
        
        assertEquals(366L, (long)results.get(StandardPipelines.ELEMENT_COUNT_BINDING));
        
        assertEquals(174.72d, (double)results.get("Front Brake" + StandardPipelines.AVERAGE_SUFFIX), 0.01);
        assertEquals(0.0d, (double)results.get("Front Brake" + StandardPipelines.MIN_SUFFIX), 0.01);
        assertEquals(365.0d, (double)results.get("Front Brake" + StandardPipelines.MAX_SUFFIX), 0.01);
        
        assertEquals(174.72d, (double)results.get("Rear Brake" + StandardPipelines.AVERAGE_SUFFIX), 0.01);
        assertEquals(0.0d, (double)results.get("Rear Brake" + StandardPipelines.MIN_SUFFIX), 0.01);
        assertEquals(365.0d, (double)results.get("Rear Brake" + StandardPipelines.MAX_SUFFIX), 0.01);
        
        assertEquals(67.39d, (double)results.get("Front Shock" + StandardPipelines.AVERAGE_SUFFIX), 0.01);
        assertEquals(0.0d, (double)results.get("Front Shock" + StandardPipelines.MIN_SUFFIX), 0.01);
        assertEquals(150.0d, (double)results.get("Front Shock" + StandardPipelines.MAX_SUFFIX), 0.01);
        
        assertEquals(37.76d, (double)results.get("Rear Shock" + StandardPipelines.AVERAGE_SUFFIX), 0.01);
        assertEquals(0.0d, (double)results.get("Rear Shock" + StandardPipelines.MIN_SUFFIX), 0.01);
        assertEquals(80.0d, (double)results.get("Rear Shock" + StandardPipelines.MAX_SUFFIX), 0.01);
        
        assertEquals(2.00d, (double)results.get("Frame Accelerometer" + StandardPipelines.AVERAGE_X_SUFFIX), 0.1);
        assertEquals(0.0d, (double)results.get("Frame Accelerometer" + StandardPipelines.MIN_X_SUFFIX), 0.01);
        assertEquals(4.0d, (double)results.get("Frame Accelerometer" + StandardPipelines.MAX_X_SUFFIX), 0.01);
        
        assertEquals(2.00d, (double)results.get("Frame Accelerometer" + StandardPipelines.AVERAGE_Y_SUFFIX), 0.1);
        assertEquals(0.0d, (double)results.get("Frame Accelerometer" + StandardPipelines.MIN_Y_SUFFIX), 0.01);
        assertEquals(4.0d, (double)results.get("Frame Accelerometer" + StandardPipelines.MAX_Y_SUFFIX), 0.01);
        
        assertEquals(2.00d, (double)results.get("Frame Accelerometer" + StandardPipelines.AVERAGE_Y_SUFFIX), 0.1);
        assertEquals(0.0d, (double)results.get("Frame Accelerometer" + StandardPipelines.MIN_Y_SUFFIX), 0.01);
        assertEquals(4.0d, (double)results.get("Frame Accelerometer" + StandardPipelines.MAX_Y_SUFFIX), 0.01);
    }

    /**
     * Test of captureVideoOverlayPipeline method, of class StandardPipelines.
     */
    @Test
    public void testCaptureVideoOverlayPipeline() throws Exception {
        System.out.println("captureVideoOverlayPipeline");
        
        Capture source = Captures.createCapture();
        
        // add some unique data
        for (int i = 0; i <= 365; i++) {
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
        
        Path videoFile = null;
        URL reso = getClass().getResource("GOPR0054_1.mp4");
        try {
            videoFile = new File(URLDecoder.decode(reso.getFile(), "UTF-8")).toPath();
        } catch (UnsupportedEncodingException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
        
        Pipeline toTest = StandardPipelines.captureVideoOverlayPipeline(source, videoFile);
        
        Map<String, Object> results = CaptureProcessor.process(source, toTest);
    }    
}
