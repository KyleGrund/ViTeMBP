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
package com.vitembp.embedded.controller;

import java.util.function.Consumer;

/**
 * Signal to get calibration status.
 */
public class SignalGetCalibrationStatus extends Signal {
    /**
     * Initializes a new instance of the SignalGetCalibrationStatus class.
     * @param resultCallback The callback which receives the disposition of the
     * signal.
     */
    public SignalGetCalibrationStatus(Consumer<String> resultCallback) {
        super(resultCallback);
    }
    
}
