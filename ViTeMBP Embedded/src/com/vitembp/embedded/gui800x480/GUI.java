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
package com.vitembp.embedded.gui800x480;

/**
 * Interface class for running the embedded GUI.
 */
public class GUI {
    
    private static CaptureStatus captureStatusWindow = new CaptureStatus(null, true);
    private static OptionsMenu optionsMenuWindow = new OptionsMenu(null, true);
    
    public static void start() {
        java.awt.EventQueue.invokeLater(() -> {
            captureStatusWindow.setVisible(true);
        });
    }
    
    public static void showCaptureControl() {
        java.awt.EventQueue.invokeLater(() -> {
            optionsMenuWindow.setVisible(false);
            captureStatusWindow.setVisible(true);
        });
    }
    
    public static void showOptionsControl() {
        java.awt.EventQueue.invokeLater(() -> {
            captureStatusWindow.setVisible(false);
            optionsMenuWindow.setVisible(true);
        });
    }
}
