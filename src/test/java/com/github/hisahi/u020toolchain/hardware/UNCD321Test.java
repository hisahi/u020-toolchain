
package com.github.hisahi.u020toolchain.hardware;

import com.github.hisahi.u020toolchain.cpu.StandardMemory;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import java.util.Arrays;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class UNCD321Test {
    UNCD321 disp;
    DummyPixelWriter ipw;
    
    public UNCD321Test() {
        ipw = new DummyPixelWriter();
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @Before
    public void setUp() {
        clear();
        disp = new UNCD321(new UCPU16(new StandardMemory()), null);
    }
    
    private void clear() {
        ipw.clear();
    }
    
    private void draw() {
        disp.displayFrame(ipw, null);
    }
    
    private int hwi(int a, int b) {
        disp.cpu.writeRegister(UCPU16.REG_A, a);
        disp.cpu.writeRegister(UCPU16.REG_B, b);
        disp.hwi(disp.cpu);
        return disp.cpu.readRegister(UCPU16.REG_C);
    }
    
    @Test
    public void testMode0() {
        hwi(6, 0); // mode 0
        hwi(0, 0x2000); // VRAM 0x2000
        int[] memarr = disp.cpu.getMemory().array();
        for (int i = 0x2000; i < 0x2000 + 608; ++i) {
            memarr[i] = 0x0000;
        }
        memarr[0x2000] = 0xf007; // small dot character in top-left corner
        draw();
        for (int y = 1; y < 11; ++y) {
            for (int x = 0; x < 8; ++x) {
                boolean expectedOn = ((x == 3) && (y == 5)) 
                        || ((x == 3) && (y == 4)) 
                        || ((x == 3) && (y == 6)) 
                        || ((x == 2) && (y == 5)) 
                        || ((x == 4) && (y == 5));
                assertEquals("(" + x + "," + y + ")", expectedOn ? 0xFFFFFFFF : 0xFF000000, ipw.getPixel(x, y));
            }
        }
    }
    
    @Test
    public void testMode0CustomFont() {
        hwi(6, 0); // mode 0
        hwi(0, 0x2000); // VRAM 0x2000
        int[] memarr = disp.cpu.getMemory().array();
        for (int i = 0x2000; i < 0x2000 + 608; ++i) {
            memarr[i] = 0x0000;
        }
        hwi(4, 0x4000); // dump font to 0x4000
        memarr[0x4080] = 0x0000;
        memarr[0x4180] = 0xFFFF;
        memarr[0x4280] = 0xFFFF;
        memarr[0x4380] = 0xFFFF;
        memarr[0x4480] = 0xFFFF;
        hwi(1, 0x4000); // copy font from 0x4000
        memarr[0x2000] = 0xf080; // character 0x80 in top-left corner
        draw();
        for (int y = 1; y < 11; ++y) {
            for (int x = 0; x < 8; ++x) {
                boolean expectedOn = y >= 3;
                assertEquals("(" + x + "," + y + ")", expectedOn ? 0xFFFFFFFF : 0xFF000000, ipw.getPixel(x, y));
            }
        }
    }
    
    @Test
    public void testMode0CustomPalette() {
        hwi(6, 0); // mode 0
        hwi(0, 0x2000); // VRAM 0x2000
        int[] memarr = disp.cpu.getMemory().array();
        for (int i = 0x2000; i < 0x2000 + 608; ++i) {
            memarr[i] = 0x1100;
        }
        hwi(5, 0x4000); // dump palette to 0x4000
        memarr[0x4001] = 0x0A0A;
        hwi(2, 0x4000); // copy palette from 0x4000
        draw();
        assertEquals(0xFFAA00AA, ipw.getPixel(1, 1));
    }
    
    @Test
    public void testMode0BlinkingCursor() throws InterruptedException {
        hwi(6, 0); // mode 0
        hwi(0, 0x2000); // VRAM 0x2000
        hwi(8, 0x0000); // cursor top-left
        int[] memarr = disp.cpu.getMemory().array();
        for (int i = 0x2000; i < 0x2000 + 608; ++i) {
            memarr[i] = 0xF000;
        }
        draw();
        assertEquals("cursor should be off", 0xFF000000, ipw.getPixel(1, 1));
        Thread.sleep(600);
        draw();
        assertEquals("cursor should be on", 0xFFFFFFFF, ipw.getPixel(1, 1));
    }
    
    @Test
    public void testMode0Copy() throws InterruptedException {
        hwi(6, 0); // mode 0
        hwi(0, 0x2000); // VRAM 0x2000
        int[] memarr = disp.cpu.getMemory().array();
        for (int i = 0x2000; i < 0x2000 + 608; ++i) {
            memarr[i] = 0x0000;
        }
        String test = "Hello, World!";
        for (int i = 0; i < test.length(); ++i) {
            memarr[0x2000 + i] = test.charAt(i) | 0xF000;
        }
        assertEquals(test, disp.copyScreenBuffer().trim());
    }
    
    @Test
    public void testMode1() throws InterruptedException {
        hwi(6, 1); // mode 1
        hwi(0, 0x2000); // VRAM 0x2000
        int[] memarr = disp.cpu.getMemory().array();
        for (int i = 0x2000; i < 0x2000 + 3072; ++i) {
            memarr[i] = 0x0000;
        }
        memarr[0x2000] = 0xAAAA;
        draw();
        for (int x = 0; x < 16; ++x) {
            assertEquals("(" + x + ",0)", (x & 1) == 0 ? 0xFF0000AA : 0xFF000000, ipw.getPixel(x, 0));
        }
    }
    
    @Test
    public void testMode2() throws InterruptedException {
        hwi(6, 2); // mode 2
        hwi(0, 0x2000); // VRAM 0x2000
        int[] memarr = disp.cpu.getMemory().array();
        for (int i = 0x2000; i < 0x2000 + 2 * 3072; ++i) {
            memarr[i] = 0x0000;
        }
        memarr[0x2000] = 0xAAAA;
        memarr[0x2000 + 3072] = 0xCCCC;
        draw();
        int[] pal = new int[] {0xFF000000, 0xFF0000AA, 0xFF00AA00, 0xFF00AAAA};
        for (int x = 0; x < 16; ++x) {
            assertEquals("(" + x + ",0)", pal[3 - (x & 3)], ipw.getPixel(x, 0));
        }
    }
    
    @Test
    public void testMode3() throws InterruptedException {
        hwi(6, 3); // mode 3
        hwi(0, 0x2000); // VRAM 0x2000
        int[] memarr = disp.cpu.getMemory().array();
        for (int i = 0x2000; i < 0x2000 + 4 * 3072; ++i) {
            memarr[i] = 0x0000;
        }
        memarr[0x2000] = 0xAAAA;
        memarr[0x2000 + 3072] = 0xCCCC;
        memarr[0x2000 + 6144] = 0xF0F0;
        memarr[0x2000 + 9216] = 0xFF00;
        draw();
        int[] pal = new int[] {0xFF000000, 0xFF0000AA, 0xFF00AA00, 0xFF00AAAA,
                    0xFFAA0000, 0xFFAA00AA, 0xFFAA5500, 0xFFAAAAAA,
                    0xFF555555, 0xFF5555FF, 0xFF55FF55, 0xFF55FFFF,
                    0xFFFF5555, 0xFFFF55FF, 0xFFFFFF55, 0xFFFFFFFF};
        for (int x = 0; x < 16; ++x) {
            assertEquals("(" + x + ",0)", pal[15 - x], ipw.getPixel(x, 0));
        }
    }
    
    // using a helper class from now on
    public void setupSprites() {
        hwi(6, 0); // mode 0
        hwi(0, 0x2000); // VRAM
        hwi(10, 0xa000); // sprite memory
        int[] memarr = disp.cpu.getMemory().array();
        for (int i = 0x2000; i < 0x2000 + 608; ++i) {
            memarr[i] = 0xF000;
        }
        for (int i = 0x8000; i < 0x8000 + 16; ++i) {
            memarr[i] = 0xFFFF;
        }
        for (int i = 0xa000; i < 0xa000 + 256; ++i) {
            memarr[i] = 0;
        }
        UNCD321Sprites.enableSprite(disp, 0);
        UNCD321Sprites.setSpriteGraphicsAddress(disp, 0, 0x8000);
        UNCD321Sprites.setSpritePalette(disp, 0, new int[] {0x0000, 0x0555, 0x0005, 0x0500});
        UNCD321Sprites.setSpriteX(disp, 0, 40);
        UNCD321Sprites.setSpriteY(disp, 0, 50);
    }
    
    @Test
    public void testSprite() throws InterruptedException {
        setupSprites();
        draw();
        assertEquals(0xFF000000, ipw.getPixel(39, 49));
        assertEquals(0xFF000000, ipw.getPixel(39, 50));
        assertEquals(0xFF000000, ipw.getPixel(40, 49));
        assertEquals(0xFF555555, ipw.getPixel(40, 50));
        assertEquals(0xFF555555, ipw.getPixel(55, 65));
        assertEquals(0xFF000000, ipw.getPixel(56, 65));
        assertEquals(0xFF000000, ipw.getPixel(55, 66));
        assertEquals(0xFF000000, ipw.getPixel(56, 66));
    }
    
    @Test
    public void testSpriteDoubleWidth() throws InterruptedException {
        setupSprites();
        UNCD321Sprites.setDoubleWidth(disp, 0, true);
        draw();
        assertEquals(0xFF000000, ipw.getPixel(39, 50));
        assertEquals(0xFF555555, ipw.getPixel(40, 50));
        assertEquals(0xFF555555, ipw.getPixel(71, 50));
        assertEquals(0xFF000000, ipw.getPixel(72, 50));
    }
    
    @Test
    public void testSpriteDoubleHeight() throws InterruptedException {
        setupSprites();
        UNCD321Sprites.setDoubleHeight(disp, 0, true);
        draw();
        assertEquals(0xFF000000, ipw.getPixel(40, 49));
        assertEquals(0xFF555555, ipw.getPixel(40, 50));
        assertEquals(0xFF555555, ipw.getPixel(40, 81));
        assertEquals(0xFF000000, ipw.getPixel(40, 82));
    }
    
    @Test
    public void testSpriteNoHFlip() throws InterruptedException {
        int[] memarr = disp.cpu.getMemory().array();
        setupSprites();
        for (int i = 0x8000; i < 0x8000 + 16; ++i) {
            memarr[i] = 0xF0F0;
        }
        UNCD321Sprites.setHFlip(disp, 0, false);
        draw();
        assertEquals(0xFF555555, ipw.getPixel(43, 50));
        assertEquals(0xFF000000, ipw.getPixel(44, 50));
    }
    
    @Test
    public void testSpriteHFlip() throws InterruptedException {
        int[] memarr = disp.cpu.getMemory().array();
        setupSprites();
        for (int i = 0x8000; i < 0x8000 + 16; ++i) {
            memarr[i] = 0xF0F0;
        }
        UNCD321Sprites.setHFlip(disp, 0, true);
        draw();
        assertEquals(0xFF000000, ipw.getPixel(43, 50));
        assertEquals(0xFF555555, ipw.getPixel(44, 50));
    }
    
    @Test
    public void testSpriteNoVFlip() throws InterruptedException {
        int[] memarr = disp.cpu.getMemory().array();
        setupSprites();
        for (int i = 0x8000; i < 0x8000 + 16; ++i) {
            memarr[i] = i == 0x8000 ? 0xFFFF : 0x0000;
        }
        UNCD321Sprites.setVFlip(disp, 0, false);
        draw();
        assertEquals(0xFF555555, ipw.getPixel(40, 50));
        assertEquals(0xFF000000, ipw.getPixel(40, 51));
    }
    
    @Test
    public void testSpriteVFlip() throws InterruptedException {
        int[] memarr = disp.cpu.getMemory().array();
        setupSprites();
        for (int i = 0x8000; i < 0x8000 + 16; ++i) {
            memarr[i] = i == 0x8000 ? 0xFFFF : 0x0000;
        }
        UNCD321Sprites.setVFlip(disp, 0, true);
        draw();
        assertEquals(0xFF000000, ipw.getPixel(40, 50));
        assertEquals(0xFF555555, ipw.getPixel(40, 65));
    }
    
    @Test
    public void testSpriteHighPriority() throws InterruptedException {
        setupSprites();
        disp.cpu.getMemory().array()[0x2000] = 0xF0DF;
        UNCD321Sprites.setSpriteX(disp, 0, 1);
        UNCD321Sprites.setSpriteY(disp, 0, 1);
        UNCD321Sprites.setLowPriority(disp, 0, false);
        draw();
        assertEquals(0xFF000000, ipw.getPixel(1, 0));
        assertEquals(0xFFFFFFFF, ipw.getPixel(0, 1));
        assertEquals(0xFF555555, ipw.getPixel(1, 1));
        assertEquals(0xFF555555, ipw.getPixel(8, 8));
    }
    
    @Test
    public void testSpriteLowPriority() throws InterruptedException {
        setupSprites();
        disp.cpu.getMemory().array()[0x2000] = 0xF0DF;
        UNCD321Sprites.setSpriteX(disp, 0, 1);
        UNCD321Sprites.setSpriteY(disp, 0, 1);
        UNCD321Sprites.setLowPriority(disp, 0, true);
        draw();
        assertEquals(0xFF000000, ipw.getPixel(1, 0));
        assertEquals(0xFFFFFFFF, ipw.getPixel(0, 1));
        assertEquals(0xFFFFFFFF, ipw.getPixel(1, 1));
        assertEquals(0xFF555555, ipw.getPixel(8, 8));
    }
    
    @Test
    public void testSpriteBGCollisionOff() throws InterruptedException {
        setupSprites();
        disp.cpu.getMemory().array()[0x2000] = 0x0000;
        UNCD321Sprites.setSpriteX(disp, 0, 1);
        UNCD321Sprites.setSpriteY(disp, 0, 1);
        UNCD321Sprites.setLowPriority(disp, 0, true);
        draw();
        assertFalse(UNCD321Sprites.getBGCollision(disp, 0));
    }
    
    @Test
    public void testSpriteBGCollisionOn() throws InterruptedException {
        setupSprites();
        disp.cpu.getMemory().array()[0x2000] = 0xF0DF;
        UNCD321Sprites.setSpriteX(disp, 0, 1);
        UNCD321Sprites.setSpriteY(disp, 0, 1);
        UNCD321Sprites.setLowPriority(disp, 0, true);
        draw();
        assertTrue(UNCD321Sprites.getBGCollision(disp, 0));
    }
    
    @Test
    public void testSpriteCollisionOff() throws InterruptedException {
        int[] memarr = disp.cpu.getMemory().array();
        setupSprites();
        for (int i = 1; i < 16; ++i) {
            memarr[0x8000 + i] = 0;
        }
        UNCD321Sprites.enableSprite(disp, 1);
        UNCD321Sprites.setSpriteGraphicsAddress(disp, 1, 0x8010);
        UNCD321Sprites.setSpritePalette(disp, 1, new int[] {0x0000, 0x0aaa, 0x000a, 0x0a00});
        UNCD321Sprites.setSpriteX(disp, 1, 40);
        UNCD321Sprites.setSpriteY(disp, 1, 50);
        for (int i = 0; i < 16; ++i) {
            memarr[0x8010 + i] = i == 1 ? 0xFFFF : 0x0000;
        }
        draw();
        assertFalse(UNCD321Sprites.getSpriteCollision(disp, 0));
        assertFalse(UNCD321Sprites.getSpriteCollision(disp, 1));
    }
    
    @Test
    public void testSpriteCollisionOn() throws InterruptedException {
        int[] memarr = disp.cpu.getMemory().array();
        setupSprites();
        UNCD321Sprites.enableSprite(disp, 1);
        UNCD321Sprites.setSpriteGraphicsAddress(disp, 1, 0x8010);
        UNCD321Sprites.setSpritePalette(disp, 1, new int[] {0x0000, 0x0aaa, 0x000a, 0x0a00});
        UNCD321Sprites.setSpriteX(disp, 1, 40);
        UNCD321Sprites.setSpriteY(disp, 1, 50);
        for (int i = 0; i < 16; ++i) {
            memarr[0x8010 + i] = 0xFFFF;
        }
        draw();
        assertTrue(UNCD321Sprites.getSpriteCollision(disp, 0));
        assertTrue(UNCD321Sprites.getSpriteCollision(disp, 1));
    }
    
    class DummyPixelWriter implements IPixelWriter {
        int[] pbuffer = new int[256*192];
        @Override
        public void clear() {
            Arrays.fill(pbuffer, 0xFF000000);
        }
        @Override
        public void write(int x, int y, int width, int height, int[] data, int stride) {
            pbuffer = Arrays.copyOf(data, data.length);
        }
        public int getPixel(int x, int y) {
            return pbuffer[y * 256 + x];
        }
    }
}
