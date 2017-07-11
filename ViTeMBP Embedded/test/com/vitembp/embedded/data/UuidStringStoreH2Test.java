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
package com.vitembp.embedded.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Kyle
 */
public class UuidStringStoreH2Test {
    
    public UuidStringStoreH2Test() {
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
     * Test of instantiation of class UuidStringStoreH2.
     */
    @Test
    public void testInstantiate() {
        System.out.println("instantiate");
        try {
            // create a temp file and delete it to get a filename for the db
            Path tempFile = Files.createTempFile("testdb", "");
            Files.delete(tempFile);
            
            // the file name actually created will have the .mv.db appeneded
            Path expected = Paths.get(tempFile.toAbsolutePath().toString() + ".mv.db");
            
            // instantiate the connector
            UuidStringStoreH2 instance = new UuidStringStoreH2(tempFile);
            
            // verify the db file was created
            if (!Files.exists(expected)) {
                fail("Database file not created.");
            }
            
            // close the db and delete the db file
            instance.close();
            Files.delete(expected);
        } catch (SQLException ex) {
            Assert.fail("SQLException occurred: " + ex.getLocalizedMessage());
        } catch (IOException ex) {
            Assert.fail("IOException occurred: " + ex.getLocalizedMessage());
        }
    }
    
    /**
     * Test of read method, of class UuidStringStoreH2.
     */
    @Test
    public void testRead() {
        System.out.println("read");        
        try {
            // create a temp file and delete it to get a filename for the db
            Path tempFile = Files.createTempFile("testdb", "");
            Files.delete(tempFile);
            
            // the file name actually created will have the .mv.db appeneded
            Path expected = Paths.get(tempFile.toAbsolutePath().toString() + ".mv.db");
            
            // instantiate the connector
            UuidStringStoreH2 instance = new UuidStringStoreH2(tempFile);
            
            UUID key = UUID.randomUUID();
            String expResult = "A test string.";
            instance.write(key, expResult);
            String result = instance.read(key);
            assertEquals(expResult, result);
            
            // verify the db file was created
            if (!Files.exists(expected)) {
                fail("Database file not created.");
            }
            
            // close the db and delete the db file
            instance.close();
            Files.delete(expected);
        } catch (SQLException ex) {
            Assert.fail("SQLException occurred: " + ex.getLocalizedMessage());
        } catch (IOException ex) {
            Assert.fail("IOException occurred: " + ex.getLocalizedMessage());
        }
    }

    /**
     * Test of write method, of class UuidStringStoreH2.
     */
    @Test
    public void testWrite() {
        System.out.println("write");
        UUID key = UUID.randomUUID();
        String value = "A test string.";
        try {
            // create a temp file and delete it to get a filename for the db
            Path tempFile = Files.createTempFile("testdb", "");
            Files.delete(tempFile);
            
            // the file name actually created will have the .mv.db appeneded
            Path expected = Paths.get(tempFile.toAbsolutePath().toString() + ".mv.db");
            
            // instantiate the connector
            UuidStringStoreH2 instance = new UuidStringStoreH2(tempFile);
            
            instance.write(key, value);
        } catch (SQLException ex) {
            Assert.fail("SQLException occurred: " + ex.getLocalizedMessage());
        } catch (IOException ex) {
            Assert.fail("IOException occurred: " + ex.getLocalizedMessage());
        }
    }
    
    /**
     * Test of write method, of class UuidStringStoreH2.
     */
    @Test
    public void testUpdate() {
        System.out.println("write");
        UUID key = UUID.randomUUID();
        try {
            // create a temp file and delete it to get a filename for the db
            Path tempFile = Files.createTempFile("testdb", "");
            Files.delete(tempFile);

            // instantiate the connector
            UuidStringStoreH2 instance = new UuidStringStoreH2(tempFile);
           
            String expected = "A test string.";
            instance.write(key, expected);
            assertTrue(expected.equals(instance.read(key)));
            
            expected = "A different string.";
            instance.write(key, expected);
            assertTrue(expected.equals(instance.read(key)));
        } catch (SQLException ex) {
            Assert.fail("SQLException occurred: " + ex.getLocalizedMessage());
        } catch (IOException ex) {
            Assert.fail("IOException occurred: " + ex.getLocalizedMessage());
        }
    }
    
    /**
     * Test of getKeys method, of class UuidStringStoreH2.
     */
    @Test
    public void testGetKeys() {
        System.out.println("read");        
        try {
            // create a temp file and delete it to get a filename for the db
            Path tempFile = Files.createTempFile("testdb", "");
            Files.delete(tempFile);
            
            // the file name actually created will have the .mv.db appeneded
            Path expected = Paths.get(tempFile.toAbsolutePath().toString() + ".mv.db");
            
            // instantiate the connector
            UuidStringStoreH2 instance = new UuidStringStoreH2(tempFile);
            
            UUID key = UUID.randomUUID();
            String expResult = "A test string.";
            instance.write(key, expResult);
            
            assertEquals(1, instance.getKeys().count());
            
            instance.getKeys().forEach((id) -> assertTrue(key.equals(id)));
            
            // verify the db file was created
            if (!Files.exists(expected)) {
                fail("Database file not created.");
            }
            
            // close the db and delete the db file
            instance.close();
            Files.delete(expected);
        } catch (SQLException ex) {
            Assert.fail("SQLException occurred: " + ex.getLocalizedMessage());
        } catch (IOException ex) {
            Assert.fail("IOException occurred: " + ex.getLocalizedMessage());
        }
    }
}
