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
import com.vitembp.services.imaging.Overlay;
import com.vitembp.services.imaging.OverlayFactory;
import com.vitembp.services.sensors.Sensor;
import com.vitembp.services.sensors.SensorFactory;
import com.vitembp.services.video.VideoFileInfo;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import org.apache.logging.log4j.LogManager;

/**
 * The element creates an image with data to be overlaid onto a video frame.
 */
class FrameDataOverlayGeneratorElement implements PipelineElement {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * The location on the data object with the frame Path to process.
     */
    private final String framePathBinding;
    
    /**
     * The overlay to apply.
     */
    private final Overlay overlay;
    
    /**
     * Initializes a new instance of the FrameDataOverlayGeneratorElement class.
     * @param toBuildFor The capture that will be processed.
     * @throws InstantiationException If sensor instances cannot be built for
     * all sensors in the capture.
     */
    FrameDataOverlayGeneratorElement(Capture toBuildFor, VideoFileInfo videoInfo, String overlayDefinition, String framePathBinding) throws InstantiationException {
        // save parameters
        this.framePathBinding = framePathBinding;
        
        // build sensors used to decode the data from the capture
        Map<String, Sensor> sensors = SensorFactory.getSensors(toBuildFor);
        
        // calculate basic statistics for capture
        Pipeline statsPipe = StandardPipelines.captureStatisticsPipeline(toBuildFor, sensors);
        Map<String, Object> stats = CaptureProcessor.process(toBuildFor.getSamples(), statsPipe);
        
        this.overlay = OverlayFactory.buildOverlay(
                overlayDefinition,
                new ArrayList<>(((Map<String, Sensor>)stats.get(StandardPipelines.SENSORS_BINDING)).values()),
                (Map<Sensor, Double>)stats.get(StandardPipelines.MIN_BINDING),
                (Map<Sensor, Double>)stats.get(StandardPipelines.MAX_BINDING),
                videoInfo);
    }
    
    @Override
    public Map<String, Object> accept(Map<String, Object> state) {
        // do not process data if the pipeline is flushing
        if (state.containsKey("Flush")) {
            return state;
        }
        
        // get the current file to process
        Path frame = (Path)state.get(this.framePathBinding);
        
        // get the data to apply
        Sample toAccept = (Sample)state.get("sample");
        
        if (toAccept != null) {
            try {
                // apply the overlay
                this.overlay.addOverlay(frame, toAccept);
            } catch (IOException ex) {
                LOGGER.error("IOException occurred generating overlay for: " + frame.toString(), ex);
            }
        }
        
        return state;
    }
}
