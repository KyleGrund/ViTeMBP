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
package com.vitembp.services.video;

import com.vitembp.embedded.data.Capture;
import com.vitembp.embedded.data.CaptureFactory;
import com.vitembp.embedded.data.CaptureTypes;
import com.vitembp.embedded.data.Sample;
import com.vitembp.services.imaging.SyncDiagFrameProcessor;
import com.vitembp.services.ApiFunctions;
import com.vitembp.services.FilenameGenerator;
import com.vitembp.services.config.ServicesConfig;
import com.vitembp.services.data.CaptureProcessor;
import com.vitembp.services.data.Pipeline;
import com.vitembp.services.data.StandardOverlayDefinitions;
import com.vitembp.services.data.StandardPipelines;
import com.vitembp.services.imaging.Histogram;
import com.vitembp.services.imaging.HistogramList;
import com.vitembp.services.interfaces.AmazonSimpleStorageService;
import com.vitembp.services.sensors.AccelerometerThreeAxis;
import com.vitembp.services.sensors.DistanceSensor;
import com.vitembp.services.sensors.RotarySensor;
import com.vitembp.services.sensors.Sensor;
import com.vitembp.services.sensors.SensorFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Functions for processing of video files.
 */
public class Processing {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * The number of standard deviations out in brightness a color channel frame
     * must be to be considered an outlier and thus a synchronization frame.
     */
    private static final double OUTLIER_DEVIATIONS = 2;
    
    /**
     * Finds the frames which have an outlier brightness in the given color.
     * channel.
     * @param videoFile The file to examine.
     * @param channel The color channel to evaluate.
     * @param fileGenerator The filename generator which defines the names of
     * sequential files to use when processing frames.
     * @param overlayOutput The filename to output the video with overlay data.
     * @return The frames which have an outlier brightness in the given color.
     * @throws java.io.IOException If there is an IOException processing the
     * video file.
     */
    public static List<Integer> findChannelSyncFrames(String videoFile, ApiFunctions.COLOR_CHANNELS channel, FilenameGenerator fileGenerator, Path overlayOutput) throws IOException {
        // build a temporary directory for images
        Path tempDir = Files.createTempDirectory("vitempb");
        
        // creates the function which picks the color channel on the histograms
        // when processing to find the synchronization frames
        Function<Histogram, Double> selector;
        switch (channel) {
            case RED:
                selector = (histogram) -> histogram.getRedBrightness();
                break;
            case GREEN:
                selector = (histogram) -> histogram.getGreenBrightness();
                break;
            case BLUE:
                selector = (histogram) -> histogram.getBlueBrightness();
                break;
            default:
                throw new IllegalArgumentException("Unknown color channel specified.");
        }
        
        // crate images with ffmpeg
        Conversion.extractFrames(videoFile, tempDir, 0, 900, fileGenerator);
        
        // create histogram list from images
        HistogramList histograms = HistogramList.loadFromDirectory(tempDir, fileGenerator);
        
        // get stats for diagnostics
        double stdDev = histograms.getPosStdev(selector);
        double average = histograms.getAverage(selector);
        
        // return outliers which are the sync frames
        List<Integer> outliers = histograms.getPositiveOutliers(selector, OUTLIER_DEVIATIONS);
        
        // if diagnostic data requested build it now
        if (overlayOutput != null) {
            VideoFileInfo info = new VideoFileInfo(videoFile);
            Processing.buildDiagOverlay(histograms, (OUTLIER_DEVIATIONS * stdDev) + average, outliers, fileGenerator, tempDir, overlayOutput, info);
            // uploadPublic to S3
            AmazonSimpleStorageService s3 = new AmazonSimpleStorageService("www-vitembp-com");
            s3.uploadPublic(overlayOutput.toFile(), "debug/" + overlayOutput.getFileName());
        }
        
        deleteTree(tempDir);
        
        // return synchronization frames
        return outliers;
    }
    
