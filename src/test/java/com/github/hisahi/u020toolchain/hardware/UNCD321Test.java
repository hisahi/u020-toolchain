
package com.github.hisahi.u020toolchain.hardware;

import com.github.hisahi.u020toolchain.cpu.StandardMemory;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class UNCD321Test {
    static Canvas screen;
    static GraphicsContext ctx;
    static PixelWriter pw;
    UNCD321 disp;
    
    public UNCD321Test() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        screen = new Canvas(256, 192);
        ctx = screen.getGraphicsContext2D();
        pw = ctx.getPixelWriter();
    }
    
    @Before
    public void setUp() {
        clear();
        disp = new UNCD321(new UCPU16(new StandardMemory()), null);
    }
    
    private static void clear() {
        ctx.setFill(Color.BLACK);
        ctx.fillRect(0, 0, 256, 192);
    }
    
    private void draw() {
        disp.displayFrame(screen, ctx, pw, null);
    }

    // TODO
    /*
            test mode 0 (text mode)
                test custom font
                test custom palette
                test blinking cursor
                test copying all text from screen
            test mode 1
            test mode 2
            test mode 3
            test sprites
                test sprite priority
                test H-flip, V-flip
                test double width/height modes
                test BG/sprite collisions
                test sprite/sprite collision
        
    */
}
