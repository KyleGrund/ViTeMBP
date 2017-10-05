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

import com.vitembp.embedded.hardware.HardwareInterface;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;

/**
 * Class which provides an interface to the embedded GUI.
 */
public class GUI {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * The window showing the capture status and control.
     */
    private static final CaptureStatus CAPTURE_STATUS_WINDOW = new CaptureStatus(null, true);
    
    /**
     * The options window allowing basic configuration control.
     */
    private static final OptionsMenu OPTIONS_MENU_WINDOW = new OptionsMenu(null, true);
    
    /**
     * The window providing sensors control.
     */
    private static final SensorsMenu SENSORS_MENU_WINDOW = new SensorsMenu(null, true);
    
    /**
     * Starts the GUI.
     * @param sensorsChangedCallback
     * @param dataReadCallback
     */
    public static void start(
            Consumer<Consumer<Set<String>>> sensorsChangedCallback,
            Consumer<Consumer<Map<String, String>>> dataReadCallback) {
        // register callbacks
        sensorsChangedCallback.accept(CAPTURE_STATUS_WINDOW::updateSensors);
        dataReadCallback.accept(CAPTURE_STATUS_WINDOW::sensorReading);
        
        // show status window
        java.awt.EventQueue.invokeLater(() -> {
            CAPTURE_STATUS_WINDOW.setVisible(true);
        });
    }
    
    /**
     * Shows the capture status window and hides all others.
     */
    public static void showCaptureControl() {
        java.awt.EventQueue.invokeLater(() -> {
            OPTIONS_MENU_WINDOW.setVisible(false);
            SENSORS_MENU_WINDOW.setVisible(false);
            CAPTURE_STATUS_WINDOW.setVisible(true);
        });
    }
    
    /**
     * Shows the options window and hides all others.
     */
    public static void showOptionsControl() {
        java.awt.EventQueue.invokeLater(() -> {
            CAPTURE_STATUS_WINDOW.setVisible(false);
            SENSORS_MENU_WINDOW.setVisible(false);
            OPTIONS_MENU_WINDOW.setVisible(true);
        });
    }
    
    /**
     * Shows the sensors window and hides all others.
     */
    public static void showSensorsControl() {
        java.awt.EventQueue.invokeLater(() -> {
            CAPTURE_STATUS_WINDOW.setVisible(false);
            OPTIONS_MENU_WINDOW.setVisible(false);
            SENSORS_MENU_WINDOW.setVisible(true);
        });
    }
    
    /**
     * Shuts the system down using the hardware interface.
     */
    public static void systemShutdown() {
        try {
            // shuts the system down for halt
            HardwareInterface.getInterface().shutDownSystem();
        } catch (IOException ex) {
            LOGGER.error("Could not start shutdown process.", ex);
        }
    }

    /**
     * Restarts the system using the hardware interface.
     */
    static void systemRestart() {
        try {
            // shuts the system down for halt
            HardwareInterface.getInterface().restartSystem();
        } catch (IOException ex) {
            LOGGER.error("Could not start shutdown process.", ex);
        }
    }
}
