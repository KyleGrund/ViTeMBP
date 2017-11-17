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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;

/**
 * A factory class that creates Capture instances.
 */
public class CaptureFactory {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * Builds a new capture with the specified parameters.
     * @param type The type of the capture, reflecting the backing store it uses.
     * @param frequency The sample frequency of the capture.
     * @param nameToIds A map of the Sensor names to types.
     * @param calData Calibration data for sensors.
     * @return A new capture with the specified parameters.
     * @throws InstantiationException If an appropriate capture cannot be created.
     */
    public static Capture buildCapture(CaptureTypes type, double frequency, Map<String, UUID> nameToIds, Map<String, String> calData) throws InstantiationException {
        switch (type) { 
            case InMemory:
                UuidStringStore inMemory = UuidStringStoreFactory.build(CaptureTypes.InMemory);
                UUID locationID = UUID.randomUUID();
                UuidStringLocation hashMapStore = new UuidStringLocation(inMemory, locationID);
                RunnableIOException delCallback = () -> inMemory.removeCaptureDescription(locationID);
                return new UuidStringStoreCapture(delCallback, frequency, hashMapStore, nameToIds);
            case EmbeddedH2:
                try {
                    UuidStringStore h2Store = UuidStringStoreFactory.build(CaptureTypes.EmbeddedH2);
                    UUID h2LocationID = UUID.randomUUID();
                    UuidStringLocation location = new UuidStringLocation(h2Store, h2LocationID);
                    RunnableIOException delH2 = () -> h2Store.removeCaptureDescription(h2LocationID);
                    Capture toReturn = new UuidStringStorePagingCapture(delH2, frequency, location, (int)Math.ceil(frequency * 3), nameToIds, calData);
                    h2Store.addCaptureDescription(new CaptureDescription(toReturn, h2LocationID));
                    return toReturn;
                } catch (IOException ex) {
                    throw new InstantiationException("Could not create new capture location. " + ex.getLocalizedMessage());
                }
            case AmazonDynamoDB:
                try {
                    UuidStringStore ddbStore = UuidStringStoreFactory.build(CaptureTypes.AmazonDynamoDB);
                    UUID adbLocationID = UUID.randomUUID();
                    UuidStringLocation location = new UuidStringLocation(ddbStore, adbLocationID);
                    RunnableIOException delAdb = () -> ddbStore.removeCaptureDescription(adbLocationID);
                    Capture toReturn = new UuidStringStorePagingCapture(delAdb, frequency, location, (int)Math.ceil(frequency * 3), nameToIds, calData);
                    ddbStore.addCaptureDescription(new CaptureDescription(toReturn, adbLocationID));
                    return toReturn;
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
     * @throws java.lang.InstantiationException If there is an error instantiating a capture.
     */
    public static Iterable<Capture> getCaptures(CaptureTypes type) throws InstantiationException {
        switch (type) { 
            case EmbeddedH2:
                try {
                    UuidStringStore h2Store = UuidStringStoreFactory.build(CaptureTypes.EmbeddedH2);
                    List<Capture> toReturn = new ArrayList<>();
                    for (UUID id : (Iterable<UUID>)h2Store.getCaptureLocations().map((c) -> c.getLocation())::iterator) {
                        toReturn.add( new UuidStringStorePagingCapture(
                                () -> h2Store.removeCaptureDescription(id),
                                new UuidStringLocation(h2Store, id)));
                    }
                    return toReturn;
                } catch (IOException ex) {
                    throw new InstantiationException("Could not load H2 database capture index. " + ex.getLocalizedMessage());
                }
            case AmazonDynamoDB:
                try {
                    UuidStringStore ddbStore = UuidStringStoreFactory.build(CaptureTypes.AmazonDynamoDB);
                    List<Capture> toReturn = new ArrayList<>();
                    for (UUID id : (Iterable<UUID>)ddbStore.getCaptureLocations().map((c) -> c.getLocation())::iterator) {
                        toReturn.add(new UuidStringStorePagingCapture(
                                () -> ddbStore.removeCaptureDescription(id),
                                new UuidStringLocation(ddbStore, id)));
                    }
                    return toReturn;
                } catch (IOException ex) {
                    LOGGER.error("Could not load DynamoDB database capture index.", ex);
                    throw new InstantiationException("Could not load DynamoDB database capture index. " + ex.getLocalizedMessage());
                }
        }
        
        throw new UnsupportedOperationException();
    }
}
