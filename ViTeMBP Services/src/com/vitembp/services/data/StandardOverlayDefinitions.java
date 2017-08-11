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
package com.vitembp.services.data;

/**
 * A class containing standard overlay definitions for default applications.
 */
class StandardOverlayDefinitions {
    public static String getStandardFourQuadrant() {
        return  "<overlay><type>FourQuadrant</type><elements>" +
                "<element>" +
                "<type>BrakeSensor</type>" +
                "<location>BottomRight</location>" +
                "<sensorbindings>" +
                "<sensor>" +
                "<location>Right</location>" +
                "<name>Rear Brake</name>" +
                "</sensor>" +
                "<sensor>" +
                "<location>Left</location>" +
                "<name>Front Brake</name>" +
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
    }
}
