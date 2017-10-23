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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Provides an interface to a UUID keyed String value store.
 */
interface UuidStringStore {
    /**
     * Reads a String value for the supplied key.
     * @param key The key of the value to read.
     * @return The value specified by the key.
     * @throws java.io.IOException If an exception occurs while reading from the
     * persistent store.
     */
    public abstract String read(UUID key) throws IOException;
    
    /**
     * Writes a value to the location specified by the key.
     * @param key The location to write the data to.
     * @param value The value to write.
     * @throws java.io.IOException If an exception occurs while writing to the
     * persistent store.
     */
    public abstract void write(UUID key, String value) throws IOException;
    
    /**
     * Gets an Iterable of CaptureDescription descriptors for captures.
     * @return A stream of CaptureDescription descriptors for captures.
     * @throws java.io.IOException If an exception occurs while reading from the
     * persistent store.
     */
    public abstract Stream<CaptureDescription> getCaptureLocations() throws IOException;
    
    /**
     * Adds a description of a capture in the store.
     * @param toAdd The description to add.
     * @throws IOException If an exception occurs while adding the UUID to the
     * list of captures in the persistent store.
     */
    public abstract void addCaptureDescription(CaptureDescription toAdd) throws IOException;
    
    /**
     * Removes a description of a capture in the store.
     * @param location The location of the capture to remove.
     * @throws IOException If an exception occurs while removing the UUID to the
     * list of captures in the persistent store.
     */
    public abstract void removeCaptureDescription(UUID location) throws IOException;
    
    /**
     * Returns a boolean description of a capture in the store.
     * @param location The capture to find.
     * @throws IOException If an exception occurs while removing the UUID to the
     * list of captures in the persistent store.
     */
    public abstract CaptureDescription getCaptureDescription(UUID location) throws IOException;
    
    /**
     * Deletes a value from the location specified by the key.
     * @param key The location to delete the data from.
     * @throws java.io.IOException If an exception occurs while writing to the
     * persistent store.
     */
    public abstract void delete(UUID key) throws IOException;
    
    /**
     * Gets a stream of UUID keys which contain data in the store.
     * @return A stream of UUID keys which contain data in the store.
     * @throws java.io.IOException If an exception occurs while reading from the
     * persistent store.
     */
    public abstract Stream<UUID> getKeys() throws IOException;
    
    /**
     * Gets a Map of UUID keys to their hash in the data in the store.
     * If the key does not exist in the store it will generate an empty string.
     * @param locations The locations to retrieve hashes for.
     * @return A Map of UUID keys to their hash in the data in the store.
     * @throws java.io.IOException If an exception occurs while writing to the
     * persistent store.
     */
    public abstract Map<UUID, String> getHashes(List<UUID> locations) throws IOException;
}
