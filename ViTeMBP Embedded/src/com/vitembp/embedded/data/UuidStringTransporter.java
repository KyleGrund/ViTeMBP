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
     * The sleep time between upload checks.
     */
    private static final int LONG_SLEEP = 10000;
    
    /**
     * The sleep time during checks to allow for higher priority threads to
     * execute if we are on a single core system.
     */
    private static final int SHORT_SLEEP = 20;
    
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
     * The thread that runs the data synchronization.
     */
    private final Thread dataSyncThread;
    
    /**
     * The thread that runs the data synchronization.
     */
    private final Thread capturesSyncThread;
    
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
        
        this.dataSyncThread = new Thread(this::syncDataTask);
        this.dataSyncThread.setName("DataSync");
        this.dataSyncThread.setPriority(Thread.MIN_PRIORITY);
        
        this.capturesSyncThread = new Thread(this::syncCapturesTask);
        this.capturesSyncThread.setName("CaptureSync");
        this.capturesSyncThread.setPriority(Thread.MIN_PRIORITY);
    }
    
    /**
     * Starts the sync thread.
     */
    public void startSync() {
        this.dataSyncThread.start();
        this.capturesSyncThread.start();
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
     * The method which executes the data synchronization task.
     */
    private void syncDataTask() {
        // loop until this flag tells us to stop
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
                    Thread.sleep(SHORT_SLEEP);
                    
                    // for each key in this batch
                    for (UUID key : batch) {
                        if (!fromHashes.get(key).equals(toHashes.get(key))) {
                            // the hashes don't match so copy the entry between
                            // the stores
                            to.write(key, from.read(key));
                            LOGGER.debug("Synced key: " + key.toString());
                            Thread.sleep(SHORT_SLEEP);
                        } else if (this.deleteAfterTransfer) {
                            // the hashes match so delete if set to do so by the
                            // deleteAfterTransfer parameter.
                            from.delete(key);
                            LOGGER.debug("Deleted key: " + key.toString());
                        }
                    }
                }
                
                Thread.sleep(LONG_SLEEP);
            } catch (IOException ex) {
                LOGGER.error("Failed to access keys in store.", ex);
            } catch (InterruptedException ex) {
                LOGGER.error("Thread interrupted.", ex);
                this.stopSync();
            } catch (Exception ex) {
                LOGGER.error("Unexpected exception in synchronization thread.", ex);
            }
        }
        
        LOGGER.info("Data sync thread exiting.");
    }
    
    /**
     * The method which executes the capture synchronization task.
     */
    private void syncCapturesTask() {
        // booleans to track whether device has been registered in the stores
        boolean toRegistered = false;
        boolean fromRegistered = false;
        
        // loop until this flag tells us to stop
        while (this.isRunning) {
            try {
                // make sure the device is regesterd in the stores
                if (!toRegistered) {
                    to.registerDeviceUUID();
                    toRegistered = true;
                }
                if (!fromRegistered) {
                    from.registerDeviceUUID();
                    fromRegistered = true;
                }
            } catch (IOException ex) {
                LOGGER.error("Failed to register device in store.", ex);
            } catch (Exception ex) {
                LOGGER.error("Unexpected exception registering device.", ex);
            }
             
            try {
                // sync the capture definitions
                CaptureDescription[] toSend = this.from.getCaptureLocations().toArray(CaptureDescription[]::new);
                for (CaptureDescription desc : toSend) {
                    Thread.sleep(SHORT_SLEEP);
                    CaptureDescription existingDesc = to.getCaptureDescription(desc.getLocation());
                    if (existingDesc == null ||
                            !existingDesc.getCreated().equals(desc.getCreated()) ||
                            !(Math.abs(existingDesc.getFrequency() - desc.getFrequency()) < 0.0001) ||
                            !existingDesc.getCreated().equals(desc.getCreated()) ||
                            !existingDesc.getCreated().equals(desc.getCreated())) {
                        // was not in destination, or needs updated
                        this.to.addCaptureDescription(desc);
                        LOGGER.debug("Synced capture description: " + desc.getLocation().toString());
                    } else if (this.deleteAfterTransfer) {
                        // was in destination, and set to delete after transfer
                        this.from.removeCaptureDescription(desc);
                        LOGGER.debug("Deleted capture description: " + desc.getLocation().toString());
                    }
                }
                
                Thread.sleep(LONG_SLEEP);
            } catch (IOException ex) {
                LOGGER.error("Failed to access keys in store.", ex);
            } catch (InterruptedException ex) {
                LOGGER.error("Thread interrupted.", ex);
                this.stopSync();
            } catch (Exception ex) {
                LOGGER.error("Unexpected exception in synchronization thread.", ex);
            }
        }
        
        LOGGER.info("Captures sync thread exiting.");
    }
}
