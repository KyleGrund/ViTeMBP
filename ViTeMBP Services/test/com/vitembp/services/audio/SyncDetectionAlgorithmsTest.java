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

import com.vitembp.services.video.VideoFileInfo;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
public class SyncDetectionAlgorithmsTest {
    private final double signalFrequency = 3000;
    private final int signalFrameLength = 60;
    
    public SyncDetectionAlgorithmsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws IOException {
        
    }
    
    @After
    public void tearDown() {
    }
    
    /**
     * Loads the audio frame values for the given filename in the test package.
     * @param filename The name of the file containing the test video.
     * @return The frame data.
     * @throws IOException If the data cannot be read.
     */
    private List<Double> getFrameValues(String filename) throws IOException {
        // get refrence to test file
        URL reso = getClass().getResource(filename);
        String sourceFile = null;
        try {
            sourceFile = new File(URLDecoder.decode(reso.getFile(), "UTF-8")).getAbsolutePath();
        } catch (UnsupportedEncodingException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
        
        // create a temporary file for the output
        Path localTempOutput = Files.createTempFile("vitembp", ".wav");
        localTempOutput.toFile().delete();
        
        // extract audio
        com.vitembp.services.video.Conversion.extractWaveAudio(sourceFile, localTempOutput.toString());
        
        // get data frame chunks of data
        File fileIn = localTempOutput.toFile();
        
        // provides video file information, such as frame rate
        VideoFileInfo videoInfo = new VideoFileInfo(new File(sourceFile));
        
        // performs an SignalProcessing on the audio of each video frame and gets the value
        // at the target signal frequency
        List<Double> frameValues = SignalProcessing.getFrameFFTValues(fileIn, videoInfo, signalFrequency);
        
        // perform averaging to smooth noise
        frameValues = SignalProcessing.appplyAveragingWindow(frameValues, 6);
        
        // return the frame values
        return frameValues;
    }

    /**
     * Test of findSyncFramesByAveragingWindow method, of class SyncDetectionAlgorithms.
     */
    @Test
    public void testFindSyncFramesByAveragingWindow() throws IOException {
        System.out.println("findSyncFramesByAveragingWindow");
        Set<Integer> expResult = new HashSet<>(Arrays.asList(new Integer[]{60}));
        Set<Integer> result = SyncDetectionAlgorithms.findSyncFramesByAveragingWindow(getFrameValues("GOPR0089.MP4"), signalFrameLength);
        assertEquals(expResult, result);
        
        expResult = new HashSet<>(Arrays.asList(new Integer[]{428}));
        result = SyncDetectionAlgorithms.findSyncFramesByAveragingWindow(getFrameValues("GOPR0084cut.MP4"), signalFrameLength);
        assertEquals(expResult, result);
    }

    /**
     * Test of findSyncFramesByRunLength method, of class SyncDetectionAlgorithms.
     */
    @Test
    public void testFindSyncFramesByRunLength() throws IOException {
        System.out.println("findSyncFramesByRunLength");
        // this method does not work for this file
        Set<Integer> expResult = new HashSet<>(Arrays.asList(new Integer[]{}));
        Set<Integer> result = SyncDetectionAlgorithms.findSyncFramesByRunLength(getFrameValues("GOPR0089.MP4"), signalFrameLength);
        assertEquals(expResult, result);
        
        // this method does not work for this file
        expResult = new HashSet<>(Arrays.asList(new Integer[]{}));
        result = SyncDetectionAlgorithms.findSyncFramesByRunLength(getFrameValues("GOPR0084cut.MP4"), signalFrameLength);
        assertEquals(expResult, result);
    }

    /**
     * Test of findSyncFramesByFirstCloseToMax method, of class SyncDetectionAlgorithms.
     */
    @Test
    public void testFindSyncFramesByFirstCloseToMax() throws IOException {
        System.out.println("findSyncFramesByFirstCloseToMax");
        Set<Integer> expResult = new HashSet<>(Arrays.asList(new Integer[]{66}));;
        Set<Integer> result = SyncDetectionAlgorithms.findSyncFramesByFirstCloseToMax(getFrameValues("GOPR0089.MP4"));
        assertEquals(expResult, result);
        
        expResult = new HashSet<>(Arrays.asList(new Integer[]{482}));
        result = SyncDetectionAlgorithms.findSyncFramesByFirstCloseToMax(getFrameValues("GOPR0084cut.MP4"));
        assertEquals(expResult, result);
    }
    
}
