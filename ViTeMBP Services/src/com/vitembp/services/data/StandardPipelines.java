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
import com.vitembp.services.sensors.AccelerometerThreeAxis;
import com.vitembp.services.sensors.DistanceSensor;
import com.vitembp.services.sensors.RotarySensor;
import com.vitembp.services.sensors.Sensor;
import com.vitembp.services.sensors.SensorFactory;
import com.vitembp.services.FilenameGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;

/**
 * Class providing standard pipeline implementations.
 */
public class StandardPipelines {
    public static final String ELEMENT_COUNT_BINDING = "ElementCount";
    public static final String MIN_Z_SUFFIX = "MinZ";
    public static final String MIN_Y_SUFFIX = "MinY";
    public static final String MIN_X_SUFFIX = "MinX";
    public static final String MIN_SUFFIX = "Min";
    public static final String MAX_Z_SUFFIX = "MaxZ";
    public static final String MAX_Y_SUFFIX = "MaxY";
    public static final String MAX_X_SUFFIX = "MaxX";
    public static final String MAX_SUFFIX = "Max";
    public static final String AVERAGE_Z_SUFFIX = "AverageZ";
    public static final String AVERAGE_Y_SUFFIX = "AverageY";
    public static final String AVERAGE_X_SUFFIX = "AverageX";
    public static final String AVERAGE_SUFFIX = "Average";
    
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    public static Pipeline captureVideoOverlayPipeline(Capture capture, Path videoFile) throws IOException {
        List<PipelineElement> toBuild = new ArrayList<>();
        Path outDir = Files.createTempDirectory("vitembp");
        toBuild.add(new CountElement("count"));
        toBuild.add(new FrameExtractorElement(videoFile, outDir, FilenameGenerator.PNG_NUMERIC_OUT, 300, "ProcessingFrame"));
        
        return new Pipeline(toBuild);
    }
    
    /**
     * Builds and returns a pipeline for processing a data capture for video
     * overlay generation.
     * @param toBuildFor The capture to build the pipeline for.
     * @return A pipeline for processing a data capture for video
     * overlay generation
     */
    public static Pipeline captureStatisticsPipeline(Capture toBuildFor) {
        List<PipelineElement> toBuild = new ArrayList<>();
        
        // use the sensor factory to build sensor objects for all known sensor types
        List<Sensor> sensors = buildSensors(toBuildFor);
        
        // add a count for number of elements
        toBuild.add(new CountElement(ELEMENT_COUNT_BINDING));
        
        // for each sensor calculate min, max, and average elements
        sensors.forEach((sensor) -> addAverages(sensor, toBuild));
        sensors.forEach((sensor) -> addMaximums(sensor, toBuild));
        sensors.forEach((sensor) -> addMinimums(sensor, toBuild));
        
        return new Pipeline(toBuild);
    }

    /**
     * Adds average elements to a pipeline being built for various sensor types.
     * @param sensor The sensor to add the average for.
     * @param toBuild The pipeline being built.
     */
    private static void addAverages(Sensor sensor, List<PipelineElement> toBuild) {
        if (RotarySensor.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleAverageElement(
                    ((RotarySensor)sensor)::getPositionDegrees, ELEMENT_COUNT_BINDING,
                    sensor.getName() + AVERAGE_SUFFIX));
        } else if (DistanceSensor.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleAverageElement(
                    ((DistanceSensor)sensor)::getDistanceMilimeters, ELEMENT_COUNT_BINDING,
                    sensor.getName() + AVERAGE_SUFFIX));
        } else if (AccelerometerThreeAxis.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleAverageElement(
                    ((AccelerometerThreeAxis)sensor)::getXAxisG, ELEMENT_COUNT_BINDING,
                    sensor.getName() + AVERAGE_X_SUFFIX));
            toBuild.add(new SampleAverageElement(
                    ((AccelerometerThreeAxis)sensor)::getYAxisG, ELEMENT_COUNT_BINDING,
                    sensor.getName() + AVERAGE_Y_SUFFIX));
            toBuild.add(new SampleAverageElement(
                    ((AccelerometerThreeAxis)sensor)::getZAxisG, ELEMENT_COUNT_BINDING,
                    sensor.getName() + AVERAGE_Z_SUFFIX));
        }
    }

    /**
     * Adds maximum elements to a pipeline being built for various sensor types.
     * @param sensor The sensor to add the average for.
     * @param toBuild The pipeline being built.
     */
    private static void addMaximums(Sensor sensor, List<PipelineElement> toBuild) {
        if (RotarySensor.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleMaxValueElement(
                    ((RotarySensor)sensor)::getPositionDegrees,
                    sensor.getName() + MAX_SUFFIX));
        } else if (DistanceSensor.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleMaxValueElement(
                    ((DistanceSensor)sensor)::getDistanceMilimeters,
                    sensor.getName() + MAX_SUFFIX));
        } else if (AccelerometerThreeAxis.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleMaxValueElement(
                    ((AccelerometerThreeAxis)sensor)::getXAxisG,
                    sensor.getName() + MAX_X_SUFFIX));
            toBuild.add(new SampleMaxValueElement(
                    ((AccelerometerThreeAxis)sensor)::getYAxisG,
                    sensor.getName() + MAX_Y_SUFFIX));
            toBuild.add(new SampleMaxValueElement(
                    ((AccelerometerThreeAxis)sensor)::getZAxisG,
                    sensor.getName() + MAX_Z_SUFFIX));
        }
    }
    
    /**
     * Adds minimum elements to a pipeline being built for various sensor types.
     * @param sensor The sensor to add the average for.
     * @param toBuild The pipeline being built.
     */
    private static void addMinimums(Sensor sensor, List<PipelineElement> toBuild) {
        if (RotarySensor.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleMinValueElement(
                    ((RotarySensor)sensor)::getPositionDegrees,
                    sensor.getName() + MIN_SUFFIX));
        } else if (DistanceSensor.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleMinValueElement(
                    ((DistanceSensor)sensor)::getDistanceMilimeters,
                    sensor.getName() + MIN_SUFFIX));
        } else if (AccelerometerThreeAxis.class.isAssignableFrom(sensor.getClass())) {
            toBuild.add(new SampleMinValueElement(
                    ((AccelerometerThreeAxis)sensor)::getXAxisG,
                    sensor.getName() + MIN_X_SUFFIX));
            toBuild.add(new SampleMinValueElement(
                    ((AccelerometerThreeAxis)sensor)::getYAxisG,
                    sensor.getName() + MIN_Y_SUFFIX));
            toBuild.add(new SampleMinValueElement(
                    ((AccelerometerThreeAxis)sensor)::getZAxisG,
                    sensor.getName() + MIN_Z_SUFFIX));
        }
    }
    
    /**
     * Builds sensor objects for all known sensors in the capture.
     * @param toBuildFor The capture to build sensors for.
     * @return A list containing sensor objects for all known sensors.
     */
    private static List<Sensor> buildSensors(Capture toBuildFor) {
        List<Sensor> toReturn = new ArrayList<>();
        
        toBuildFor.getSensorTypes().entrySet().stream()
                .map((entry) -> {
                    try {
                        return SensorFactory.getSensor(entry.getKey(), entry.getValue());
                    } catch (InstantiationException ex) {
                        LOGGER.error("Unknown sensor: " + entry.getKey() + ", " + entry.getValue().toString() + ".", ex);
                        return null;
                    }
                })
                .forEach(toReturn::add);
        
        return toReturn;
    }
}
