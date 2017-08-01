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
package com.vitembp.data;

import com.vitembp.embedded.data.Sample;
import java.util.Map;

/**
 * Represents an element in a processing pipeline for performing calculations
 * on samples.
 */
@FunctionalInterface
public abstract interface SamplePipeline {
    /**
     * Performs a calculation in a pipeline.
     * @param toAccept The sample stream which will be processed.
     * @param state The state containing necessary data and results.
     */
    public abstract void accept(final Sample toAccept, final Map<String, Object> state);
}
