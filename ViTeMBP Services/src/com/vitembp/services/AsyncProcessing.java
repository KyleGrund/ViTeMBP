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
package com.vitembp.services;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;

/**
 * This class provides asynchronous processing services for long-lived
 * operations.
 */
public class AsyncProcessing {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * The thread which processes commands.
     */
    private static final Thread PROCESSING_THREAD;
    
    /**
     * The queue which holds commands waiting to be processed.
     */
    private static final BlockingQueue<Runnable> TASK_QUEUE = new LinkedBlockingQueue<>();
    
    /**
     * Static class initializer.
     */
    static {
        PROCESSING_THREAD = new Thread(AsyncProcessing::runTask);
        PROCESSING_THREAD.setName("Async Processing Thread");
        PROCESSING_THREAD.start();
    }
    
    /**
     * Enqueue a task.
     * @param toAdd The task to add.
     */
    public static void enqueue(Runnable toAdd) {
        try {
            TASK_QUEUE.put(toAdd);
        } catch (InterruptedException ex) {
            // something strange happened because this is an un-boudned queue
            // it is unlikely to be recoverable so just log the error
            LOGGER.error("Interrupted enqueuing video to process in unbounded queue.", ex);
        }
    }
    
    /**
     * Runs async tasks synchronously.
     */
    private static void runTask() {
        while (true) {
            try {
                TASK_QUEUE.take().run();
            } catch (InterruptedException ex) {
                LOGGER.error("Interrupted waiting for task.", ex);
                return;
            }
        }
    }
}
