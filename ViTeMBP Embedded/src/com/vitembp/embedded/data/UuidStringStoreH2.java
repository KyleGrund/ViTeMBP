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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.LogManager;

/**
     * Creates a UuidStringStore for the H2 embedded database.
 */
class UuidStringStoreH2 implements UuidStringStore {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * The locations in the store where a list of captures are stored.
     */
    private static final UUID CAPTURE_LOCATIONS = UUID.fromString("c05ccaec-fdb8-4723-9eb9-2afa756e7fae");
    
    /**
     * The connection to the database.
     */
    private final Connection connection;
    
    /**
     * Initializes a new instance of the UuidStringStoreH2 class.
     * @param dataFile The file to store the database to.
     * @throws SQLException If there is an error connecting to the database.
     */
    UuidStringStoreH2(Path dataFile) throws SQLException {
        // load the jdbc driver
        org.h2.Driver.load();
        
        // connect to the database with jdbc
        String location = "jdbc:h2:file:" + dataFile.toAbsolutePath().toString();
        try {
            connection = DriverManager.getConnection(location, "sa", "");
        } catch (SQLException ex) {
            LOGGER.error("Could not connect to database.", ex);
            throw ex;
        }
        
        // initialize the database if needed
        try {
            this.initializeDatabase();
        } catch (SQLException ex) {
            LOGGER.error("Could not initialize database.", ex);
            throw ex;
        }
    }
    
    /**
     * Closes the connection to the database.
     */
    public void close() {
        try {
            this.connection.close();
        } catch (SQLException ex) {
            LOGGER.error("Could not close database.", ex);
        }
    }
    
    @Override
    public Iterable<UUID> getCaptureLocations() throws IOException {
        return Arrays.asList(Arrays.asList(this.read(CAPTURE_LOCATIONS).split(","))
                .stream()
                .map(UUID::fromString)
                .toArray(UUID[]::new));
    }
    
    @Override
    public UUID addCaptureLocation() throws IOException {
        // generate a new UUID
        UUID toAdd = UUID.randomUUID();
        
        // get the current list
        String captures = this.read(CAPTURE_LOCATIONS);
        
        // if there are no captures just store the single UUID, otherwise
        // append the list with a comma and then the UUID.
        if (captures == null || "".equals(captures)) {
            this.write(CAPTURE_LOCATIONS, toAdd.toString());
        } else {
            this.write(CAPTURE_LOCATIONS, captures.concat(",").concat(toAdd.toString()));
        }
        
        return toAdd;
    }
    
    @Override
    public String read(UUID key) throws IOException {
        // create query to get value for UUID key
        StringBuilder query = new StringBuilder();
        query.append("SELECT VALUE FROM DATA WHERE ID='");
        query.append(key.toString());
        query.append("'");
        
        try {
            // execute query and return the results
            ResultSet results = this.connection.createStatement().executeQuery(query.toString());
            // if next returns false there are no valid rows, return null
            if (!results.next()) {
                return null;
            }
            BufferedReader val = new BufferedReader(results.getClob("VALUE").getCharacterStream());
            return val.readLine();
        } catch (SQLException ex) {
            LOGGER.error("Could not read from database.", ex);
            throw new IOException("Exception reading from H2 database.", ex);
        }
    }

    @Override
    public void write(UUID key, String value) throws IOException {
        // build query to put value String into the data store at UUID key
        StringBuilder query = new StringBuilder();
        query.append("MERGE INTO DATA VALUES('");
        query.append(key.toString());
        query.append("', '");
        query.append(value);
        query.append("')");
        
        try {
            // execute query
            int rowsUpdated = this.connection.createStatement().executeUpdate(query.toString());
            // only one row should have been updated
            if (rowsUpdated != 1) {
                throw new SQLException("Expected one row to be udpdated, actually updated: " + Integer.toString(rowsUpdated));
            }
        } catch (SQLException ex) {
            LOGGER.error("Could not write to database.", ex);
            throw new IOException("Exception writing to H2 database.", ex);
        }
    }
    
    @Override
    public void delete(UUID key) throws IOException {
        // build query to delete the data stored at UUID key
        StringBuilder query = new StringBuilder();
        query.append("DELETE FROM DATA WHERE ID='");
        query.append(key.toString());
        query.append("'");
        
        try {
            // execute query
            int rowsUpdated = this.connection.createStatement().executeUpdate(query.toString());
            // only one row should have been updated
            if (rowsUpdated != 1) {
                throw new SQLException("Expected one row to be udpdated, actually updated: " + Integer.toString(rowsUpdated));
            }
        } catch (SQLException ex) {
            LOGGER.error("Could not write to database.", ex);
            throw new IOException("Exception writing to H2 database.", ex);
        }
    }
    
    private void initializeDatabase() throws SQLException {
        // execute query to create the DATA table
        this.connection.createStatement().execute("CREATE CACHED TABLE IF NOT EXISTS DATA(ID UUID PRIMARY KEY, VALUE CLOB)");
    }

    @Override
    public Stream<UUID> getKeys() throws IOException {
        try {
            // get all ID entries from the DATA table in the database
            ResultSet set = this.connection.createStatement().executeQuery("SELECT ID FROM DATA");
            boolean hasElements = set.first();
            
            // generate a stream of the parsed UUIDs excluding CAPTURE_LOCATIONS
            return StreamSupport.stream(((Iterable<UUID>)() -> new Iterator<UUID>() {
                boolean hasNext = hasElements;
                @Override
                public boolean hasNext() {
                    return hasNext;
                }

                @Override
                public UUID next() {
                    if (hasNext) {
                        try {
                            UUID value = UUID.fromString(set.getString("ID"));
                            hasNext = set.next();
                            return value;
                        } catch (SQLException ex) {
                            LOGGER.error("Unexpected exception accessing ID column.", ex);
                        }
                    }
                    return null;
                }
            }).spliterator(), false).filter(id -> !id.equals(CAPTURE_LOCATIONS));
        } catch (SQLException ex) {
            throw new IOException("Could not retrieve keys from H2 store.", ex);
        }
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
}
