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
package com.vitembp.services.interfaces;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;

/**
 * Provides an interface to Amazon S3
 */
public class AmazonSimpleStorageService {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * The name of the queue.
     */
    private final String bucketName;
    
    /**
     * The file transfer manager for the bucket.
     */
    private final TransferManager transferManager;
    
    /**
     * Initializes a new instance of the AmazonSQS class.
     * @param bucketName The name of the queue to connect to.
     */
    public AmazonSimpleStorageService(
            String bucketName) {
        // save parameters
        this.bucketName = bucketName;
        
        // builds a client with credentials
        AmazonS3 client = AmazonS3Client.builder().build();
        
        // builds a transfer manager from the client
        this.transferManager  = TransferManagerBuilder.standard()
                .withS3Client(client)
                .build();
    }
    
    /**
     * Uploads a file to S3 storage and sets its ACL to allow public access.
     * @param toUpload The file to uploadPublic.
     * @param destination The destination in the bucket to store the file.
     * @throws java.io.IOException If an I/O exception occurs while uploading
     * the file.
     */
    public void uploadPublic(File toUpload, String destination) throws IOException {
        try {
            // create a request that makes the object public
            PutObjectRequest req = new PutObjectRequest(this.bucketName, destination, toUpload);
            req.setCannedAcl(CannedAccessControlList.PublicRead);
            
            // uploadPublic the file and wait for completion
            Upload xfer = this.transferManager.upload(req);
            xfer.waitForCompletion();
        } catch (AmazonServiceException ex) {
            LOGGER.error("Exception uploading " + toUpload.toString(), ex);
            throw new IOException("Exception uploading " + toUpload.toString(), ex);
        } catch (AmazonClientException | InterruptedException ex) {
            LOGGER.error("Exception uploading " + toUpload.toString(), ex);
            throw new IOException("Exception uploading " + toUpload.toString(), ex);
        }
    }
    
    /**
     * Downloads a file from S3 storage to the local file system.
     * @param source The item in the bucket to download to the file.
     * @param destination The location to save the file to.
     * @throws java.io.IOException If an I/O exception occurs while downloading
     * the file.
     */
    public void download(String source, File destination) throws IOException {
        try {
            // download the file and wait for completion
            Download xfer = this.transferManager.download(this.bucketName, source, destination);
            xfer.waitForCompletion();
        } catch (AmazonServiceException ex) {
            LOGGER.error("Exception downloading " + source, ex);
            throw new IOException("Exception downloading " + source, ex);
        } catch (AmazonClientException | InterruptedException ex) {
            LOGGER.error("Exception downloading " + source, ex);
            throw new IOException("Exception downloading " + source, ex);
        }
    }
}
