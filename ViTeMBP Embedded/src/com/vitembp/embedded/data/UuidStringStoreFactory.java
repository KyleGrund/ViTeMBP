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

/**
 * A factory class for building UuidStringStore singletons.
 */
class UuidStringStoreFactory {
    /**
     * The singleton instance for the UuidStringStoreH2 type.
     */
    private static UuidStringStore h2Instance;
    
    /**
     * The singleton instance for the UuidStringStoreDynamoDB type.
     */
    private static UuidStringStore dynamoDBInstance;
    
    /**
     * The singleton instance for the UuidStringStoreHashMap type.
     */
    private static UuidStringStore inMemoryInstance;
    
    /**
     * Returns the UuidStringStore singleton instance for the capture.
     * @param type The type of capture for which to build the UuidStringStore.
     * @return The UuidStringStore singleton instance for the capture.
     * @throws InstantiationException If a UuidStringStore cannot be built for
     * the type.
     */
    public synchronized static UuidStringStore build(CaptureTypes type) throws InstantiationException {
        switch (type) { 
            case InMemory:
                if (inMemoryInstance == null) {
                    inMemoryInstance = new UuidStringStoreGZip(new UuidStringStoreHashMap());
                }
                return inMemoryInstance;
            case EmbeddedH2:
                if (h2Instance == null) {
                    try {
                        h2Instance = new UuidStringStoreGZip(new UuidStringStoreH2(Paths.get("capturedata")));
                    } catch (SQLException ex) {
                        throw new InstantiationException("Could not create database file. " + ex.getLocalizedMessage());
                    }
                }
                return h2Instance;
            case AmazonDynamoDB:
                if (dynamoDBInstance == null) {
                    dynamoDBInstance = new UuidStringStoreGZip(new UuidStringStoreDynamoDB());
                }
                return dynamoDBInstance;
        }
        
        throw new InstantiationException("Could not build a Capture instance for the given parameters.");
    }
}
