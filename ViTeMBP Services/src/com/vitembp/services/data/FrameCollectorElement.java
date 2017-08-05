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

import java.util.Map;

/**
 * This element collects frames and composes them into an MP4.
 */
class FrameCollectorElement implements PipelineElement {

    @Override
    public Map<String, Object> accept(Map<String, Object> state) {
        // get current file name
        
        // add to merege list
        
        // if merge list is greater than batch size
        {
            // encode merge list to video file
            
            // delete files in merge list
            
            // clear merge list
        }
        
        return state;
    }
    
}
