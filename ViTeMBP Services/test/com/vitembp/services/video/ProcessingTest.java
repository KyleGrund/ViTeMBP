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

import com.vitembp.services.ApiFunctions.COLOR_CHANNELS;
import com.vitembp.services.FilenameGenerator;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
 * @author kyle
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
     * Test of findChannelSyncFrames method, of class Processing.
     */
    @Test
    public void testFindChannelSyncFrames_4args() throws Exception {
        System.out.println("findChannelSyncFrames");
        // build a temporary directory for images
        Path tempDir = Files.createTempDirectory("vitempbProcTest");
        
        URL reso = getClass().getResource("GOPR0013.MP4");
        String videoFile = null;
        try {
            videoFile = new File(URLDecoder.decode(reso.getFile(), "UTF-8")).getAbsolutePath();
        } catch (UnsupportedEncodingException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
        
        COLOR_CHANNELS channel = COLOR_CHANNELS.GREEN;
        FilenameGenerator fileGenerator = FilenameGenerator.PNG_NUMERIC_OUT;
        Path overlayOutput = tempDir.resolve("videoFile.MP4");
        List<Integer> expResult = Arrays.asList(new Integer[]{ 52, 53, 54, 55, 56, 57, 58, 59, 60, 126, 127, 128, 195, 196, 197 });
        List<Integer> result = Processing.findChannelSyncFrames(videoFile, channel, fileGenerator, overlayOutput);
        assertEquals(expResult, result);
        
        // make sure video was created and delete the directory
        int delCount = this.DeleteTree(tempDir);
        assertEquals(1, delCount);
    }

    /**
     * Test of findChannelSyncFrames method, of class Processing.
     */
    @Test
    public void testFindChannelSyncFrames_3args() throws Exception {
        System.out.println("findChannelSyncFrames");
        URL reso = getClass().getResource("GOPR0013.MP4");
        String videoFile = null;
        try {
            videoFile = new File(URLDecoder.decode(reso.getFile(), "UTF-8")).getAbsolutePath();
        } catch (UnsupportedEncodingException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
        
        COLOR_CHANNELS channel = COLOR_CHANNELS.GREEN;
        FilenameGenerator fileGenerator = FilenameGenerator.PNG_NUMERIC_OUT;
        List<Integer> expResult = Arrays.asList(new Integer[]{ 52, 53, 54, 55, 56, 57, 58, 59, 60, 126, 127, 128, 195, 196, 197 });
        List<Integer> result = Processing.findChannelSyncFrames(videoFile, channel, fileGenerator);
        assertEquals(expResult, result);
    }
    
    /**
     * Deletes all contents of the directory recursively.
     * @param toDelete The directory to delete.
     * @return The number of files that were deleted.
     * @throws IOException If an exception occurs when deleting directory tree.
     */
    private int DeleteTree(Path toDelete) throws IOException {
        // we use an array here to allow us to decalare it final for use inside
        // the anonymous class.
        final int[] filesCount = new int[] { 0 };
        
        // recursively delete all files in temp directory
        Files.walkFileTree(toDelete, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException
            {
                Files.delete(file);
                filesCount[0]++;
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e)
                    throws IOException
            {
                if (e == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    // directory iteration failed
                    throw e;
                }
            }
        });
        
        return filesCount[0];
    }
}
