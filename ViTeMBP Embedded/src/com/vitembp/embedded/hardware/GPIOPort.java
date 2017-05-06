/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vitembp.embedded.hardware;

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
     */
    public abstract GpioDirection getDirection();
    
    /**
     * Sets the direction of the GPIOPort port.
     * @param direction The direction to set the GPIOPort port to.
     */
    public abstract void setDirection(GpioDirection direction);
    
    /**
     * Returns the boolean value repres1enting the state of the port.
     * @return The boolean value repres1enting the state of the port.
     */
    public abstract boolean getValue();
    
    /**
     * Sets the state of the port.
     * @param value The boolean value repres1enting the state to set the port to.
     */
    public abstract void setValue(boolean value);
}
