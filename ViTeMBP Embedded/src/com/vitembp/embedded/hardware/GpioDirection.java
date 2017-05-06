/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vitembp.embedded.hardware;

/**
 * An enumeration of the possible GPIO direction states.
 */
public enum GpioDirection {
    /**
     * The port will be set to an input.
     */
    Input,
    
    /**
     * The port will be set to an output.
     */
    Output,

    /**
     * The port will be tri-stated.
     */
    TriState
}
