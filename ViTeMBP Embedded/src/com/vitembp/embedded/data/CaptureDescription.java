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

import java.time.Instant;
import java.util.UUID;

/**
 * This class provides the data used by the databases to describe a capture.
 */
public class CaptureDescription {
    /**
     * The location of the capture data in the data store table.
     */
    private final UUID location;
    
    /**
     * The system which generated the capture.
     */
    private final UUID system;
    
    /**
     * The time the capture was created.
     */
    private final Instant created;
    
    /**
     * The frequency of data samples of the capture.
     */
    private final double frequency;
    
    /**
     * Initializes a new instance of the CaptureDescription class.
     * @param location The location of the capture data in the data store table.
     * @param system The system which generated the capture.
     * @param created The time the capture was created.
     * @param frequency The frequency of data samples of the capture.
     */
    public CaptureDescription(UUID location, UUID system, Instant created, double frequency){
        this.location = location;
        this.system = system;
        this.created = created;
        this.frequency = frequency;
    }
    
    /**
     * Gets the location of the capture data in the data store table.
     * @return The location of the capture data in the data store table.
     */
    public UUID getLocation() {
        return this.location;
    }
    
    /**
     * Gets the system which generated the capture.
     * @return The system which generated the capture.
     */
    public UUID getSystem() {
        return this.system;
    }
    
    /**
     * Gets the time the capture was created.
     * @return The time the capture was created.
     */
    public Instant getCreated() {
        return this.created;
    }
    
    /**
     * Gets the frequency of data samples of the capture.
     * @return The frequency of data samples of the capture.
     */
    public double getFrequency() {
        return this.frequency;
    }
}
