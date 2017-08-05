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

import com.vitembp.embedded.data.Sample;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface used to add an overlay to an existing image.
 */
public interface Overlay {
    /**
     * Adds an overlay to an existing image.
     * @param image The image to overlay.
     * @param data The data sample to overlay on the image.
     * @throws java.io.IOException If there is an error reading or writing the
     * image file.
     */
    public void addOverlay(Path image, Sample data) throws IOException;
}
