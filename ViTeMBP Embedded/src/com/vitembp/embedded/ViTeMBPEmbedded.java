/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vitembp.embedded;

import com.vitembp.embedded.hardware.Platform;

/**
 * Class containing the main entry point for program.
 */
public class ViTeMBPEmbedded {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // initialize platform
        Platform.getPlatform();
    }
    
}
