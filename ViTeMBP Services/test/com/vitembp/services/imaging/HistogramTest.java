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
package com.vitembp.services.imaging;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Histogram class.
 * @author Kyle
 */
public class HistogramTest {
    
    public HistogramTest() {
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
     * Test of getBlueValues method, of class Histogram.
     */
    @Test
    public void testGetBlueValues() {
        // a function which will test the histogram values
        BiConsumer<String, Double> testBitmap = (String toTest, Double val) -> {
                List<Double> values = loadHistogram(toTest).getBlueValues();
                assertEquals(val, values.get(255), .0001);
                assertEquals(1 - val, values.get(0), .0001);
        };
        
        // run test function for each test bitmap
        testBitmap.accept("quarterBlue.png", 0.25);
        testBitmap.accept("quarterGreen.png", 0.0);
        testBitmap.accept("quarterRed.png", 0.0);
        testBitmap.accept("threeQuarterAlpha.png", 1.0);
    }
    
    /**
     * Test of getGreenValues method, of class Histogram.
     */
    @Test
    public void testGetGreenValues() {
        // a function which will test the histogram values
        BiConsumer<String, Double> testBitmap = (String toTest, Double val) -> {
                List<Double> values = loadHistogram(toTest).getGreenValues();
                assertEquals(val, values.get(255), .0001);
                assertEquals(1 - val, values.get(0), .0001);
        };
        
        // run test function for each test bitmap
        testBitmap.accept("quarterBlue.png", 0.0);
        testBitmap.accept("quarterGreen.png", 0.25);
        testBitmap.accept("quarterRed.png", 0.0);
        testBitmap.accept("threeQuarterAlpha.png", 1.0);
    }

    /**
     * Test of getRedValues method, of class Histogram.
     */
    @Test
    public void testGetRedValues() {
        // a function which will test the histogram values
        BiConsumer<String, Double> testBitmap = (String toTest, Double val) -> {
                List<Double> values = loadHistogram(toTest).getRedValues();
                assertEquals(val, values.get(255), .0001);
                assertEquals(1 - val, values.get(0), .0001);
        };
        
        // run test function for each test bitmap
        testBitmap.accept("quarterBlue.png", 0.0);
        testBitmap.accept("quarterGreen.png", 0.0);
        testBitmap.accept("quarterRed.png", 0.25);
        testBitmap.accept("threeQuarterAlpha.png", 1.0);
    }

    /**
     * Test of getAlphaValues method, of class Histogram.
     */
    @Test
    public void testGetAlphaValues() {
        // a function which will test the histogram values
        BiConsumer<String, Double> testBitmap = (String toTest, Double val) -> {
                List<Double> values = loadHistogram(toTest).getAlphaValues();
                assertEquals(val, values.get(255), .0001);
                assertEquals(1 - val, values.get(0), .0001);
        };
        
        // run test function for each test bitmap
        testBitmap.accept("quarterBlue.png", 1.0);
        testBitmap.accept("quarterGreen.png", 1.0);
        testBitmap.accept("quarterRed.png", 1.0);
        testBitmap.accept("threeQuarterAlpha.png", 0.25);
    }
    
    /**
     * Test of getBlueBrightness method, of class Histogram.
     */
    @Test
    public void testGetBlueBrightness() {
        // load the bitmap to test
        Histogram instance = loadHistogram("quarterBlue.png");
        
        // a quarter of the pixels expected to have a value of 255
        double expResult =  0.25 * 255;
        assertEquals(expResult, instance.getBlueBrightness(), 0.0001);
        
        // other colors should be absent, 0.0
        assertEquals(0.0, instance.getGreenBrightness(), 0.0001);
        assertEquals(0.0, instance.getRedBrightness(), 0.0001);
        
        // alpha channel should be completely 255
        assertEquals(255.0, instance.getAlphaBrightness(), 0.0001);
    }
    
    /**
     * Test of getGreenBrightness method, of class Histogram.
     */
    @Test
    public void testGetGreenBrightness() {
        // load the bitmap to test
        Histogram instance = loadHistogram("quarterGreen.png");
        
        // a quarter of the pixels expected to have a value of 255
        double expResult = 0.25 * 255;
        assertEquals(expResult, instance.getGreenBrightness(), 0.0001);

        // other colors should be absent, 0.0
        assertEquals(0.0, instance.getBlueBrightness(), 0.0001);
        assertEquals(0.0, instance.getRedBrightness(), 0.0001);
        
        // alpha channel should be completely 255
        assertEquals(255.0, instance.getAlphaBrightness(), 0.0001);
    }
    
    /**
     * Test of getRedBrightness method, of class Histogram.
     */
    @Test
    public void testGetRedBrightness() {
        // load the bitmap to test
        Histogram instance = loadHistogram("quarterRed.png");
        
        // a quarter of the pixels expected to have a value of 255
        double expResult =  0.25 * 255;
        assertEquals(expResult, instance.getRedBrightness(), 0.0001);
        
        // other colors should be absent, 0.0
        assertEquals(0.0, instance.getBlueBrightness(), 0.0001);
        assertEquals(0.0, instance.getGreenBrightness(), 0.0001);
        
        // alpha channel should be completely 255
        assertEquals(255.0, instance.getAlphaBrightness(), 0.0001);
    }
    
    /**
     * Test of getAlphaBrightness method, of class Histogram.
     */
    @Test
    public void testGetAlphaBrightness() {
        // load the bitmap to test
        Histogram instance = loadHistogram("threeQuarterAlpha.png");
        
        // a quarter of the pixels expected to have a value of 255
        double expResult =  0.25 * 255;
        assertEquals(expResult, instance.getAlphaBrightness(), 0.0001);

        // RGB channels should be completely 255 (white)
        assertEquals(255.0, instance.getBlueBrightness(), 0.0001);
        assertEquals(255.0, instance.getGreenBrightness(), 0.0001);
        assertEquals(255.0, instance.getRedBrightness(), 0.0001);
    }
    
    /**
     * Loads a histogram for a bitmap from the test package by filename.
     * @param name The name of the bitmap file.
     * @return A histogram of the bitmap file.
     */
    private Histogram loadHistogram(String name) {
        try {
            return new Histogram(getClass().getResourceAsStream(name));
        } catch (IOException ex) {
            fail("Could not open test bitmap: " + name + ". " + ex.getMessage());
        }
        return null;
    }
}
