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
package com.vitembp.services.interfaces;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;
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
public class AmazonSimpleStorageServiceTest {
    
    public AmazonSimpleStorageServiceTest() {
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
     * Test of uploadPublic method, of class AmazonSimpleStorageService.
     */
    @Test
    public void testUploadPublic() {
        System.out.println("upload");
        
        Random rnd = new Random();
        
        // generate a random size of bytes to send to test the uploadPublic
        final int FILE_SIZE_BYTES = 32767 + rnd.nextInt(32767);
        
        // test bucket access keys (never production)
        String testBucketName = "vitembp.kylegrund.com";
        
        try {
            // write data out to a temp file for uploadPublic
            File tempFile = Files.createTempFile("vitembpS3Test", ".tmp").toFile();
            tempFile.deleteOnExit();
            
            // generate random data and write it to the temp file
            try (FileWriter sw = new FileWriter(tempFile)) {
                char[] toWrite = new char[FILE_SIZE_BYTES];
                for (int i = 0; i < FILE_SIZE_BYTES; i++) {
                    toWrite[i] = (char)rnd.nextInt(Character.MAX_VALUE + 1);
                }
                sw.write(toWrite, 0, FILE_SIZE_BYTES);
            }
            
            // uploadPublic the data
            AmazonSimpleStorageService instance = new AmazonSimpleStorageService(testBucketName);
            instance.uploadPublic(tempFile, "debug/" + tempFile.getName());
        } catch (IOException ex) {
            System.out.println(ex.toString());
            fail("IOException occured: " + ex.toString());
        }
    }
    
}
