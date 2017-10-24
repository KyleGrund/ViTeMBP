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

import com.vitembp.services.sensors.AccelerometerThreeAxis;
import com.vitembp.services.sensors.DistanceSensor;
import com.vitembp.services.sensors.RotarySensor;
import com.vitembp.services.sensors.Sensor;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Builds overlay elements.
 */
class OverlayElementFactory {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Builds an overlay element.
     * @return The overlay element.
     */
    static OverlayElement buildElement(ElementDefinition definition, ElementLocation layoutLocation, List<Sensor> sensors, Map<Sensor, Double> minimumValues, Map<Sensor, Double> maximumValues, int upperLeftX, int upperLeftY, int lowerRightX, int lowerRightY) throws InstantiationException {
        // this checks that all sensors described in the definition are
        // in the list of sensors
        if (!definition.getSensors().stream()
                        .map((def) -> def.getName())
                        .allMatch((name) -> sensors.stream()
                                .map(sensor -> sensor.getName())
                                .filter(sensorName -> name.equals(sensorName))
                                .count() == 1)) {
            LOGGER.error(
                    "Could not create sensor overlay for: " +
                    definition.getElementType().name() +
                    " - " +
                    definition.getLocation().name());
            return null;
        }
        switch (definition.getElementType()) {
            case BrakeSensor:
                // only uses a single sensor
                if (definition.getSensors().size() != 2) {
                    throw new InstantiationException("Brake sensor element type requires two sensor definitions.");
                }
                
                // get the sensor definitions
                SensorDefinition left = definition.getSensors().stream()
                        .filter(d -> d.getLocation() == ElementLocation.Left)
                        .findFirst()
                        .get();
                SensorDefinition right = definition.getSensors().stream()
                        .filter(d -> d.getLocation() == ElementLocation.Right)
                        .findFirst()
                        .get();
                
                // retrieve the sensor by name
                Sensor leftBinding = sensors.stream()
                                .filter((s) -> s.getName().equals(left.getName()))
                                .findFirst()
                                .get();
                Sensor rightBinding = sensors.stream()
                                .filter((s) -> s.getName().equals(right.getName()))
                                .findFirst()
                                .get();
                
                // make sure the sensor is the proper type
                if (!(leftBinding instanceof RotarySensor) || !(rightBinding instanceof RotarySensor)) {
                    throw new InstantiationException("Brake sensor element requires rotary sensor type.");
                }
                
                // get minimum/maximum values
                if (!minimumValues.containsKey(leftBinding) || !maximumValues.containsKey(leftBinding) ||
                        !minimumValues.containsKey(rightBinding) || !maximumValues.containsKey(rightBinding)) {
                    throw new InstantiationException("Brake sensors must have both minimum and maximum values defined.");
                }
                
                // build and return the overlay
                return new BrakeSensorOverlayElement(
                        upperLeftX,
                        upperLeftY,
                        lowerRightX,
                        lowerRightY,
                        layoutLocation,
                        minimumValues.get(leftBinding),
                        maximumValues.get(leftBinding),
                        (RotarySensor)leftBinding,
                        minimumValues.get(rightBinding),
                        maximumValues.get(rightBinding),
                        (RotarySensor)rightBinding);
            case Shock:
                // only uses a single sensor
                if (definition.getSensors().size() != 1) {
                    throw new InstantiationException("Shock sensor element type requires a single sensor definition.");
                }
                
                // retrieve the sensor by name
                Sensor shockBinding = sensors.stream()
                                .filter((s) -> s.getName().equals(definition.getSensors().get(0).getName()))
                                .findFirst()
                                .get();
                
                // make sure the sensor is the proper type
                if (!(shockBinding instanceof DistanceSensor)) {
                    throw new InstantiationException("Shock sensor element requires a distance sensor type.");
                }
                
                // get minimum/maximum values
                if (!minimumValues.containsKey(shockBinding) || !maximumValues.containsKey(shockBinding)) {
                    throw new InstantiationException("Shock sensor must have both minimum and maximum values defined.");
                }
                
                // build and return the overlay
                return new ShockSensorOverlayElement(
                        upperLeftX,
                        upperLeftY,
                        lowerRightX,
                        lowerRightY,
                        layoutLocation,
                        minimumValues.get(shockBinding),
                        maximumValues.get(shockBinding),
                        (DistanceSensor)shockBinding);
            case ThreeAxisG:
                // only uses a single sensor
                if (definition.getSensors().size() != 1) {
                    throw new InstantiationException("3-axis accelerometer sensor element type requires a single sensor definition.");
                }
                
                // retrieve the sensor by name
                Sensor accelBinding = sensors.stream()
                                .filter((s) -> s.getName().equals(definition.getSensors().get(0).getName()))
                                .findFirst()
                                .get();
                
                // make sure the sensor is the proper type
                if (!(accelBinding instanceof AccelerometerThreeAxis)) {
                    throw new InstantiationException("3-axis accelerometer sensor element requires a distance sensor type.");
                }
                
                // get minimum/maximum values
                if (!minimumValues.containsKey(accelBinding) || !maximumValues.containsKey(accelBinding)) {
                    throw new InstantiationException("3-axis accelerometer sensor must have both minimum and maximum values defined.");
                }
                
                // build and return the overlay
                return new ThreeAxisGOverlayElement(
                        upperLeftX,
                        upperLeftY,
                        lowerRightX,
                        lowerRightY,
                        layoutLocation,
                        minimumValues.get(accelBinding),
                        maximumValues.get(accelBinding),
                        (AccelerometerThreeAxis)accelBinding);
        }
        
        throw new InstantiationException("Unknown sensor type.");
    }
}
