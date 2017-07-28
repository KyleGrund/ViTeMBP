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
package com.vitembp.sensors;

import com.vitembp.embedded.data.Sample;
import java.util.UUID;

/**
 * Class providing interface to the FXOS8700CQ sensor.
 */
class AccelerometerFXOS8700CQSerial extends AccelerometerThreeAxis {
    /**
     * A UUID representing the type of this sensor.
     */
    static final UUID TYPE_UUID = UUID.fromString("fe3c4af2-feb4-4c9b-a717-2d0db3052293");

    /**
     * Initializes a new instance of the AccelerometerFXOS8700CQSerial class.
     * @param name The name of the sensor.
     */
    public AccelerometerFXOS8700CQSerial(String name) {
        super(name);
    }

    @Override
    public double getXAxisG(Sample toDecode) {
        String[] values = this.getData(toDecode).split(",");
        String trimmed = values[0].trim().substring(1);
        double value = Double.parseDouble(trimmed);
        return value;
    }

    @Override
    public double getYAxisG(Sample toDecode) {
        String[] values = this.getData(toDecode).split(",");
        String trimmed = values[1].trim();
        double value = Double.parseDouble(trimmed);
        return value;
    }

    @Override
    public double getZAxisG(Sample toDecode) {
        String[] values = this.getData(toDecode).split(",");
        String trimmed = values[2].trim();
        trimmed = trimmed.substring(0, trimmed.length() - 1);
        double value = Double.parseDouble(trimmed);
        return value;
    }
}
