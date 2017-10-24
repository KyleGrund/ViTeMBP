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
package com.vitembp.services.data;

import com.vitembp.embedded.data.Capture;
import com.vitembp.embedded.data.CaptureFactory;
import com.vitembp.embedded.data.CaptureTypes;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Provides operations which can be performed on a capture.
 */
public class CaptureOperations {
    /**
     * Deletes a capture and all of its associated data and index entries.
     * @param toDelete The UUID of the capture to delete.
     * @return A message indicating the disposition of the request.
     * @throws IOException If there is an error deleting form the data store.
     */
    public static String delete(UUID toDelete) throws IOException {
        // get and delete the capture
        getCaptureAtLocation(toDelete).delete();
        return "Capture successfully deleted.";
    }

    /**
     * Opens and loads a capture for a give location.
     * @param toOpen The location where the capture to open is stored.
     * @return The opened capture.
     * @throws IOException If the capture cannot be opened.
     */
    public static Capture getCaptureAtLocation(UUID toOpen) throws IOException {
        // try to find and return the capture from the database
        try {
            Stream<Capture> allCaptures = java.util.stream.StreamSupport.stream(
                    CaptureFactory.getCaptures(CaptureTypes.AmazonDynamoDB).spliterator(),
                    false);
            return allCaptures.filter((Capture c) -> c.getId().equals(toOpen)).findFirst().get();
        } catch (InstantiationException ex) {
            throw new IOException("Could not read captures from database.", ex);
        }
    }
}
