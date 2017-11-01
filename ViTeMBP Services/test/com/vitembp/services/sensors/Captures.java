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
package com.vitembp.services.sensors;

import com.vitembp.embedded.data.Capture;
import com.vitembp.embedded.data.CaptureFactory;
import com.vitembp.embedded.data.CaptureTypes;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class that helps tests build captures.
 */
public class Captures {
    /**
     * Creates a capture with various types of sensors.
     * @return The capture which was created.
     * @throws InstantiationException If there is an exception while creating
     * the capture.
     */
    public static Capture createCapture() throws InstantiationException {
        Map<String, UUID> nameToIds = new HashMap<>();
        nameToIds.put("Front Brake", RotaryEncoderEAW0J.TYPE_UUID);
        nameToIds.put("Rear Brake", RotaryEncoderEAW0J.TYPE_UUID);
        nameToIds.put("Frame Accelerometer", AccelerometerFXOS8700CQSerial.TYPE_UUID);
        nameToIds.put("Front Shock", DistanceVL53L0X.TYPE_UUID);
        nameToIds.put("Rear Shock", DistanceVL53L0X.TYPE_UUID);
        Map<String, String> calibrations = new HashMap<>();
        return CaptureFactory.buildCapture(CaptureTypes.InMemory, 29.9, nameToIds, calibrations);
    }
}
