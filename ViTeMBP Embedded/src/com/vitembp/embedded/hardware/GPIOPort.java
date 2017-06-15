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
package com.vitembp.embedded.hardware;

import java.io.IOException;

/**
 * An abstract class providing a logtical inteface GPIOPort.
 */
public abstract class GPIOPort {
    /**
     * Gets the name of the GPIOPort port controlled by this object.
     * @return The name of the GPIOPort port controlled by this object.
     */
    public abstract String getName();
    
    /**
     * Gets the direction of the GPIOPort port.
     * @return The direction of the GPIOPort port.
     * @throws java.io.IOException If an exception occurs getting the port direction.
     */
    public abstract GpioDirection getDirection() throws IOException;
    
    /**
     * Sets the direction of the GPIOPort port.
     * @param direction The direction to set the GPIOPort port to.
     * @throws java.io.IOException If an exception occurs setting the port direction.
     */
    public abstract void setDirection(GpioDirection direction) throws IOException;
    
    /**
     * Returns the boolean value representing the state of the port.
     * @return The boolean value representing the state of the port.
     * @throws java.io.IOException If an exception occurs getting the port value.
     */
    public abstract boolean getValue() throws IOException;
    
    /**
     * Sets the state of the port.
     * @param value The boolean value representing the state to set the port to.
     * @throws java.io.IOException If an exception occurs setting the port value.
     */
    public abstract void setValue(boolean value) throws IOException;
}
