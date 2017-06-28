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
 * Provides an interface to a UUID keyed String value store.
 */
interface UuidStringStore {
    /**
     * Reads a String value for the supplied key.
     * @param key The key of the value to read.
     * @return The value specified by the key.
     * @throws java.io.IOException If an exception occurs while writing to the
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
}