    /**
     * Processes a video with a capture.
     * @param capture The capture containing data to overlay on the video.
     * @param inputBucket The bucket to read the video from.
     * @param outputBucket The bucket to store the output video in.
     * @param videoKey The S3 key pointing to the file to process.
     * @throws java.io.IOException If there is an IOException processing the
     * video file.
     */
    public static void processVideo(UUID capture, String inputBucket, String outputBucket, String videoKey) throws IOException {
        // create S3 bucket interface objects
        AmazonSimpleStorageService sourceBucket = new AmazonSimpleStorageService(inputBucket);
        AmazonSimpleStorageService destinationBucket = new AmazonSimpleStorageService(outputBucket);

        // download the source file to temporary directory
        Path tempDir = ServicesConfig.getConfig().getTemporaryDirectory();
        Path videoFilename = Paths.get(videoKey).getFileName();
        Path localVideoSourcePath = tempDir.resolve(videoFilename);
        File localVideoSource = localVideoSourcePath.toFile();
        sourceBucket.download(videoKey, localVideoSource);
        
        // find video sync frames
        List<Integer> frames = Processing.findChannelSyncFrames(localVideoSource.getAbsolutePath(), ApiFunctions.COLOR_CHANNELS.GREEN, FilenameGenerator.PNG_NUMERIC_OUT);
        
        if (frames.isEmpty()) {
            LOGGER.warn("No sync frames detected, will sync to frame 0.");
            frames.add(0);
        }
        // create a temporary file for the output
        Path localTempOutput = 
                Files.createTempFile(tempDir, null, videoFilename.toString());
        localTempOutput.toFile().delete();

        // get the capture to process from the database
        Capture toProcess;
        try {
            Stream<Capture> allCaptures = java.util.stream.StreamSupport.stream(
                    CaptureFactory.getCaptures(CaptureTypes.AmazonDynamoDB).spliterator(),
                    false);
            toProcess = allCaptures.filter((Capture c) -> c.getId().equals(capture)).findFirst().get();
        } catch (InstantiationException ex) {
            throw new IOException("Could not read captures from database.", ex);
        }

        // build up the processing pipeline
        Pipeline toTest;
        try {
            toTest = StandardPipelines.captureVideoOverlayPipeline(toProcess, localVideoSourcePath, localTempOutput, StandardOverlayDefinitions.getStandardFourQuadrant());
        } catch (InstantiationException ex) {
            LOGGER.error("Could not create overlay pipeline.", ex);
            throw new IOException("Could not create overlay pipeline.", ex);
        }

        // process the data
        Map<String, Object> results = CaptureProcessor.processUntilFlush(toProcess, toTest, 75);

        // upload to the destination in the target S3 bucket.
        destinationBucket.uploadPublic(localTempOutput.toFile(), videoKey);
    }
    
    /**
     * Exports un-calibrated data from the capture to a CSV file in the target bucket.
     * @param capture The capture containing data to overlay on the video.
     * @param outputBucket The bucket to store the output video in.
     * @throws java.io.IOException If there is an IOException processing the
     * video file.
     */
    public static void exportRawData(UUID capture, String outputBucket) throws IOException {
        // create S3 bucket to save data to
        AmazonSimpleStorageService destinationBucket = new AmazonSimpleStorageService(outputBucket);
        String outputFilename = "RawData.csv";
        
        // download the source file to temporary directory
        Path tempDir = ServicesConfig.getConfig().getTemporaryDirectory();
        
        // create a temporary file for the output
        Path localTempOutput = 
                Files.createTempFile(tempDir, null, outputFilename);
        localTempOutput.toFile().delete();

        // get the capture to process from the database
        Capture toProcess;
        try {
            Stream<Capture> allCaptures = java.util.stream.StreamSupport.stream(
                    CaptureFactory.getCaptures(CaptureTypes.AmazonDynamoDB).spliterator(),
                    false);
            toProcess = allCaptures.filter((Capture c) -> c.getId().equals(capture)).findFirst().get();
        } catch (InstantiationException ex) {
            throw new IOException("Could not read captures from database.", ex);
        }

        // write out header
        try ( // create output file
                FileWriter outFile = new FileWriter(localTempOutput.toFile())) {
            // write out header
            String[] names = toProcess.getSensorNames().toArray(new String[0]);
            StringBuilder nameLine = new StringBuilder();
            toProcess.getSensorNames().forEach((name) -> {
                nameLine.append(name);
                nameLine.append(",");
            });
            nameLine.deleteCharAt(nameLine.length() - 1);
            nameLine.append('\n');
            outFile.write(nameLine.toString());
            
            // process the data
            Iterator<Sample> samples = toProcess.getSamples().iterator();
            while (samples.hasNext()) {
                Sample toWrite = samples.next();
                StringBuilder nextLine = new StringBuilder();
                for (String name : names) {
                    if (toWrite.getSensorData().containsKey(name)) {
                        nextLine.append('"');
                        nextLine.append(toWrite.getSensorData().get(name));
                        nextLine.append('"');
                        nextLine.append(',');
                    }
                }
                nextLine.deleteCharAt(nextLine.length() - 1);
                nextLine.append("\n");
                outFile.write(nextLine.toString());
            }
        }

        // upload to the destination in the target S3 bucket.
        destinationBucket.uploadPublic(localTempOutput.toFile(), capture.toString() + "/" + outputFilename);
        
        // delte local file
        localTempOutput.toFile().delete();
    }
    
