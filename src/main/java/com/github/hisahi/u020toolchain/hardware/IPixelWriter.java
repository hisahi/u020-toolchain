
package com.github.hisahi.u020toolchain.hardware;

/**
 * A generic interface for a class that handles a virtual screen. 
 * They must implement clearing the screen and writing pixels into it.
 * 
 * @author hisahi
 */
public interface IPixelWriter {
    /**
     * Clears the screen.
     */
    public void clear();
    
    /**
     * Writes pixels into the screen.
     * 
     * @param x       The x coordinate of the top-left corner where to write.
     * @param y       The y coordinate of the top-left corner where to write.
     * @param width   The width of the pixel area written in pixels.
     * @param height  The height of the pixel area written in pixels.
     * @param data    The pixel data.
     * @param stride  How many elements of pixel data correspond to one row.
     */
    public void write(int x, int y, int width, int height, int[] data, int stride);
}
