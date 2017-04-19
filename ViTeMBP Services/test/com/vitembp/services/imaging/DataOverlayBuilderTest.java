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

import java.io.File;
import java.io.IOException;
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
public class DataOverlayBuilderTest {
    
    public DataOverlayBuilderTest() {
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
     * Test of addText method, of class FrameBuilder.
     */
    @Test
    public void testAddText() {
        try {
            System.out.println("addText");
            String str = "A string.";
            int x = 10;
            int y = 10;
            DataOverlayBuilder instance = new DataOverlayBuilder(400, 400);
            instance.addText(str, x, y);
            File tempFile = File.createTempFile("addTextTest", ".png");
            tempFile.deleteOnExit();
            instance.saveImage(tempFile);
        } catch (IOException ex) {
            fail("An IOException occured during test: " + ex.getMessage());
        }
    }

    /**
     * Test of addProgressBar method, of class FrameBuilder.
     */
    @Test
    public void testAddProgressBar() {
        try {
            System.out.println("addProgressBar");
            float progressScaleFactor = 0.5F;
            int topLeftX = 10;
            int topLeftY = 10;
            int lowerRightX = 50;
            int lowerRightY = 100;
            DataOverlayBuilder instance = new DataOverlayBuilder(400, 400);
            instance.addVerticalProgressBar(progressScaleFactor, topLeftX, topLeftY, lowerRightX, lowerRightY);
            instance.addVerticalProgressBar(1.0f, topLeftX + 60, topLeftY, lowerRightX + 60, lowerRightY);
            instance.addVerticalProgressBar(0.0f, topLeftX, topLeftY + 110, lowerRightX, lowerRightY + 110);
            File tempFile = File.createTempFile("addProgressBarTest", ".png");
            tempFile.deleteOnExit();
            instance.saveImage(tempFile);
            
        } catch (IOException ex) {
            fail("An IOException occured during test: " + ex.getMessage());
        }
    }
    
}
