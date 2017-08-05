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
import com.vitembp.services.sensors.Sensor;
import com.vitembp.services.sensors.SensorFactory;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * The element creates an image with data to be overlaid onto a video frame.
 */
class FrameDataOverlayGeneratorElement implements PipelineElement {
    private final String framePathBinding;
    
    /**
     * Initializes a new instance of the FrameDataOverlayGeneratorElement class.
     * @param toBuildFor The capture that will be processed.
     * @throws InstantiationException If sensor instances cannot be built for
     * all sensors in the capture.
     */
    FrameDataOverlayGeneratorElement(Capture toBuildFor, String framePathBinding) throws InstantiationException {
        // save parameters
        this.framePathBinding = framePathBinding;
        
        // build up sensor objects for each 
        Map<String, Sensor> sensors = new HashMap<>();
        for (Entry<String, UUID> entry : toBuildFor.getSensorTypes().entrySet()) {
                sensors.put(
                        entry.getKey(),
                        SensorFactory.getSensor(entry.getKey(), entry.getValue()));
        }
        
        
    }
    
    @Override
    public Map<String, Object> accept(Map<String, Object> state) {
        // get the current file to process
        Path frame = (Path)state.get(this.framePathBinding);
        
        // get the graphics processing generator
        
        
        // create the target filename
        
        // build the overlay
        
        // save the overlay
        
        // put overlay file name in state variable
        
        return state;
    }
    
}
