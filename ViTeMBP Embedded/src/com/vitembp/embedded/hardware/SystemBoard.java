/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vitembp.embedded.hardware;

/**
 * Class providing an interface to embedded system boards.
 */
public abstract class SystemBoard {
    /**
     * Detects the current board the system is operating on and creates the
     * appropriate singleton instance.
     * @return The board instance for the system that the program is currently
     * executing on. If the system is executing on an unknown board a mock
     * simulation will be returned.
     */
    public static SystemBoard getBoard() {
        throw new UnsupportedOperationException();
    }
}
