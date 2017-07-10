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
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * A factory class that creates Capture instances.
 */
public class CaptureFactory {
    /**
     * Builds a new capture with the specified parameters.
     * @param type The type of the capture, reflecting the backing store it uses.
     * @param frequency The sample frequency of the capture.
     * @param nameToIds A map of the Sensor names to types.
     * @return A new capture with the specified parameters.
     * @throws InstantiationException If an appropriate capture cannot be created.
     */
    public static Capture buildCapture(CaptureTypes type, double frequency, Map<String, UUID> nameToIds) throws InstantiationException {
        switch (type) { 
            case InMemory:
                UuidStringLocation hashMapStore;
                hashMapStore = new UuidStringLocation(new UuidStringStoreHashMap(), UUID.randomUUID());
                return new UuidStringStoreCapture(frequency, hashMapStore, nameToIds);
            case EmbeddedH2:
                try {
                    UuidStringStoreH2 h2Store = new UuidStringStoreH2(Paths.get("capturedata"));
                    UuidStringLocation location = new UuidStringLocation(h2Store, h2Store.addCaptureLocation());
                    return new UuidStringStoreCapture(frequency, location, nameToIds);
                } catch (SQLException ex) {
                    throw new InstantiationException("Could not create database file. " + ex.getLocalizedMessage());
                } catch (IOException ex) {
                    throw new InstantiationException("Could not create new capture location. " + ex.getLocalizedMessage());
                }
            case AmazonDynamoDB:
                try {
                    UuidStringStoreDynamoDB ddbStore = new UuidStringStoreDynamoDB();
                    UuidStringLocation location = new UuidStringLocation(ddbStore, ddbStore.addCaptureLocation());
                    return new UuidStringStoreCapture(frequency, location, nameToIds);
                } catch (IOException ex) {
                    throw new InstantiationException("Could not create new capture location. " + ex.getLocalizedMessage());
                }
        }
        
        throw new InstantiationException("Could not build a Capture instance for the given parameters.");
    }
    
    /**
     * Gets an Iterable of Capture objects for the captures in the store.
     * @param type The type of capture to load captures from.
     * @return An Iterable of Capture objects for the captures in the store.
     */
    public static Iterable<Capture> getCaptures(CaptureTypes type) {
        switch (type) { 
            case EmbeddedH2:
//                try {
//                    UuidStringStoreH2 h2Store = new UuidStringStoreH2(Paths.get("capturedata"));
//                    UuidStringLocation location = new UuidStringLocation(h2Store, UUID.randomUUID());
//                    return new UuidStringStoreCapture(frequency, location, nameToIds);
//                } catch (SQLException ex) {
//                    throw new InstantiationException("Could not create database file. " + ex.getLocalizedMessage());
//                }
        }
        return new ArrayList<Capture>();
    }
}
