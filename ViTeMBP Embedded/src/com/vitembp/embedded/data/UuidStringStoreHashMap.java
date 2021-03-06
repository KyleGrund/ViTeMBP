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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Implementation of the UuidStringtStore class using a HashMap
 */
class UuidStringStoreHashMap implements UuidStringStore {
    /**
     * The backing data store.
     */
    private final Map<UUID, String> store = new ConcurrentHashMap<>();
    
    /**
     * Holds list of capture descriptions.
     */
    private final List<CaptureDescription> captureList = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * Holds UUID of devices.
     */
    private final List<UUID> devices = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * The locations in the store where a list of captures are stored.
     */
    private static final UUID CAPTURE_LOCATIONS = UUID.fromString("3dec3d2b-f220-4bc8-b299-330816d12f25");
    
    /**
     * Initializes a new instance of the UuidStringStoreHashMap class.
     */
    UuidStringStoreHashMap() {
    }
    
    @Override
    public String read(UUID key) throws IOException {
        return this.store.get(key);
    }

    @Override
    public void write(UUID key, String value) throws IOException {
        this.store.put(key, value);
    }
    
    @Override
    public void delete(UUID key) throws IOException {
        this.store.remove(key);
    }
    
    @Override
    public Stream<CaptureDescription> getCaptureLocations() throws IOException {
        return Collections.unmodifiableList(this.captureList).stream();
    }
    
    @Override
    public void addCaptureDescription(CaptureDescription toAdd) throws IOException {
        this.captureList.add(toAdd);
    }
    
    @Override
    public Stream<UUID> getKeys() throws IOException {
        // generate a stream of the parsed UUIDs
        return Collections.unmodifiableSet(this.store.keySet()).stream();
    }

    @Override
    public Map<UUID, String> getHashes(List<UUID> locations) throws IOException {
        Map<UUID, String> hashes = new HashMap<>();
        for (UUID loc : locations) {
            // try to get any data
            String toHash = this.read(loc);
            if (toHash == null) {
                // put in an empty string as this entry is blank
                hashes.put(loc, "");
            } else {
                // had data, add the standard 32bit string hash
                hashes.put(loc, Integer.toString(toHash.hashCode()));
            }
        }
        return hashes;
    }

    @Override
    public void removeCaptureDescription(UUID toRemove) throws IOException {
        // filter matching captures, and then remove them
        for (CaptureDescription desc : this.captureList.stream()
                .filter((cap) -> cap.getLocation().equals(toRemove))
                .toArray(CaptureDescription[]::new))
        {
            captureList.remove(desc);
        }
    }

    @Override
    public CaptureDescription getCaptureDescription(UUID location) throws IOException {
        // return the first description with a matching location or null if none
        return this.captureList.stream()
                .filter((cap) -> cap.getLocation().equals(location))
                .findFirst()
                .orElse(null);
    }
}
