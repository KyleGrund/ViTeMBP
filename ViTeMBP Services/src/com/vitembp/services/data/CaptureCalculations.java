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
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Class containing calculations which can be performed on Captures.
 */
public class CaptureCalculations {
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
        
        data.append(",\"created time\":\"");
        data.append(toProcess.getStartTime().toString());
        data.append("\"");
        
        data.append(",\"created time\":\"");
        data.append(Double.toString(toProcess.getSampleFrequency()));
        data.append("\"");
        
        // sample count
        data.append(",\"sample count\":\"");
        data.append(Long.toString(toProcess.getSampleCount()));
        data.append("\"");
        
        data.append("}");
        
        return data.toString();
    }
}
