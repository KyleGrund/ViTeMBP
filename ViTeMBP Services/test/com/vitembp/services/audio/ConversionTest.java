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

import com.vitembp.services.video.Conversion;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author kgrund
 */
public class ConversionTest {
    
    public ConversionTest() {
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
     * Test of extractWaveAudio method, of class Conversion.
     */
    @Test
    public void testExtractWaveAudio() throws Exception {
        System.out.println("extractWaveAudio");
        
        URL reso = com.vitembp.services.video.ConversionTest.class.getResource("GOPR0026.MP4");
        String source = null;
        try {
            source = new File(URLDecoder.decode(reso.getFile(), "UTF-8")).getAbsolutePath();
        } catch (UnsupportedEncodingException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
        
        // build a temporary directory for images
        Path tempDir = Files.createTempDirectory("vitempbTest");
        
        com.vitembp.services.video.Conversion.extractWaveAudio(source, tempDir.resolve("test.wav").toString());
        
        // delete all files and make sure the audio file was extracted
        int filesCount = DeleteTree(tempDir);
        Assert.assertEquals(1, filesCount);
    }

    /**
     * Test of copyAudio method, of class Conversion.
     */
    @Test
    public void testCopyAudio() throws Exception {
        System.out.println("copyAudio");
        
        String sourceFilename = "GOPR0026.MP4";
        String destFilename = "GOPR0026dest.MP4";
        
        // build a temporary directory for videos
        Path tempDir = Files.createTempDirectory("vitempbTest");
        
        URL reso = com.vitembp.services.video.ConversionTest.class.getResource(sourceFilename);
        File source = new File(URLDecoder.decode(reso.getFile(), "UTF-8"));
        
        // define source and destination filenames
        Path sourceFile = tempDir.resolve(sourceFilename);
        Path destFile = tempDir.resolve(destFilename);
        
        // copy test video to the source and destination locations
        Files.copy(source.toPath(), sourceFile);
        Files.copy(source.toPath(), destFile);
        
        // perform the copy
        Conversion.copyAudio(sourceFile, destFile);
        
        // clean up the temp dir
        Assert.assertEquals(2, this.DeleteTree(tempDir));
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