    /**
     * Exports calibrated data from the capture to a CSV file in the target bucket.
     * @param capture The capture containing data to overlay on the video.
     * @param outputBucket The bucket to store the output video in.
     * @throws java.io.IOException If there is an IOException processing the
     * video file.
     */
    public static void exportCalData(UUID capture, String outputBucket) throws IOException {
        // create S3 bucket to save data to
        AmazonSimpleStorageService destinationBucket = new AmazonSimpleStorageService(outputBucket);
        String outputFilename = "CalibratedData.csv";
        
        // download the source file to temporary directory
        Path tempDir = ServicesConfig.getConfig().getTemporaryDirectory();
        
        // create a temporary file for the output
        Path localTempOutput = 
                Files.createTempFile(tempDir, null, outputFilename);
        localTempOutput.toFile().delete();

        // get the capture to process from the database
        Capture toProcess;
        try {
            Stream<Capture> allCaptures = java.util.stream.StreamSupport.stream(
                    CaptureFactory.getCaptures(CaptureTypes.AmazonDynamoDB).spliterator(),
                    false);
            toProcess = allCaptures.filter((Capture c) -> c.getId().equals(capture)).findFirst().get();
        } catch (InstantiationException ex) {
            throw new IOException("Could not read captures from database.", ex);
        }

        // create output file
        try (FileWriter outFile = new FileWriter(localTempOutput.toFile())) {
            // write out header
            String[] names = toProcess.getSensorNames().toArray(new String[0]);
            StringBuilder nameLine = new StringBuilder();
            toProcess.getSensorNames().forEach((name) -> {
                nameLine.append(name);
                nameLine.append(",");
            });
            nameLine.deleteCharAt(nameLine.length() - 1);
            nameLine.append('\n');
            outFile.write(nameLine.toString());
            
            // get sensors for processing calibrated data
            Map<String, Sensor> sensors = SensorFactory.getSensors(toProcess);
            
            // process the data
            Iterator<Sample> samples = toProcess.getSamples().iterator();
            while (samples.hasNext()) {
                Sample toWrite = samples.next();
                StringBuilder nextLine = new StringBuilder();
                for (String name : names) {
                    // write data
                    if (toWrite.getSensorData().containsKey(name)) {
                        // get calibrated data
                        Sensor sensor = sensors.get(name);
                        String exportData = "";
                        if (sensor instanceof RotarySensor) {
                            exportData = Double.toString(
                                ((RotarySensor)sensor).getPositionPercentage(toWrite).get());
                        } else if (sensor instanceof DistanceSensor) {
                            exportData = Double.toString(
                                ((DistanceSensor)sensor).getDistancePercent(toWrite).get());
                        }else if (sensor instanceof AccelerometerThreeAxis) {
                            double x = ((AccelerometerThreeAxis)sensor).getXAxisG(toWrite).get();
                            double y = ((AccelerometerThreeAxis)sensor).getYAxisG(toWrite).get();
                            double z = ((AccelerometerThreeAxis)sensor).getZAxisG(toWrite).get();
                            
                            exportData = Double.toString(Math.sqrt(
                                    Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)));
                        } else {
                            LOGGER.error("Unknown sensor type exporting calibrated data.");
                        }
                        
                        // write out calibrated data
                        nextLine.append('"');
                        nextLine.append(exportData);
                        nextLine.append('"');
                        nextLine.append(',');
                    }
                }
                nextLine.deleteCharAt(nextLine.length() - 1);
                nextLine.append("\n");
                outFile.write(nextLine.toString());
            }
        }

        // upload to the destination in the target S3 bucket.
        destinationBucket.uploadPublic(localTempOutput.toFile(), capture.toString() + "/" + outputFilename);
        
        // delte local file
        localTempOutput.toFile().delete();
    }

    /**
     * Deletes the directory including all subdirectories and files.
     * @param tempDir The directory to delete.
     * @throws IOException If an exception occurs during deletion.
     */
    private static void deleteTree(Path tempDir) throws IOException {
        // recursively delete all files in temp directory
        Files.walkFileTree(tempDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException
            {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e)
                    throws IOException
            {
                if (e == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    // directory iteration failed
                    throw e;
                }
            }
        });
    }
    
    /**
     * Finds the frames which have an outlier brightness in the given color.
     * channel.
     * @param videoFile The file to examine.
     * @param channel The color channel to evaluate.
     * @param fileGenerator The filename generator which defines the names of
     * sequential files to use when processing frames.
     * @return The frames which have an outlier brightness in the given color.
     * @throws java.io.IOException If there is an IOException processing the
     * video file.
     */
    public static List<Integer> findChannelSyncFrames(String videoFile, ApiFunctions.COLOR_CHANNELS channel, FilenameGenerator fileGenerator) throws IOException {
        return Processing.findChannelSyncFrames(videoFile, channel, fileGenerator, null);
    }

    /**
     * Builds a video from frames with overlaid histogram data.
     * @param histograms The histograms for the frames.
     * @param targetValue The target value of the channel to be considered a
     * sync frame.
     * @param syncFrames The list of sync frames that were found by the
     * algorithm.
     * @param fileGenerator The filename generator which defines the names of
     * sequential files to use when processing frames.
     * @param overlayOutput The output video file.
     */
    private static void buildDiagOverlay(HistogramList histograms, double tagetValue, List<Integer> syncFrames, FilenameGenerator fileGenerator, Path inputDir, Path overlayOutput, VideoFileInfo inputFileInfo) throws IOException {
        SyncDiagFrameProcessor.getFrameProcessor(syncFrames, tagetValue).buildDiagOverlay(histograms, fileGenerator, inputDir, inputFileInfo);
        
        // build the output video for the overlaid frames
        Conversion.assembleFrames(inputDir, overlayOutput, fileGenerator, inputFileInfo.getFrameRate());
    }
}
