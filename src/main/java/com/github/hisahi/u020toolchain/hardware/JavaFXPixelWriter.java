
package com.github.hisahi.u020toolchain.hardware; 

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

/**
 * An implementation of the IPixelWriter interface that operates on an
 * JavaFX Canvas. This is the primary implementation used by the
 * emulator UI.
 * 
 * @author hisahi
 */
public class JavaFXPixelWriter implements IPixelWriter {
    private final GraphicsContext ctx;
    private final PixelWriter pw;
    
    /**
     * Initializes a new JavaFXPixelWriter instance.
     * 
     * @param screen The Canvas to write the pixels to.
     */
    public JavaFXPixelWriter(Canvas screen) {
        this.ctx = screen.getGraphicsContext2D();
        this.pw = this.ctx.getPixelWriter();
    }

    @Override
    public void clear() {
        ctx.setFill(Color.BLACK);
        ctx.fillRect(0, 0, 256, 192);
    }

    @Override
    public void write(int x, int y, int width, int height, int[] data, int stride) {
        pw.setPixels(x, y, width, height, PixelFormat.getIntArgbInstance(), 
                data, 0, stride);
    }
}
