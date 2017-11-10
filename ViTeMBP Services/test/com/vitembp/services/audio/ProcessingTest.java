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
package com.vitembp.services.audio;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
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
public class ProcessingTest {
    
    public ProcessingTest() {
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
     * Test of findSyncFrames method, of class Processing.
     */
    @Test
    public void testFindSyncFrames() throws Exception {
        System.out.println("findSyncFrames");
        
        // get refrence to test file
        URL reso = getClass().getResource("GOPR0089.MP4");
        String sourceFile = null;
        try {
            sourceFile = new File(URLDecoder.decode(reso.getFile(), "UTF-8")).getAbsolutePath();
        } catch (UnsupportedEncodingException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
        
        double signalFrequency = 3000.0;
        List<Integer> expResult = Arrays.asList(new Integer[] { 62 });
        List<Integer> result = Processing.findSyncFrames(sourceFile, signalFrequency);
        assertEquals(result.size(), 1);
    }
    
}
