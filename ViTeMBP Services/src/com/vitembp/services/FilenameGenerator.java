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
package com.vitembp.services;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class which provides generators which produce identical names in varying
 * contexts.
 */
public abstract class FilenameGenerator {
    /**
     * A filename generator for the format "outXXXXX.png".
     */
    public static FilenameGenerator PNG_NUMERIC_OUT = new FilenameGenerator() {
        @Override
        public String getString(int index) {
            StringBuilder fileName = new StringBuilder();
            String fileNum = Integer.toString(index);
            fileName.append("out");
            while (fileName.length() + fileNum.length() < 8) {
                fileName.append("0");
            }
            fileName.append(fileNum);
            fileName.append(".png");
            return fileName.toString();
        }

        @Override
        public String getFFmpegString() {
            return "out%05d.png";
        } 
    };
    
    /**
     * Private default constructor making this class closed.
     */
    private FilenameGenerator() { }
    
    /**
     * Gets the filename in the sequence as a String for the given index.
     * @param index The index of the name to return.
     * @return The filename as a String.
     */
    public abstract String getString(int index);
    
    /**
     * Gets the String which allows FFmpeg to generate the matching 
     * sequence to this class.
     * @return The filename generator as a String.
     */
    public abstract String getFFmpegString();
    
    /**
     * Gets the filename in the sequence as a Path for the given index.
     * @param index The index of the name to return.
     * @return The filename as a Path.
     */
    public Path getPath(int index) {
        return Paths.get(this.getString(index));
    }
}
