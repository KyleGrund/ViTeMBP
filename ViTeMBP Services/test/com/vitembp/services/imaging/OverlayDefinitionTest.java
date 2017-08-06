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

import java.util.List;
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
    
    /**
     * Test of getElementDefinitions method, of class OverlayDefinition.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetElementDefinitionsFullConfig() throws Exception {
        System.out.println("getElementDefinitions");
        String xmlDefinition = "<overlay><type>FourQuadrant</type><elements>" +
                "<element>" +
                "<type>BrakeSensor</type>" +
                "<location>BottomRight</location>" +
                "<sensorbindings>" +
                "<sensor>" +
                "<location>Right</location>" +
                "<name>Right Brake</name>" +
                "</sensor>" +
                "<sensor>" +
                "<location>Left</location>" +
                "<name>Left Brake</name>" +
                "</sensor>" +
                "</sensorbindings>" +
                "</element>" +
                "<element>" +
                "<type>Shock</type>" +
                "<location>TopLeft</location>" +
                "<sensorbindings>" +
                "<sensor>" +
                "<location>Center</location>" +
                "<name>Front Shock</name>" +
                "</sensor>" +
                "</sensorbindings>" +
                "</element>" +
                "<element>" +
                "<type>Shock</type>" +
                "<location>TopRight</location>" +
                "<sensorbindings>" +
                "<sensor>" +
                "<location>Center</location>" +
                "<name>Rear Shock</name>" +
                "</sensor>" +
                "</sensorbindings>" +
                "</element>" +
                "<element>" +
                "<type>ThreeAxisG</type>" +
                "<location>BottomLeft</location>" +
                "<sensorbindings>" +
                "<sensor>" +
                "<location>Center</location>" +
                "<name>Frame Accelerometer</name>" +
                "</sensor>" +
                "</sensorbindings>" +
                "</element>" +
                "</elements></overlay>";
        OverlayDefinition result = OverlayDefinition.getDefinition(xmlDefinition);
        assertEquals(OverlayType.FourQuadrant, result.getOverlayType());
        List<ElementDefinition> defns = result.getElementDefinitions();
        assertEquals(4, defns.size());
        assertEquals(1L, defns.stream().filter(ed -> ed.getElementType().equals(ElementType.BrakeSensor)).count());
        assertEquals(1L, defns.stream().filter(ed -> ed.getElementType().equals(ElementType.ThreeAxisG)).count());
        assertEquals(2L, defns.stream().filter(ed -> ed.getElementType().equals(ElementType.Shock)).count());
        ElementDefinition brake = defns.stream().filter(ed -> ed.getElementType().equals(ElementType.BrakeSensor)).findFirst().get();
        ElementDefinition accel = defns.stream().filter(ed -> ed.getElementType().equals(ElementType.ThreeAxisG)).findFirst().get();
        ElementDefinition shockLeft = defns.stream()
                .filter(ed -> ed.getElementType().equals(ElementType.Shock))
                .filter(ed -> ed.getLocation() == ElementLocation.TopLeft)
                .findFirst().get();
        ElementDefinition shockRight = defns.stream()
                .filter(ed -> ed.getElementType().equals(ElementType.Shock))
                .filter(ed -> ed.getLocation() == ElementLocation.TopRight)
                .findFirst().get();
        assertEquals(brake.getLocation(), ElementLocation.BottomRight);
        assertEquals(2, brake.getSensors().size());
        assertEquals(1, brake.getSensors().stream().filter((s) -> s.getLocation() == ElementLocation.Left).count());
        assertEquals(1, brake.getSensors().stream().filter((s) -> s.getLocation() == ElementLocation.Right).count());
        assertEquals(1, brake.getSensors().stream().filter((s) -> "Left Brake".equals(s.getName())).count());
        assertEquals(1, brake.getSensors().stream().filter((s) -> "Right Brake".equals(s.getName())).count());
        
        assertEquals(accel.getLocation(), ElementLocation.BottomLeft);
        assertEquals(1, accel.getSensors().size());
        assertEquals(1, accel.getSensors().stream().filter((s) -> s.getLocation() == ElementLocation.Center).count());
        assertEquals(1, accel.getSensors().stream().filter((s) -> "Frame Accelerometer".equals(s.getName())).count());
        
        assertEquals(shockLeft.getLocation(), ElementLocation.TopLeft);
        assertEquals(1, shockLeft.getSensors().size());
        assertEquals(1, shockLeft.getSensors().stream().filter((s) -> s.getLocation() == ElementLocation.Center).count());
        assertEquals(1, shockLeft.getSensors().stream().filter((s) -> "Front Shock".equals(s.getName())).count());
        
        assertEquals(shockRight.getLocation(), ElementLocation.TopRight);
        assertEquals(1, shockRight.getSensors().size());
        assertEquals(1, shockRight.getSensors().stream().filter((s) -> s.getLocation() == ElementLocation.Center).count());
        assertEquals(1, shockRight.getSensors().stream().filter((s) -> "Rear Shock".equals(s.getName())).count());
    }
}
