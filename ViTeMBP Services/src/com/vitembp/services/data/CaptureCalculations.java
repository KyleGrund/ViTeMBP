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
import com.vitembp.services.sensors.Sensor;
import com.vitembp.services.sensors.SensorFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import static java.util.stream.Collectors.toMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class containing calculations which can be performed on Captures.
 */
public class CaptureCalculations {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Calculates and returns summary information about the capture.
     * @param captureLocation The location of the capture in the data store.
     * @return The summarized data about the capture.
     */
    public static String getSummaryData(UUID captureLocation) throws IOException {
        StringBuilder data = new StringBuilder();
        
        // load the capture
        Capture toProcess = CaptureOperations.getCaptureAtLocation(captureLocation);
        
        // build summary info
        data.append("{\"sensor names\":[");
        
        // sensor names
        Set<String> names = toProcess.getSensorNames();
        names.forEach((elm) -> {
            data.append("\"");
            data.append(elm);
            data.append("\",");
        });
        data.setCharAt(data.length() - 1, ']');
        
        // capture start time
        data.append(",\"created time\":\"");
        data.append(toProcess.getStartTime().toString());
        data.append("\"");
        
        // capture run time
        data.append(",\"capture length\":\"");
        data.append(Double.toString(toProcess.getSampleCount() / toProcess.getSampleFrequency()));
        data.append("\"");
        
        // samling frequency
        data.append(",\"sample frequency\":\"");
        data.append(Double.toString(toProcess.getSampleFrequency()));
        data.append("\"");
        
        // sample count
        data.append(",\"sample count\":\"");
        data.append(Long.toString(toProcess.getSampleCount()));
        data.append("\"");
        
         // is complete
        data.append(",\"is complete\":\"");
        data.append(Boolean.toString(toProcess.isComplete()));
        data.append("\"");
        
        data.append("}");
        
        return data.toString();
    }
    
    /**
     * Calculates and returns summary information about the capture.
     * @param captureLocation The location of the capture in the data store.
     * @param points The number of points to reduce the data to.
     * @return The summarized data about the capture.
     * @throws java.io.IOException If an error occurs reading from data store.
     */
    public static String buildGraphDataForCapture(UUID captureLocation, int points) throws IOException {
        // load the capture
        Capture toProcess = CaptureOperations.getCaptureAtLocation(captureLocation);
        
        // build pipeline to get statistics
        Pipeline statsPipe = StandardPipelines.captureStatisticsPipeline(
                toProcess,
                SensorFactory.getSensors(toProcess));
        
        // find number of samples per point
        int samplesPerPoint = (int)Math.ceil(
                ((double)toProcess.getSampleCount()) / ((double)points));
        
        // create maps for final results
        List<Map<String, Double>> min = new ArrayList<>();
        List<Map<String, Double>> max = new ArrayList<>();
        List<Map<String, Double>> avg = new ArrayList<>();
                
        // step through data and calculate the results
        Iterator<Sample> iter = toProcess.getSamples().iterator();
        List<Sample> toProc = new ArrayList<>();
        
        while (iter.hasNext()) {
            toProc.add(iter.next());
            if (toProc.size() >= samplesPerPoint) {
                Map<String, Object> stats = CaptureProcessor.process(toProc.stream(), statsPipe);
                
                // get specific results changing map key to sensor name
                min.add(((Map<Sensor, Double>)stats.get(StandardPipelines.MIN_BINDING)).entrySet().stream()
                        .collect(toMap(e -> e.getKey().getName(), Entry::getValue)));
                max.add(((Map<Sensor, Double>)stats.get(StandardPipelines.MAX_BINDING)).entrySet().stream()
                        .collect(toMap(e -> e.getKey().getName(), Entry::getValue)));
                avg.add(((Map<Sensor, Double>)stats.get(StandardPipelines.AVERAGE_BINDING)).entrySet().stream()
                        .collect(toMap(e -> e.getKey().getName(), Entry::getValue)));
                
                // clear the Items
                toProc.clear();
            }
        }
        
        // build return string
        StringBuilder toReturn = new StringBuilder();
        
        Set<String> sensorNames = toProcess.getSensorNames();
        
        toReturn.append("[['index',");
        
        sensorNames.forEach((n) -> {
            toReturn.append("'");
            toReturn.append(n);
            toReturn.append("',");
        });
        
        toReturn.setCharAt(toReturn.length() - 1, ']');
        toReturn.append(",");
        
        for (int i = 0; i < avg.size(); i++) {
            Map<String, Double> pt = avg.get(i);
            toReturn.append("[");
            toReturn.append(i);
            toReturn.append(",");
            sensorNames.forEach(sensorName -> {
                toReturn.append(pt.get(sensorName));
                toReturn.append(",");
            });
            toReturn.setCharAt(toReturn.length() - 1, ']');
            toReturn.append(",");
        }
        toReturn.setCharAt(toReturn.length() - 1, ']');
        
        return toReturn.toString();
    }
}
