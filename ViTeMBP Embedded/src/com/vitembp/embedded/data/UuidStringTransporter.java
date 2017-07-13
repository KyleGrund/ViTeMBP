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
 * This class will handle uploading local data to a remote database.
 */
public class UuidStringTransporter {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * The time to wait before retrying after an IO failure.
     */
    private static final int IO_FAIL_BACKOFF_TIME = 10000;
    
    /**
     * The number of hashes to create in one batch transaction.
     */
    private static final int HASH_BATCH_SIZE = 2;
    
    /**
     * The store with the source data.
     */
    private final UuidStringStore from;
    
    /**
     * The destination store for the source data.
     */
    private final UuidStringStore to;
    
    /**
     * The thread that runs the synchronization.
     */
    private final Thread syncThread;
    
    /**
     * A boolean value indicating whether to delete values from the source store
     * after they have been synchronized.
     */
    private final boolean deleteAfterTransfer;
    
    /**
     * A boolean flag indicating whether the sync thread should keep running.
     */
    private boolean isRunning = true;
    
    /**
     * Initializes a new instance of the UuidStringTransporter class.
     * @param from The store to transfer data from.
     * @param to The store to transfer data to.
     * @param deleteAfterTransfer Whether data should be deleted after it is
     * found to be synchronized.
     */
    UuidStringTransporter(UuidStringStore from, UuidStringStore to, boolean deleteAfterTransfer) throws InstantiationException {
        if (from == to) {
            throw new IllegalArgumentException("To and from capture types cannot be the same.");
        }
        this.from = from;
        this.to = to;
        this.deleteAfterTransfer = deleteAfterTransfer;
        this.syncThread = new Thread(this::syncTask);
        this.syncThread.setName("UuidStringTransporter");
    }
    
    /**
     * Starts the sync thread.
     */
    public void startSync() {
        this.syncThread.start();
    }
    
    /**
     * Starts the sync thread.
     */
    public void stopSync() {
        this.isRunning = false;
    }
    
    /**
     * Gets a boolean value indicating whether to delete items in the source
     * store after they have been transfered.
     * @return A boolean value indicating whether to delete items in the source
     * store after they have been transfered.
     */
    public boolean getDeleteAfterTransfer() {
        return this.deleteAfterTransfer;
    }
    
    /**
     * The method which executes the synchronization task.
     */
    private void syncTask() {
        // look until this flag tells us to stop
        while (this.isRunning) {
            try {
                // get batches of UUIDs to transport
                List<List<UUID>> batches = this.from.getKeys().collect(
                        ArrayList::new,
                        (List<List<UUID>> list, UUID toAdd) -> {
                            List<UUID> last;
                            while (list.isEmpty() || (last = list.get(list.size() - 1)).size() >= HASH_BATCH_SIZE) {
                                list.add(new ArrayList<>());
                            }
                            last.add(toAdd);
                        },
                        (a,b) -> a.addAll(b));

                // for each batch of UUIDs to process
                for (List<UUID> batch : batches) {
                    // get their hashes
                    Map<UUID, String> fromHashes = from.getHashes(batch);
                    Map<UUID, String> toHashes = to.getHashes(batch);

                    // for each key in this batch
                    for (UUID key : batch) {
                        if (!fromHashes.get(key).equals(toHashes.get(key))) {
                            // the hashes don't match so copy the entry between
                            // the stores
                            to.write(key, from.read(key));
                        } else if (this.deleteAfterTransfer) {
                            // the hashes match so delete if set to do so by the
                            // deleteAfterTransfer parameter.
                            from.delete(key);
                        }
                    }
                }
                
                Thread.sleep(UuidStringTransporter.IO_FAIL_BACKOFF_TIME);
                
            } catch (IOException ex) {
                LOGGER.error("Failed to access keys in store.", ex);
            } catch (InterruptedException ex) {
                LOGGER.error("UuidStringTransporter thread interrupted.", ex);
                this.stopSync();
            }
        }
    }
}
