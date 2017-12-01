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
import com.vitembp.embedded.data.Sample;
import com.vitembp.services.sensors.AccelerometerThreeAxis;
import com.vitembp.services.sensors.DistanceSensor;
import com.vitembp.services.sensors.RotarySensor;
import com.vitembp.services.sensors.Sensor;
import com.vitembp.services.FilenameGenerator;
import com.vitembp.services.video.VideoFileInfo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;

/**
 * Class providing standard pipeline implementations.
 */
public class StandardPipelines {
    /**
     * The binding location on the data object of the element count results.
     */
    public static final String ELEMENT_COUNT_BINDING = "elementCount";
    
    /**
     * The binding location on the data object of the sensor minimums results.
     */
    public static final String MIN_BINDING = "minimums";
    
    /**
     * The binding location on the data object of the sensor maximums results.
     */
    public static final String MAX_BINDING = "maximums";
    
    /**
     * The binding location on the data object of the sensor averages results.
     */
    public static final String AVERAGE_BINDING = "averages";
    
    /**
     * The binding location on the data object of the sensors list.
     */
    public static final String SENSORS_BINDING = "sensors";
    
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * Function that returns a vector absolute value function for a three axis
     * accelerometer.
     */
    private static Function<AccelerometerThreeAxis, Function<Sample, Optional<Double>>> absoluteValue = (sensor) -> {
        return (sample) -> {
            // read out x, y, z values
            Optional<Double> x = ((AccelerometerThreeAxis)sensor).getXAxisG(sample);
            Optional<Double> y = ((AccelerometerThreeAxis)sensor).getYAxisG(sample);
            Optional<Double> z = ((AccelerometerThreeAxis)sensor).getZAxisG(sample);

            // if any values are not present calculation is meaningless, so return empty
            if (!x.isPresent() || !y.isPresent() || !z.isPresent()) {
                return Optional.empty();
            }

            // return the vector absolute value of the reading
            return Optional.of(Math.sqrt(
                    Math.pow(x.get(), 2) +
                    Math.pow(y.get(), 2) +
                    Math.pow(z.get(), 2)));
        };
    };
    
    /**
     * Creates a video overlay generation pipeline.
     * @param capture The capture to build an overlay generator for.
     * @param videoFile The input video file to build the overlay generator for.
     * @param outputFile The file to output the overlaid video to.
     * @param overlayDefinition The definition of the overlay to add.
     * @return The built up overlay.
     * @throws InstantiationException If the pipeline cannot be built.
     */
    public static Pipeline captureVideoOverlayPipeline(Capture capture, Path videoFile, Path outputFile, String overlayDefinition) throws InstantiationException {
        // verify output file doesn't exist
        if (Files.exists(outputFile)) {
            LOGGER.error("Output file already exits: " + outputFile.toString() + ".");
            throw new InstantiationException("Output file already exits: " + outputFile.toString() + ".");
        }
        
        // list of elements that make up the pipeline
        List<PipelineElement> toBuild = new ArrayList<>();
        
        // create a temp dir for intermediate processing files
        Path outDir;
        try {
            outDir = Files.createTempDirectory("vitembp");
        } catch (IOException ex) {
            LOGGER.error("Exception getting video file information.", ex);
            throw new InstantiationException("Could not load video file information.");
        }
        
        // create file properties objects
        VideoFileInfo videoInfo = new VideoFileInfo(videoFile.toFile());
        FilenameGenerator filenameGenerator = FilenameGenerator.PNG_NUMERIC_OUT;
        toBuild.add(new SeedValueElement("VideoOutputFile", outputFile));
        toBuild.add(new SeedValueElement("VideoInputFile", videoFile));
        toBuild.add(new CountElement("Count"));
        toBuild.add(new FrameExtractorElement(videoFile, outDir, filenameGenerator, 300, "ProcessingFrame", "Count"));
        toBuild.add(new FrameDataOverlayGeneratorElement(capture, videoInfo, overlayDefinition, "ProcessingFrame"));
        toBuild.add(new FrameCollectorElement(filenameGenerator, "ProcessingFrame", "NewVideoSegment", "VideoOutputFile", 300, videoInfo.getFrameRate()));
        toBuild.add(new VideoCollectorElement("NewVideoSegment", "VideoOutputFile"));
        toBuild.add(new CopyAudioElement("VideoInputFile", "VideoOutputFile", "AudioCopied"));
        
        return new Pipeline(toBuild);
    }
    
