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
package com.vitembp.embedded.datatransport;

import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This class will handle uploading local data to a remote database.
 */
public class UuidStringTransporter {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    private static final int IO_FAIL_BACKOFF_TIME = 10000;
    
    private final TransportableStore from;
    private final TransportableStore to;
    private final Thread syncThread;
    
    public UuidStringTransporter(TransportableStore from, TransportableStore to) {
        this.from = from;
        this.to = to;
        this.syncThread = new Thread(this::syncTask);
    }
    
    private void startSync() {
        this.syncThread.start();
    }
    
    private void syncTask() {
        try {
            Iterator<UUID> keys = this.from.getKeys().iterator();
            boolean success;
            while (keys.hasNext()) {
                UUID key = keys.next();
                success = false;
                while (!success) {
                    try {
                        this.to.write(key, this.from.read(key));
                        success = true;
                    } catch (IOException ex) {
                        // write a debug exception, as these are expected
                        LOGGER.debug("IOException while uploading data.", ex);

                        // wait a while before retrying as the usual reason for
                        // failure are network issues
                        Thread.sleep(IO_FAIL_BACKOFF_TIME);
                    }
                }
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Sync task thread interrupted.", ex);
        } catch (IOException ex) {
            LOGGER.error("Failed to access keys in store.", ex);
        }
    }
}
