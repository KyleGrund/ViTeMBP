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

import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

/**
 * A factory class that creates Capture instances.
 */
public class CaptureFactory {
    public static Capture buildCapture(CaptureTypes type, double frequency, Map<String, UUID> nameToIds) throws InstantiationException {
        switch (type) { 
            case InMemory:
                UuidStringStoreHashMap hashMapStore;
                hashMapStore = new UuidStringStoreHashMap();
                return new UuidStringStoreCapture(frequency, hashMapStore, nameToIds);
            case EmbeddedH2:
                try {
                    UuidStringStoreH2 store = new UuidStringStoreH2(Paths.get("capturedata"));
                    return new UuidStringStoreCapture(frequency, store, nameToIds);
                } catch (SQLException ex) {
                    throw new InstantiationException("Could not create database file. " + ex.getLocalizedMessage());
                }
        }
        
        throw new InstantiationException("Could not build a Capture instance for the given parameters.");
    }
}
