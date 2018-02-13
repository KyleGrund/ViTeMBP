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
 * This element simply adds a data item to the state variable.
 */
class SeedValueElement implements PipelineElement {
    /**
     * The name of the binding on the data object to add value to.
     */
    private final String bindingToAdd;
    
    /**
     * The value to add to the data object.
     */
    private final Object valueToAdd;
    
    /**
     * Initializes a new instance of the AddValueElement class.
     * @param bindingToAdd The location on the data object to add the value to.
     * @param valueToAdd The value to add to the data object.
     */
    SeedValueElement(String bindingToAdd, Object valueToAdd) {
        this.bindingToAdd = bindingToAdd;
        this.valueToAdd = valueToAdd;
    }
    
    @Override
    public Map<String, Object> accept(Map<String, Object> state) throws PipelineExecutionException {
        // simply adds the value if it is not already there
        if (!state.containsKey(this.bindingToAdd)) {
            state.put(this.bindingToAdd, this.valueToAdd);
        }
        
        return state;
    }
    
}
