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

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class providing composition ability for pipelined elements.
 */
public class Pipeline {
    /**
     * The composed pipeline represented by this class.
     */
    private final Consumer<Map<String, Object>> pipeline;
    
    /**
     * Initializes a new instance of the Pipeline class.
     * @param elements The elements that will be composed to make this instance.
     */
    public Pipeline(final List<PipelineElement> elements) {
        if (elements.isEmpty()) {
            this.pipeline = (data) -> {};
        } else { 
            // start pipeline with the first element
            Function<Map<String, Object>, Map<String, Object>> toRet =
                    elements.get(0)::accept;

            // if there are additional elements add them to the pipeline in order
            for (int i = 1; i < elements.size(); i++) {
                toRet = Pipeline.compose(toRet, elements.get(i)::accept);
            }

            this.pipeline = toRet::apply;
        }
    }
    
    /**
     * Executes a pipeline for the supplied data.
     * @param data The data for the pipeline to process.
     */
    public void execute(Supplier<Map<String, Object>> data){
        Map<String, Object> item;
        while ((item = data.get()) != null) {
            this.pipeline.accept(item);
        }
    }
    
    /**
     * Composes pipeline elements for sequential execution.
     * @param first The element to be executed.
     * @param second The second element to be executed.
     * @return The composed function.
     */
    private static Function<Map<String, Object>, Map<String, Object>> compose(final Function<Map<String, Object>, Map<String, Object>> first, final Function<Map<String, Object>, Map<String, Object>> second) {
        return (data) -> second.apply(first.apply(data));
    }
}
