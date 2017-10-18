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
import java.util.UUID;

/**
 * Provides an interface to a location in a UUID keyed String value store. This
 * effectively binds to a particular location.
 */
class UuidStringLocation {
    /**
     * The underlying store.
     */
    private final UuidStringStore store;
    
    /**
     * The location in the underlying store this instance is bound to.
     */
    private final UUID location;
    
    /**
     * Initialized 
     * @param store
     * @param location 
     */
    public UuidStringLocation(UuidStringStore store, UUID location) {
        this.store = store;
        this.location = location;
    }
    
    /**
     * Reads a String value for the location bound to this instance.
     * @return The value specified by the key.
     * @throws java.io.IOException If an exception occurs while writing to the
     * persistent store.
     */
    public String read() throws IOException {
        return this.store.read(location);
    }
    
    /**
     * Writes a value to the location bound to this instance.
     * @param value The value to write.
     * @throws java.io.IOException If an exception occurs while writing to the
     * persistent store.
     */
    public void write(String value) throws IOException {
        this.store.write(this.location, value);
    }
    
    /**
     * Deletes the data at the specified location.
     * @throws IOException If an exception occurs while writing to the
     * persistent store.
     */
    public void delete() throws IOException {
        this.store.delete(this.location);
    }
    
    /**
     * Returns a new instance of UuidStringLocation bound to the supplied UUID.
     * @param location The location to bind the new instance to.
     * @return 
     */
    public UuidStringLocation getNewLocation(UUID location) {
        return new UuidStringLocation(this.store, location);
    }

    /**
     * Gets the location in the store where reads and writes address.
     * @return The location in the store where reads and writes address.
     */
    UUID getLocation() {
        return this.location;
    }
}
