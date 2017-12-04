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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;

/**
 * A factory for Platform instances.
 */
class PlatformFactory {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * Builds an appropriate Platform object for the supplied SystemBoard.
     * @param board The SystemBoard to build a platform for.
     * @return The Platform object for the System board.
     */
    public static Platform build(SystemBoard board) {
        try {
            // reflect on this class to find a create method that takes the board type
            Method buildMethod = PlatformFactory.class.getMethod("create", board.getClass());
            
            
            // check that the return type is a Platform object
            if (Platform.class.isAssignableFrom(buildMethod.getReturnType())) {            
                // build the platform using the method found
                try {
                    LOGGER.info("Building Platform for: " + board.getClass().getTypeName() + ".");
                    return (Platform)buildMethod.invoke(null, board);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    LOGGER.error("Exception invoking the create Platform method for SystemBoard type: " + board.getClass().getTypeName() + ".", ex);
                }
            } else {
                LOGGER.error("The return type for the create function for the SystemBoard type: " + board.getClass().getTypeName() + " is not a Platform.");
            }
        } catch (NoSuchMethodException ex) {
            LOGGER.error("Could not find a Platform creator for SystemBoard type: " + board.getClass().getTypeName() + ".", ex);
        } catch (SecurityException ex) {
            LOGGER.error("Could not reflecct on SystemBoard to create Platform for type: " + board.getClass().getTypeName() + ".", ex);
        }
        
        throw new IllegalArgumentException("Could not build a Platoform for: " + board.getClass().getTypeName() + ".");
    }
    
    /**
     * Builds a Platform for a SystemBoardMock instance.
     * @param board The system board to build a Platform for.
     * @return A Platform for a SystemBoardMock instance.
     */
    public static Platform create(SystemBoardMock board){
        return new PlatformFunctor(
                (Boolean t) -> {
                    if (t) {
                        LOGGER.info("Enabled synchronization light.");
                    } else {
                        LOGGER.info("Disabled synchronization light.");
                    }   
                },
                (Boolean t) -> {
                    if (t) {
                        LOGGER.info("Enabled buzzer.");
                    } else {
                        LOGGER.info("Disabled buzzer.");
                    }   
                },
                (Consumer<Character> cb) -> {
                    LOGGER.info("Set keypad callback.");
                },
                () -> {
                    Set<Sensor> toReturn = new HashSet<>();
                    toReturn.add(new AccelerometerMock());
                    return toReturn;
                },
                () -> Paths.get("/com/vitembp/embedded/configuration/DefaultConfigMock.xml"),
                () -> { LOGGER.info("Initialized mock platform."); },
                (String iface, Integer metric) -> {
                    LOGGER.info("Set interface \"" + iface + "\" metric to " + Integer.toString(metric) + ".");
                });
    }
    
    /**
     * Builds a Platform for a SystemBoardUdooNeo instance.
     * @param board The system board to build a Platform for.
     * @return A Platform for a SystemBoardUdooNeo instance.
     */
    public static Platform create(SystemBoardUdooNeo board){
        // get gpio182 which controls the sync light
        final GPIOPort lightPort = getGPIOPortByName(board, "gpio182");
        
        // get gpio4 which controls the buzzer
        final GPIOPort buzzerPort = getGPIOPortByName(board, "gpio4");
        
        // get gpio106 which controls button 1
        final GPIOPort buttonOne = getGPIOPortByName(board, "gpio106");
        
        // get gpio107 which controls button 2
        final GPIOPort buttonTwo = getGPIOPortByName(board, "gpio107");
        
        // get gpio180 which controls button 3
        final GPIOPort buttonThree = getGPIOPortByName(board, "gpio180");
        
        // get gpio181 which controls button 4
        final GPIOPort buttonFour = getGPIOPortByName(board, "gpio181");
        
        return new PlatformFunctor(
                lightPort::setValue,
                buzzerPort::setValue,
                (Consumer<Character> cb) -> {
                    // add polled event listeners to supply button press events
                    startGPIOEvent(cb, buttonOne, '1');
                    startGPIOEvent(cb, buttonFour, '4');
                },
                () -> {
                    Set<Sensor> toReturn = new HashSet<>();
                    
                    // use factory to build all devices
                    board.getI2CBusses().forEach((bus) -> {
                        toReturn.addAll(I2CSensorFactory.getI2CSensors(bus));
                    });
                    
                    board.getSerialBusses().forEach((bus) -> {
                        try { 
                            toReturn.addAll(SerialBusSensorFactory.getSerialSensors(bus));
                        } catch (IOException ex) {
                            LOGGER.error("Error building sensor for: " + bus.getName(), ex);
                        }
                    });
                    
                    return toReturn;
                },
                () -> Paths.get("/com/vitembp/embedded/configuration/DefaultConfigUdooNeo.xml"),
                () -> { 
                    try {
                        lightPort.setDirection(GPIODirection.Output);
                        lightPort.setValue(false);
                    } catch (IOException ex) {
                        LOGGER.error ("Could not initialize sync light port.", ex);
                    }
                    
                    try {
                        buzzerPort.setDirection(GPIODirection.Output);
                        buzzerPort.setValue(false);
                    } catch (IOException ex) {
                        LOGGER.error ("Could not initialize sync buzzer port.", ex);
                    }
                },
                IfmetricInterface::setInterfaceMetric);
    }
    
