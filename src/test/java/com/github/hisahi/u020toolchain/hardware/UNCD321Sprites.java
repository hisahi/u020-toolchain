
package com.github.hisahi.u020toolchain.hardware; 

public class UNCD321Sprites {
    static void enableSprite(UNCD321 disp, int sprite) {
        disp.cpu.getMemory().array()[disp.memsprite + sprite * 16] = 0xFFFF;
    }
    static void disableSprite(UNCD321 disp, int sprite) {
        disp.cpu.getMemory().array()[disp.memsprite + sprite * 16] = 0x0000;
    }
    static void setSpriteGraphicsAddress(UNCD321 disp, int sprite, int addr) {
        disp.cpu.getMemory().array()[disp.memsprite + sprite * 16 + 1] = addr;
    }
    static void setSpriteX(UNCD321 disp, int sprite, int x) {
        disp.cpu.getMemory().array()[disp.memsprite + sprite * 16 + 4] = x;
    }
    static void setSpriteY(UNCD321 disp, int sprite, int y) {
        disp.cpu.getMemory().array()[disp.memsprite + sprite * 16 + 5] = y;
    }
    static void setHFlip(UNCD321 disp, int sprite, boolean state) {
        setFlag(disp, sprite, 8, state);
    }
    static void setVFlip(UNCD321 disp, int sprite, boolean state) {
        setFlag(disp, sprite, 4, state);
    }
    static void setDoubleWidth(UNCD321 disp, int sprite, boolean state) {
        setFlag(disp, sprite, 2, state);
    }
    static void setDoubleHeight(UNCD321 disp, int sprite, boolean state) {
        setFlag(disp, sprite, 1, state);
    }
    static void setLowPriority(UNCD321 disp, int sprite, boolean state) {
        setFlag(disp, sprite, 32, state);
    }
    static boolean getBGCollision(UNCD321 disp, int sprite) {
        int[] mem = disp.cpu.getMemory().array();
        return (mem[disp.memsprite + sprite * 16 + 7] & 2) != 0;
    }
    static boolean getSpriteCollision(UNCD321 disp, int sprite) {
        int[] mem = disp.cpu.getMemory().array();
        return (mem[disp.memsprite + sprite * 16 + 7] & 1) != 0;
    }
    static void setSpritePalette(UNCD321 disp, int sprite, int[] palette) {
        int[] mem = disp.cpu.getMemory().array();
        int palsz = palette.length < 8 ? palette.length : 8;
        for (int i = 0; i < palsz; ++i) {
            mem[disp.memsprite + sprite * 16 + i + 8] = palette[i];
        }
    }
    private static void setFlag(UNCD321 disp, int sprite, int i, boolean flag) {
        int[] mem = disp.cpu.getMemory().array();
        if (flag) {
            mem[disp.memsprite + sprite * 16 + 6] |= i;
        } else {
            mem[disp.memsprite + sprite * 16 + 6] &= ~i;
        }
    }
}