    /**
     * Builds and returns a pipeline for processing a data capture for video
     * overlay generation.
     * @param toBuildFor The capture to build the pipeline for.
     * @param sensors The sensors used to decode the capture data.
     * @return A pipeline for processing a data capture for video
     * overlay generation
     */
    public static Pipeline captureStatisticsPipeline(Capture toBuildFor, Map<String, Sensor> sensors) {
        List<PipelineElement> toBuild = new ArrayList<>();
                
        // add a seed for the sensors data
        toBuild.add(new SeedValueElement(SENSORS_BINDING, sensors));
        
        // add a count for number of elements
        toBuild.add(new CountElement(ELEMENT_COUNT_BINDING));
                
        // for each sensor calculate min, max, and average elements
        sensors.values().forEach((sensor) -> addAverages(sensor, toBuild));
        sensors.values().forEach((sensor) -> addMaximums(sensor, toBuild));
        sensors.values().forEach((sensor) -> addMinimums(sensor, toBuild));
        
        return new Pipeline(toBuild);
    }
    
    /**
     * Adds average elements to a pipeline being built for various sensor types.
     * @param sensor The sensor to add the average for.
     * @param toBuild The pipeline being built.
     */
    private static void addAverages(Sensor sensor, List<PipelineElement> toBuild) {
        // add averaging elements based on sensor type
        if (RotarySensor.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleAverageElement(
                    ((RotarySensor)sensor)::getPositionPercentage,
                    ELEMENT_COUNT_BINDING,
                    AVERAGE_BINDING,
                    sensor));
        } else if (DistanceSensor.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleAverageElement(
                    ((DistanceSensor)sensor)::getDistancePercent,
                    ELEMENT_COUNT_BINDING,
                    AVERAGE_BINDING,
                    sensor));
        } else if (AccelerometerThreeAxis.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleAverageElement(
                    StandardPipelines.absoluteValue.apply((AccelerometerThreeAxis)sensor),
                    ELEMENT_COUNT_BINDING,
                    AVERAGE_BINDING,
                    sensor));
        }
    }

    /**
     * Adds maximum elements to a pipeline being built for various sensor types.
     * @param sensor The sensor to add the average for.
     * @param toBuild The pipeline being built.
     */
    private static void addMaximums(Sensor sensor, List<PipelineElement> toBuild) {
        // add maximum calculating elements based on type
        if (RotarySensor.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleMaxValueElement(
                    ((RotarySensor)sensor)::getPositionPercentage,
                    MAX_BINDING, sensor));
        } else if (DistanceSensor.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleMaxValueElement(
                    ((DistanceSensor)sensor)::getDistancePercent,
                    MAX_BINDING, sensor));
        } else if (AccelerometerThreeAxis.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleMaxValueElement(
                    StandardPipelines.absoluteValue.apply((AccelerometerThreeAxis)sensor),
                    MAX_BINDING, sensor));
        }
    }
    
    /**
     * Adds minimum elements to a pipeline being built for various sensor types.
     * @param sensor The sensor to add the average for.
     * @param toBuild The pipeline being built.
     */
    private static void addMinimums(Sensor sensor, List<PipelineElement> toBuild) {
        // add minimum calculating elements based on type
        if (RotarySensor.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleMinValueElement(
                    ((RotarySensor)sensor)::getPositionPercentage,
                    MIN_BINDING, sensor));
        } else if (DistanceSensor.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleMinValueElement(
                    ((DistanceSensor)sensor)::getDistancePercent,
                    MIN_BINDING, sensor));
        } else if (AccelerometerThreeAxis.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleMinValueElement(
                    StandardPipelines.absoluteValue.apply((AccelerometerThreeAxis)sensor),
                    MIN_BINDING, sensor));
        }
    }
}
