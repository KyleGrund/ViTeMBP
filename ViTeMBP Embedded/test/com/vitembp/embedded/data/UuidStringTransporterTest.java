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

import java.util.Random;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Class containing unit tests for the UuidStringTransporter class.
 */
public class UuidStringTransporterTest {
    /**
     * Initializes a new instance of the UuidStringTransporterTest class.
     */
    public UuidStringTransporterTest() {
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

    @Test
    public void testSyncInMemoryWithDelete() throws Exception {
        System.out.println("SyncInMemoryWithDelete"); 
        UuidStringStoreHashMap from = new UuidStringStoreHashMap();
        UuidStringStoreHashMap to = new UuidStringStoreHashMap();
        Random generator = new Random();
        
        for (int i = 0; i < 100; i++) {
            from.write(UUID.randomUUID(), Long.toString(generator.nextLong()));
        }
        
        UuidStringTransporter instance = new UuidStringTransporter(from, to, true);
        instance.startSync();
        
        while (from.getKeys().count() > 0) {
            Thread.sleep(100);
        }
        
        assertEquals(0l, from.getKeys().count());
        assertEquals(100l, to.getKeys().count());
    }
    
}