    /**
     * Builds a Platform for a SystemBoardUdooNeo instance.
     * @param board The system board to build a Platform for.
     * @return A Platform for a SystemBoardUdooNeo instance.
     */
    public static Platform create(SystemBoardUdooQdl board){
        // get gpio182 which controls the sync light
        final GPIOPort lightPort = getGPIOPortByName(board, "gpio123");
        
        // get gpio133 which controls the buzzer
        final GPIOPort buzzerPort = getGPIOPortByName(board, "gpio133");
        
        // get gpio106 which controls button 1
        final GPIOPort buttonOne = getGPIOPortByName(board, "gpio124");
        
        // get gpio107 which controls button 2
        final GPIOPort buttonTwo = getGPIOPortByName(board, "gpio125");
        
        // get gpio180 which controls button 3
        final GPIOPort buttonThree = getGPIOPortByName(board, "gpio126");
        
        // get gpio181 which controls button 4
        final GPIOPort buttonFour = getGPIOPortByName(board, "gpio127");
        
        return new PlatformFunctor(
                lightPort::setValue,
                buzzerPort::setValue,
                (Consumer<Character> cb) -> {
                    // add polled event listeners to supply button press events
                    startGPIOEvent(cb, buttonOne, '1');
                    startGPIOEvent(cb, buttonFour, '4');
                },
                () -> {
                    Set<Sensor> toReturn = new HashSet<>();
                    
                    // use factory to build all devices
                    board.getI2CBusses().forEach((bus) -> {
                        toReturn.addAll(I2CSensorFactory.getI2CSensors(bus));
                    });
                    
                    board.getSerialBusses().forEach((bus) -> {
                        try { 
                            toReturn.addAll(SerialBusSensorFactory.getSerialSensors(bus));
                        } catch (IOException ex) {
                            LOGGER.error("Error building sensor for: " + bus.getName(), ex);
                        }
                    });
                    
                    return toReturn;
                },
                () -> Paths.get("/com/vitembp/embedded/configuration/DefaultConfigUdooNeo.xml"),
                () -> { 
                    try {
                        lightPort.setDirection(GPIODirection.Output);
                        lightPort.setValue(false);
                    } catch (IOException ex) {
                        LOGGER.error ("Could not initialize sync light port.", ex);
                    }
                    
                    try {
                        buzzerPort.setDirection(GPIODirection.Output);
                        buzzerPort.setValue(false);
                    } catch (IOException ex) {
                        LOGGER.error ("Could not initialize sync buzzer port.", ex);
                    }
                },
                IfmetricInterface::setInterfaceMetric);
    }
    
    /**
     * Gets a GPIO port by name from the system board.
     * @param board The system board to get port from.
     * @param name The name of the port.
     * @return The port for the given name.
     */
    private static GPIOPort getGPIOPortByName(SystemBoard board, String name) {
        return board.getGPIOPorts()
                .stream()
                .filter((p) -> p.getName().equals(name))
                .findFirst()
                .get();
    }
    
    /**
     * Creates and starts a GPIOPolledEvent for the port that calls the callback
     * with the character.
     * @param cb The callback to call.
     * @param port The GPIOPort to poll.
     * @param ch The character to return to the callback.
     */
    private static void startGPIOEvent(Consumer<Character> cb, GPIOPort port, char ch) {
        // add polled event listeners to supply button press events
        new GPIOPolledEvent(
                port,
                8,
                (Boolean released) -> {
                    if (!released) cb.accept(ch);
                }).start();
    }
    
    /**
     * Builds a Platform for a SystemBoardMock instance.
     * @param board The system board to build a Platform for.
     * @return A Platform for a SystemBoardMock instance.
     */
    public static Platform create(SystemBoardRPi3 board){
        return new PlatformFunctor(
                (Boolean t) -> {
                    if (t) {
                        LOGGER.info("Enabled synchronization light.");
                    } else {
                        LOGGER.info("Disabled synchronization light.");
                    }   
                },
                (Boolean t) -> {
                    if (t) {
                        LOGGER.info("Enabled buzzer.");
                    } else {
                        LOGGER.info("Disabled buzzer.");
                    }   
                },
                (Consumer<Character> cb) -> {
                    LOGGER.info("Set keypad callback.");
                },
                () -> {
                    Set<Sensor> toReturn = new HashSet<>();
                    
                    // use factory to build all devices for I2C and Serial
                    board.getI2CBusses().forEach((bus) -> {
                        toReturn.addAll(I2CSensorFactory.getI2CSensors(bus));
                    });
                    
                    board.getSerialBusses().forEach((bus) -> {
                        try { 
                            toReturn.addAll(SerialBusSensorFactory.getSerialSensors(bus));
                        } catch (IOException ex) {
                            LOGGER.error("Error building sensor for: " + bus.getName(), ex);
                        }
                    });
                    
                    return toReturn;
                },
                () -> Paths.get("/com/vitembp/embedded/configuration/DefaultConfigUdooNeo.xml"),
                () -> {},
                IfmetricInterface::setInterfaceMetric);
    }
}
