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
package com.vitembp.services.video;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
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
public class VideoFileInfoTest {
    
    public VideoFileInfoTest() {
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
     * Test of getFrameRate method, of class VideoFileInfo.
     */
    @Test
    public void testGetFrameRate() {
        // get the absolute name of the test file
        URL reso = getClass().getResource("GOPR0026.MP4");
        String resoFile = null;
        try {
            resoFile = URLDecoder.decode(reso.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
        
        // load the file info
        VideoFileInfo instance = new VideoFileInfo(new File(resoFile));
        
        // check frame rate
        double expResult = 29.97;
        double result = instance.getFrameRate();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getDuration method, of class VideoFileInfo.
     */
    @Test
    public void testGetDuration() {
        // get the absolute name of the test file
        URL reso = getClass().getResource("GOPR0026.MP4");
        String resoFile = null;
        try {
            resoFile = URLDecoder.decode(reso.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
        
        // load the file info
        VideoFileInfo instance = new VideoFileInfo(new File(resoFile));
        
        // check duration
        double expResult = 5.04;
        double result = instance.getDuration();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getHorizontalResolution method, of class VideoFileInfo.
     */
    @Test
    public void testGetHorizontalResolution() {
        // get the absolute name of the test file
        URL reso = getClass().getResource("GOPR0026.MP4");
        String resoFile = null;
        try {
            resoFile = URLDecoder.decode(reso.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
        
        // load the file info
        VideoFileInfo instance = new VideoFileInfo(new File(resoFile));
        
        // check horizontal resolution
        int expResult = 1920;
        int result = instance.getHorizontalResolution();
        assertEquals(expResult, result);
    }

    /**
     * Test of getVerticalResolution method, of class VideoFileInfo.
     */
    @Test
    public void testGetVerticalResolution() {
        // get the absolute name of the test file
        URL reso = getClass().getResource("GOPR0026.MP4");
        String resoFile = null;
        try {
            resoFile = URLDecoder.decode(reso.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
        
        // load the file info
        VideoFileInfo instance = new VideoFileInfo(new File(resoFile));
        
        // check vertical resolution
        int expResult = 1080;
        int result = instance.getVerticalResolution();
        assertEquals(expResult, result);
    }   
}