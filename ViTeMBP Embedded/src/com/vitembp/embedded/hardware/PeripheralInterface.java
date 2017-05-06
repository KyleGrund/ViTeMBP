/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vitembp.embedded.hardware;

/**
 * This class provides an abstraction for peripheral interface board.
 */
public abstract class PeripheralInterface {
    /**
     * Gets an instance of the PeripheralInterface class for the given
     * SystemBoard.
     * @param board The SystemBoard interface to build the PeripheralInterface
     * instance for.
     * @return An instance of the PeripheralInterface class for the given
     * SystemBoard.
     */
    public static PeripheralInterface getInterface(SystemBoard board) {
        throw new UnsupportedOperationException();
    }
}
