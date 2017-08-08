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
package com.vitembp.services.imaging;

import com.vitembp.services.sensors.Sensor;
import com.vitembp.services.video.VideoFileInfo;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A factory class that builds an overlay from a definition.
 */
public class OverlayFactory {
    public static Overlay buildOverlay(String definition, List<Sensor> sensors, Map<Sensor, Double> minimumValues, Map<Sensor, Double> maximumValues, VideoFileInfo videoInfo) throws InstantiationException {
        OverlayDefinition def;
        try {
            def = OverlayDefinition.getDefinition(definition);
        } catch(IOException ex) {
            throw new InstantiationException("Exception loading definition.");
        }
        
        switch (def.getOverlayType()) {
            case FourQuadrant:
                return new FourQuadrantOveraly(
                        videoInfo.getHorizontalResolution(),
                        videoInfo.getVerticalResolution(),
                        sensors,
                        minimumValues,
                        maximumValues,
                        def.getElementDefinitions());
        }
        
        throw new InstantiationException("Invalid overlay definition.");
    }
}
