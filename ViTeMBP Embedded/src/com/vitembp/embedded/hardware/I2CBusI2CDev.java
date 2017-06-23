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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A synchronized I2CBus implementation using the Linux i2c-dev interface.
 */
class I2CBusI2CDev extends I2CBusFunctor {
    /**
     * Class logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Initializes a new instance of the I2CBusI2CDev class.
     * @param busID The ID of the I2C bus being controlled.
     */
    public I2CBusI2CDev(final int busID) {
        // create functor by using lambda functions calling local synchronous
        // static functions with this particular bus' ID curried.
        super("i2c" + Integer.toString(busID),
                (transaction) -> I2CBusI2CDev.busCallback(busID, transaction),
                () -> I2CBusI2CDev.deviceIDCallback(busID));
    }
    
    /**
     * The callback providing access to the I2C bus.
     */
    private static synchronized byte[] busCallback(int busID, I2CBusTransaction transaction) {
        // use i2cget and i2cset to perform transaction
        return new byte[] { };
    }
    
    /**
     * The callback which will list enumerable I2C devices.
     */
    private static synchronized List<Integer> deviceIDCallback(int busID) {
        // list of I2C devices on the bus that have been enumerated
        ArrayList<Integer> foundDevices = new ArrayList<>();
        
        // catch any IOExceptions occuring during the execution of the command
        try {
            // run command: "i2cdetect -y I2CBUS"
            // build the FFmpeg process that will extract the frames
            ProcessBuilder pb = new ProcessBuilder(
                    "i2cdetect",
                    "-y",
                    Integer.toString(busID));
            
            LOGGER.info("Executing command: " + Arrays.toString(pb.command().toArray()));
            
            // execute the command
            Process proc = pb.start();
            
            try {
                // execute and wait for the command
                int result = proc.waitFor();
                if (result != 0) {
                    // result is exit level, log anything > 0 as an error
                    LOGGER.error("The i2cdetect command exited with level: " + Integer.toString(result));
                    BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                    String line = br.readLine();
                    while (line != null) {
                        LOGGER.error(line);
                        line = br.readLine();
                    }
                }
            } catch (InterruptedException ex) {
                LOGGER.error("Interrupted while waiting for i2cdetect completion.", ex);
            }
            
            // parse output
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            
            // read past header line
            br.readLine();
            
            // read in and parse data lines for address 0x03 to 0x77
            for (int addr = 0; addr <= 7; addr++) {
                foundDevices.addAll(parseDetectLine(Integer.toString(addr) + "0", br.readLine()));
            }

        } catch (IOException ex) {
            LOGGER.error("Unexpected IO exception while enumerating I2C devices on bus: i2c" + Integer.toString(busID) + ".", ex);
        }
        
        return foundDevices;
    }
    
    /**
     * Parses a line from the output of the i2cdetect program.
     * @param address The base address the line is describing.
     * @param line The line of output to process.
     * @return A list of addresses where a device was found.
     * @throws IOException If there is a parse error.
     */
    private static List<Integer> parseDetectLine(String address, String line) throws IOException {
        // this funciton parses one line of output from the i2cdetect program
        // example output from the i2cdetect program:
        // "     0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f"
        // "00:          -- -- -- -- -- -- -- -- -- -- -- -- --"
        // "10: -- -- -- -- -- -- -- -- -- -- -- -- -- -- UU --"
        // "20: UU -- -- -- -- -- -- -- -- -- -- -- -- -- -- --"
        // "30: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --"
        // "40: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --"
        // "50: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --"
        // "60: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --"
        // "70: -- -- -- -- -- -- -- --"
        
        // check line prefix is correct
        if (!line.startsWith(address + ": ")) {
            throw new IOException("Line did not start with prefix: \"" + address + ": \".");
        }
        
        // the bus device address base for this line as determined by the header
        int baseAddress = 16 * Integer.getInteger(address.substring(0, 1));
        
        // list of addresses with a device
        List<Integer> found = new ArrayList<>();
        
        // there are 16 possible devices for each base address
        for (int addr = 0; addr < 16; addr++) {
            // calculate starting char for the device in the output
            int offset = 4 + (3 * addr);
            
            // if we haven't read past the end of the line (which should happen
            // in addresses 0x78 and above)
            if (line.length() >= offset + 2) {
                // get two char status of the address
                String res = line.substring(offset, offset + 2);

                // if the status is not "--" or "  " a device is present
                if (!(res.contentEquals("--") || res.contentEquals("  "))) {
                    found.add(baseAddress + addr);
                }
            }
        }
        
        return found;
    }
    
    static Set<I2CBus> buildBusesForPath(Path busDir) throws IOException {
        Set<I2CBus> toReturn = new HashSet<>();
        
        // make sure ports is a directory
        if (!Files.isDirectory(busDir)) {
            LOGGER.error("Cannot build I2C busses for a path which is not a directory.");
            throw new IOException("The busDir parameter must be a directory.");
        }
        
        // enumerate all paths from directory of format i2c-###
        Stream<Integer> toBuild = Files.list(busDir)
                .filter((p) -> p.getFileName().toString().startsWith("i2c-"))
                .map((p) -> p.getFileName().toString().substring(4))
                .map((num) -> Integer.parseUnsignedInt(num));
                    
            
        // build GPIOPortFiles for matching files
        toBuild.forEach((dir) -> {
            toReturn.add(new I2CBusI2CDev(dir));
        });
        
        return toReturn;
    }
}
