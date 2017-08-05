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
public class OverlayDefinitionTest {
    
    public OverlayDefinitionTest() {
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
     * Test of getOverlayType method, of class OverlayDefinition.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetOverlayType() throws Exception {
        System.out.println("getOverlayType");
        String xmlDefinition = "<overlay><type>FourQuadrant</type><elements/></overlay>";
        OverlayDefinition result = OverlayDefinition.getDefinition(xmlDefinition);
        assertEquals(OverlayType.FourQuadrant, result.getOverlayType());
    }

    /**
     * Test of getElementDefinitions method, of class OverlayDefinition.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetElementDefinitions() throws Exception {
        System.out.println("getElementDefinitions");
        String xmlDefinition = "<overlay><type>FourQuadrant</type><elements>" +
                "<element>" +
                "<type>BrakeSensor</type>" +
                "<location>TopRight</location>" +
                "<sensorbindings><sensor>" +
                "<location>Right</location>" +
                "<name>Right Brake</name>" +
                "</sensor></sensorbindings>" +
                "</element></elements></overlay>";
        OverlayDefinition result = OverlayDefinition.getDefinition(xmlDefinition);
        assertEquals(OverlayType.FourQuadrant, result.getOverlayType());
        assertEquals(1, result.getElementDefinitions().size());
        assertEquals(ElementType.BrakeSensor, result.getElementDefinitions().get(0).getElementType());
        assertEquals(ElementLocation.TopRight, result.getElementDefinitions().get(0).getLocation());
        assertEquals(1, result.getElementDefinitions().get(0).getSensors().size());
        assertEquals(ElementLocation.Right, result.getElementDefinitions().get(0).getSensors().get(0).getLocation());
        assertEquals("Right Brake", result.getElementDefinitions().get(0).getSensors().get(0).getName());
    }    
}
