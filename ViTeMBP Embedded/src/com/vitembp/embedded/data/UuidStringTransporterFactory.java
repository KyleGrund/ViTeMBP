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

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class providing instances of UuidStringTransporter objects.
 */
public class UuidStringTransporterFactory {
    /**
     * Stores the tuples mappings from -> to.
     */
    private static final Map<CaptureTypes, CaptureTypes> TUPLES = new HashMap<>();
    
    /**
     * Stores the singleton instances.
     */
    private static final Map<CaptureTypes, UuidStringTransporter> SINGLETONS = new HashMap<>();
    
    /**
     * Builds and returns unique singletons for tuples binding (from, to).
     * @param from The store type to transfer from.
     * @param to The store type to transfer to.
     * @param deleteAfterTransfer A boolean value indicating whether to delete
     * data in the source store once it has been transfered.
     * @return A unique singletons for the tuple (from, to).
     * @throws java.lang.InstantiationException If an instance cannot be created
     * for the specified parameters.
     */
    public synchronized static UuidStringTransporter build(CaptureTypes from, CaptureTypes to, boolean deleteAfterTransfer) throws InstantiationException {
        // cannot transfer from and to the same type as this would do nothing
        if (from == to) {
            throw new IllegalArgumentException("To and from cannot be the same type.");
        }
        
        // if there isn't a tuple mapping we haven't built a transporter of this type yet
        if (!TUPLES.containsKey(from)) {
            UuidStringTransporter built = new UuidStringTransporter(
                    UuidStringStoreFactory.build(from),
                    UuidStringStoreFactory.build(to),
                    deleteAfterTransfer);
            TUPLES.put(from, to);
            SINGLETONS.put(from, built);
            // return here as there should just be safety checks below this block
            return built;
        }
        
        // check that the
        if (!TUPLES.get(from).equals(to)) {
            throw new IllegalArgumentException("The destination store is already in use.");
        }
        
        UuidStringTransporter toReturn = SINGLETONS.get(from);
        if (toReturn.getDeleteAfterTransfer() != deleteAfterTransfer) {
            throw new IllegalArgumentException("The deleteAfterTranfer parameter does not match the previously instantiated instance.");
        }
        
        return toReturn;
    }
}
