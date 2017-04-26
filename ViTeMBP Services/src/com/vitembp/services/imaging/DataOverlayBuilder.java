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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 * This class provides the necessary functions to create video frame data
 * overlays.
 */
public class DataOverlayBuilder {
    /**
     * The image which is being created
     */
    private final BufferedImage frame;
    
    /**
     * Creates a blank overlay with the specified width and height.
     * @param width The width of the overlay to create in pixels.
     * @param height The height of the overlay to create in pixels.
     */
    public DataOverlayBuilder(int width, int height) {
        // create the blank images
        this.frame = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = this.frame.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fill3DRect(0, 0, width, height, true);
        
        // set all pixels to alpha color
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                this.frame.setRGB(x, y, 0x0);
            }   
        }
    }
    
    /**
     * Creates an overlay from the specified image.
     * @param image The image to load 
     * @throws java.io.IOException If there is an I/O exception loading the
     * image.
     */
    public DataOverlayBuilder(Path image) throws IOException {
        // load the bitmap data
        this.frame = ImageIO.read(image.toFile());
    }
    
    /**
     * Adds text to the overlay at the specified location.
     * @param str The text to add.
     * @param x The horizontal position of the text from the left side.
     * @param y The vertical position of the text from the top.
     */
    public void addText(String str, int x, int y) {
        Graphics2D graphics = frame.createGraphics();
        graphics.setColor(Color.white);
        
        // resize the font to 20pt, same as 20 pixels
        Font currentFont = graphics.getFont();
        graphics.setFont(currentFont.deriveFont(20.0f));
        
        // draw the text
        graphics.drawString(str, x, y);
    }
    
    /**
     * Adds a vertical progress bar to the overlay.
     * @param progressScaleFactor A float indicating how much progress to
     * display, with 1.0 being complete and 0 indicating 0% complete.
     * @param topLeftX The horizontal location of the top left of the bar in
     * pixels from the left side.
     * @param topLeftY The vertical location of the top left of the bar in
     * pixels from the top side.
     * @param lowerRightX The horizontal location of the bottom right of the bar
     * in pixels from the left side.
     * @param lowerRightY The vertical location of the bottom right of the bar
     * in pixels from the top side.
     */
    public void addVerticalProgressBar(float progressScaleFactor, int topLeftX, int topLeftY, int lowerRightX, int lowerRightY) {
        int height = this.frame.getHeight();
        int width = this.frame.getWidth();
        
        // check points are within the frame
        if (topLeftY > height || topLeftY < 0) {
            throw new IllegalArgumentException("Argument topLeftY is out of range of the frame buffer.");
        }
        
        if (topLeftX < 0 || topLeftX > width) {
            throw new IllegalArgumentException("Argument topLeftX is out of range of the frame buffer.");
        }
        
        if (lowerRightY > height || lowerRightY < 0) {
            throw new IllegalArgumentException("Argument lowerRightY is out of range of the frame buffer.");
        }
        
        if (lowerRightX < 0 || lowerRightX > width) {
            throw new IllegalArgumentException("Argument lowerRightX is out of range of the frame buffer.");
        }
        
        // check the upper left is less than right and upper is above lower
        if (topLeftY >= lowerRightY || topLeftX >= lowerRightX) {
            throw new IllegalArgumentException("The top left point vlues must be less than the lower right point values.");
        }
        
        // check bar graph percentace
        if (progressScaleFactor > 1.0 || progressScaleFactor < 0.0) {
            throw new IllegalArgumentException("The percentage must be between 0 and 1.");
        }
        
        // draw bar
        Graphics2D graphics = this.frame.createGraphics();
        graphics.setColor(Color.white);
        graphics.setStroke(new BasicStroke(2));
        graphics.drawRect(topLeftX, topLeftY, lowerRightX - topLeftX, lowerRightY - topLeftY);
        
        int progressHeight = (int)(((lowerRightY - topLeftY) - 2) * progressScaleFactor);
        int progressTopLeftY = lowerRightY - progressHeight;
        
        graphics.setColor(Color.white);
        graphics.fillRect(topLeftX + 2, progressTopLeftY, (lowerRightX - topLeftX) - 4, progressHeight - 2);
    }
    
    /**
     * Saves the overlay image to a PNG.
     * @param destination The location where the file should be saved.
     * @throws IOException If an exception occurs while saving.
     */
    public void saveImage(File destination) throws IOException {
        // retrieve image
        ImageIO.write(this.frame, "png", destination);
    }
}
