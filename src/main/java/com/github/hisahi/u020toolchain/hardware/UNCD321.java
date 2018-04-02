
package com.github.hisahi.u020toolchain.hardware; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.instructions.Operations;
import com.github.hisahi.u020toolchain.ui.EmulatorMain;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;

public class UNCD321 extends Hardware {
    private boolean on;
    private int memscr;
    private int[] font;
    private int[] palette;
    private int mode;
    private boolean lem;
    private int memsprite;
    private int[] collisions;
    private int vsyncint;
    private int cursorpos;
    private long lastblink;
    private boolean cursoron;
    private int[] buffer;
    private EmulatorMain main;
    private UNCD321(UCPU16 cpu) {
        super(cpu);
        this.reset();
    }
    public UNCD321(UCPU16 cpu, EmulatorMain main) {
        this(cpu);
        this.main = main;
    }

    @Override
    public long hardwareId() {
        return 0xdb7b373eL;
    }

    @Override
    public int hardwareVersion() {
        return 0x321a;
    }

    @Override
    public long hardwareManufacturer() {
        return 0x2590a31c;
    }

    @Override
    public void hwi(UCPU16 cpu) {
        int b = cpu.readRegister(UCPU16.REG_B);
        switch (cpu.readRegister(UCPU16.REG_A)) {
            case 0: 
                this.memscr = b;
                this.on = b != 0;
                if (this.on) {
                    this.lastblink = System.currentTimeMillis();
                }
                break;
            case 1:
                if (this.lem) {
                    this.cpu.addCycles(256);
                    for (int c = 0; c < 128; ++c) {
                        int lo = cpu.getMemory().array()[(c * 2) & 0xFFFF];
                        int hi = cpu.getMemory().array()[(c * 2 + 1) & 0xFFFF];
                        boolean[] l = new boolean[16];
                        boolean[] h = new boolean[16];
                        for (int i = 0; i < 16; ++i) {
                            l[i] = (lo >> (15 - i)) != 0;
                            h[i] = (hi >> (15 - i)) != 0;
                        }
                        cpu.getMemory().array()[(c) & 0xFFFF] = 
                              lo8b(l[7], l[7], l[15], l[15], 
                                     h[7], h[7], h[15], h[15]);
                        cpu.getMemory().array()[(c + 256) & 0xFFFF] = 
                              hi8b(l[6], l[6], l[14], l[14], 
                                     h[6], h[6], h[14], h[14])
                            | lo8b(l[5], l[5], l[13], l[13], 
                                   h[5], h[5], h[13], h[13]);
                        cpu.getMemory().array()[(c + 512) & 0xFFFF] = 
                             (hi8b(l[4], l[4], l[12], l[12], 
                                    h[4], h[4], h[12], h[12])
                            | lo8b(l[3], l[3], l[11], l[11], 
                                   h[3], h[3], h[11], h[11]));
                        cpu.getMemory().array()[(c + 768) & 0xFFFF] = 
                             (hi8b(l[2], l[2], l[10], l[10], 
                                    h[2], h[2], h[10], h[10])
                            | lo8b(l[1], l[1], l[ 9], l[ 9], 
                                   h[1], h[1], h[ 9], h[ 9]));
                        cpu.getMemory().array()[(c + 1024) & 0xFFFF] = 
                             (hi8b(l[0], l[0], l[ 8], l[ 8], 
                                    h[0], h[0], h[ 8], h[ 8]));
                    }
                } else {
                    this.cpu.addCycles(1280);
                    for (int i = 0; i < 1280; ++i) {
                        font[i] = cpu.getMemory().array()[(b + i) & 0xFFFF];
                    }
                }
                break;
            case 2:
                this.cpu.addCycles(16);
                for (int i = 0; i < 16; ++i) {
                    palette[i] = cpu.getMemory().array()[(b + i) & 0xFFFF];
                }
                this.convertPalette();
                break;
            case 4:
                if (this.lem) {
                    this.cpu.addCycles(128);
                    for (int i = 0; i < 128; ++i) {
                        cpu.getMemory().array()[(b + i) & 0xFFFF] = 
                                            DEFAULT_FONT_LEM[i];
                    }
                } else {
                    this.cpu.addCycles(1280);
                    for (int i = 0; i < 1280; ++i) {
                        cpu.getMemory().array()[(b + i) & 0xFFFF] = 
                                            DEFAULT_FONT[i];
                    }
                }
                break;
            case 5:
                this.cpu.addCycles(16);
                for (int i = 0; i < 16; ++i) {
                    this.cpu.getMemory().write(b + i, DEFAULT_PALETTE[i]);
                }
                break;
            case 6:
                this.mode = b & 3;
                this.lem = (b & (1 << 15)) != 0;
                main.setCanCopy(this.mode == 0);
                break;
            case 7:
                this.cpu.writeRegister(UCPU16.REG_C, this.mode);
                break;
            case 8:
                this.cursorpos = b;
                if (this.cursorpos > 0x25f) {
                    this.cursorpos = 0xffff;
                }
                break;
            case 9:
                this.cpu.writeRegister(UCPU16.REG_C, this.cursorpos);
                break;
            case 10:
                this.memsprite = b;
                break;
            case 11:
                this.cpu.writeRegister(UCPU16.REG_C, this.memsprite);
                break;
            case 12:
                this.vsyncint = b;
                break;
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void reset() {
        this.on = this.lem = this.cursoron = false;
        this.memscr = this.mode = this.memsprite = this.vsyncint = 0;
        this.lastblink = Long.MIN_VALUE;
        this.cursorpos = 65535;
        this.font = Arrays.copyOf(this.DEFAULT_FONT, this.DEFAULT_FONT.length);
        this.palette = Arrays.copyOf(this.DEFAULT_PALETTE, 
                                    this.DEFAULT_PALETTE.length);
        this.collisions = new int[256 * 192];
        this.buffer = new int[256 * 192];
        this.convertPalette();
    }
    
    private void drawCharacterRow(int[] memarr, int bp, int chr, boolean blink, int pxp, int bgc, int fgc) {
        for (int x = 0; x < 8; ++x) {
            if (((chr & 0x8000) != 0) != blink) {
                if (fgc != 0) {
                    setCollision(memarr, pxp + x, -1);
                    buffer[bp + x] = fgc;
                }
            } else {
                if (blink) {
                    buffer[bp + x] = fgc;
                }
                if (bgc != 0) {
                    setCollision(memarr, pxp + x, -1);
                    buffer[bp + x] = bgc;
                }
            }
            chr <<= 1;
        }
    }

    public void displayFrame(long now, Canvas screen, GraphicsContext ctx, 
                            PixelWriter pw, EmulatorMain emu) {
        if (cpu.isPaused()) {
            lastblink = System.currentTimeMillis();
            return;
        }
        if (!this.on) {
            emu.clearScreen();
            return;
        }
        long msnow = System.currentTimeMillis();
        if ((msnow - lastblink) >= (lem ? 1000 : 500)) {
            lastblink = msnow;
            cursoron = !cursoron;
        }
        int[] memarr = this.cpu.getMemory().array();
        Arrays.fill(collisions, 0);
        Arrays.fill(buffer, palette[0]);
        if (memsprite > 0) {
            for (int s = 15; s >= 0; --s) {
                memarr[(this.memsprite + 16 * s + 7) & 0xFFFF] = 0;
            }
            for (int s = 15; s >= 0; --s) {
                if (memarr[(this.memsprite + 16 * s) & 0xFFFF] != 0) {
                    if (memarr[((this.memsprite + 16 * s + 6) & 0x20) & 0xFFFF] != 0) {
                        this.drawSprite(memarr, s);
                    }
                }
            }
        }
        if (mode == 0) {
            int ptr = 0;
            int base;
            boolean blink;
            int fg, bg, fgc, bgc, ap, bp, chr;
            for (int row = 1; row < 191; row += 10) {
                for (int col = 0; col < 256; col += 8) {
                    int wrd = memarr[(memscr + ptr) & 0xFFFF];
                    if (lem) {
                        blink = (wrd & 0x80) > 0;
                        base = (wrd & 0x7F);
                    } else {
                        blink = ptr == this.cursorpos;
                        base = (wrd & 0xFF);
                    }
                    ++ptr;
                    fg = ((wrd >> 12) & 0xF);
                    bg = ((wrd >> 8) & 0xF);
                    fgc = fg == 0 ? 0 : this.palette[fg];
                    bgc = bg == 0 ? 0 : this.palette[bg];
                    ap = (row << 8) + col;
                    for (int y = 0; y < 10; y += 2) {
                        bp = ap + (y << 8);
                        chr = this.font[base];
                        base += 0x100;
                        drawCharacterRow(memarr, bp, chr, blink && cursoron, ((row + y) << 8) + col, bgc, fgc);
                        drawCharacterRow(memarr, bp + 256, chr << 8, blink && cursoron, ((row + y) << 8) + col, bgc, fgc);
                    }
                }
            }
        } else if (mode == 1) {
            int ptr = 0;
            int pxl = 0;
            for (int cy = 0; cy < 192; ++cy) {
                for (int cx = 0; cx < 256; cx += 16) {
                    int wrd0 = memarr[(this.memscr + ptr++) & 0xFFFF];
                    for (int ix = cx; ix < cx + 16; ++ix) {
                        int ci = ((wrd0 & 0x8000) >> 15);
                        if (ci > 0) {
                            setCollision(memarr, (cy << 8) + cx + ix, -1);
                            buffer[pxl] = palette[ci];
                        }
                        wrd0 <<= 1;
                        ++pxl;
                    }
                }
            }
        } else if (mode == 2) {
            int ptr = 0;
            int pxl = 0;
            for (int cy = 0; cy < 192; ++cy) {
                for (int cx = 0; cx < 256; cx += 16) {
                    int wrd0 = memarr[(this.memscr + ptr) & 0xFFFF];
                    int wrd1 = memarr[(3072 + this.memscr + ptr++) & 0xFFFF];
                    for (int ix = cx; ix < cx + 16; ++ix) {
                        int ci = ((wrd0 & 0x8000) >> 15) 
                               | ((wrd1 & 0x8000) >> 14);
                        if (ci > 0) {
                            setCollision(memarr, (cy << 8) + cx + ix, -1);
                            buffer[pxl] = palette[ci];
                        }
                        wrd0 <<= 1;
                        wrd1 <<= 1;
                        ++pxl;
                    }
                }
            }
        } else if (mode == 3) {
            int ptr = 0;
            int pxl = 0;
            for (int cy = 0; cy < 192; ++cy) {
                for (int cx = 0; cx < 256; cx += 16) {
                    int wrd0 = memarr[(this.memscr + ptr) & 0xFFFF];
                    int wrd1 = memarr[(3072 + this.memscr + ptr) & 0xFFFF];
                    int wrd2 = memarr[(6144 + this.memscr + ptr) & 0xFFFF];
                    int wrd3 = memarr[(9216 + this.memscr + ptr++) & 0xFFFF];
                    for (int ix = cx; ix < cx + 16; ++ix) {
                        int ci = ((wrd0 & 0x8000) >> 15) 
                               | ((wrd1 & 0x8000) >> 14) 
                               | ((wrd2 & 0x8000) >> 13) 
                               | ((wrd3 & 0x8000) >> 12);
                        if (ci > 0) {
                            setCollision(memarr, (cy << 8) + cx + ix, -1);
                            buffer[pxl] = palette[ci];
                        }
                        wrd0 <<= 1;
                        wrd1 <<= 1;
                        wrd2 <<= 1;
                        wrd3 <<= 1;
                        ++pxl;
                    }
                }
            }
        }
        if (memsprite > 0) {
            for (int s = 15; s >= 0; --s) {
                if (memarr[(this.memsprite + 16 * s) & 0xFFFF] != 0) {
                    if (memarr[((this.memsprite + 16 * s + 6) & 0x20) & 0xFFFF] == 0) {
                        this.drawSprite(memarr, s);
                    }
                }
            }
        }
        pw.setPixels(0, 0, 256, 192, PixelFormat.getIntArgbInstance(), 
                buffer, 0, 256);
        if (this.vsyncint != 0) {
            this.cpu.queueInterrupt(this.vsyncint);
        }
    }
    
    private int lo8b(boolean b7, boolean b6, boolean b5, boolean b4, 
                        boolean b3, boolean b2, boolean b1, boolean b0) {
        return (b7 ? 128 : 0) | (b6 ? 64 : 0) | (b5 ? 32 : 0) | (b4 ? 16 : 0)
                | (b3 ? 8 : 0) | (b2 ? 4 : 0) | (b1 ? 2 : 0) | (b0 ? 1 : 0);
    }
    private int hi8b(boolean b15, boolean b14, boolean b13, boolean b12, 
                        boolean b11, boolean b10, boolean b9, boolean b8) {
        return lo8b(b15, b14, b13, b12, b11, b10, b9, b8) << 8;
    }
    
    public void setCollision(int[] memarr, int p, int d) {
        int base = this.memsprite - 9;
        if (d >= 0) {
            if (this.collisions[p] > 0) {
                memarr[base - 9 + 16 * this.collisions[p]] |= 1;
                memarr[base - 9 + 16 * d] |= 1;
            }
            if (this.collisions[p] < 0) {
                memarr[base - 9 + 16 * d] |= 2;
            }
        } else {
            if (this.collisions[p] > 0) {
                memarr[base + 16 * this.collisions[p]] |= 2;
            }
        }
        this.collisions[p] = d;
    }
    
    public void drawSprite(int[] memarr, int s) {
        int sp = this.memsprite + (s << 4);
        int cf = memarr[(sp + 6) & 0xFFFF];
        int ax = Operations.signExtend16To32(memarr[(sp + 4) & 0xFFFF]);
        int ay = Operations.signExtend16To32(memarr[(sp + 5) & 0xFFFF]);
        int ptr = memarr[(sp + 1) & 0xFFFF];
        int bp, x, y;
        int pln = Math.max(1, (cf >> 8) & 3);
        for (int oy = 0; oy < 16; ++oy) {
            if ((cf & 4) != 0) {
                y = 15 - oy;
            } else {
                y = oy;
            }
            if ((cf & 1) != 0) {
                y <<= 1;
            }
            y += ay;
            if (y < 0 || y >= 192) {
                ++ptr;
                continue;
            }
            int mask = 0x10000;
            for (int ox = 0; ox < 16; ++ox) {
                mask >>= 1;
                if ((cf & 8) != 0) {
                    x = 15 - ox;
                } else {
                    x = ox;
                }
                if ((cf & 2) != 0) {
                    x <<= 1;
                }
                x += ax;
                if (x < 0 || x >= 256) {
                    continue;
                }
                bp = (y << 8) + x;
                int c = 0;
                for (int p = 0; p < pln; ++p) {
                    c |= ((memarr[(ptr + (p << 4)) & 0xFFFF] & mask) != 0 
                            ? 1 : 0) << p;
                }
                if (c != 0) {
                    int sc = memarr[(sp + 8 + c) & 0xFFFF];
                    buffer[bp] = sc;
                    setCollision(memarr, bp >> 2, s + 1);
                    if ((cf & 2) != 0) {
                        buffer[bp + 1] = sc;
                        setCollision(memarr, (bp >> 2) + 1, s + 1);
                    }
                    if ((cf & 1) != 0) {
                        buffer[bp + 256] = sc;
                        setCollision(memarr, (bp >> 2) + 256, s + 1);
                    }
                    if ((cf & 1) != 0 && (cf & 2) != 0) {
                        buffer[bp + 257] = sc;
                        setCollision(memarr, (bp >> 2) + 257, s + 1);
                    }
                }
            } 
        }
    }
    
    @Override
    public void saveState(DataOutputStream stream) throws IOException {
        stream.writeInt(on ? 1 : 0);
        stream.writeInt(lem ? 1 : 0);
        stream.writeInt(memscr);
        stream.writeInt(mode);
        stream.writeInt(memsprite);
        stream.writeInt(vsyncint);
        stream.writeInt(cursorpos);
        for (int i = 0; i < font.length; ++i) {
            stream.writeInt(font[i]);
        }
        for (int i = 0; i < palette.length; ++i) {
            stream.writeInt(palette[i]);
        }
    }

    @Override
    public void restoreState(DataInputStream stream) throws IOException {
        on = stream.readInt() != 0;
        lem = stream.readInt() != 0;
        memscr = stream.readInt();
        mode = stream.readInt();
        memsprite = stream.readInt();
        vsyncint = stream.readInt();
        cursorpos = stream.readInt();
        this.font = Arrays.copyOf(this.DEFAULT_FONT, 
                                this.DEFAULT_FONT.length);
        this.palette = Arrays.copyOf(this.DEFAULT_PALETTE, 
                                this.DEFAULT_PALETTE.length);
        for (int i = 0; i < font.length; ++i) {
            font[i] = stream.readInt();
        }
        for (int i = 0; i < palette.length; ++i) {
            palette[i] = stream.readInt();
        }
        this.buffer = new int[256 * 192];
        this.lastblink = System.currentTimeMillis();
        this.convertPalette();
    }

    private void convertPalette() {
        for (int i = 0; i < 16; ++i) {
            int p = palette[i];
            palette[i] = 0xff000000 | ((((p >> 8) & 15) * 17) << 16)  
                                    | ((((p >> 4) & 15) * 17) << 8)  
                                    |   ((p       & 15) * 17);
        }
    }
    
    public String copyScreenBuffer() {
        if (!this.on || this.mode != 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int addr = memscr;
        for (int row = 0; row < 19; ++row) {
            StringBuilder sbrow = new StringBuilder();
            for (int col = 0; col < 32; ++col) {
                sbrow.append(CHARSET[cpu.getMemory().array()[addr] & 0xFF]);
                addr = (addr + 1) & 0xFFFF;
            }
            sb.append(trimRight(sbrow.toString()));
            sb.append("\n");
        }
        return trimRight(sb.toString());
    }
    
    private String trimRight(String str) {
        return ("." + str).trim().substring(1);
    }
    
    private static final int[] DEFAULT_FONT = new int[] {0, 31874, 31998, 0, 16, 56, 16, 0, 65535, 0, 65535, 14, 56, 16, 62, 0, 64, 8, 16, 108, 60, 56, 0, 16, 16, 16, 0, 0, 259, 32960, 0, 0, 0, 16, 40, 72, 16, 96, 48, 16, 8, 32, 16, 0, 0, 0, 0, 0, 56, 16, 56, 56, 8, 124, 56, 124, 56, 56, 0, 0, 0, 0, 0, 56, 48, 56, 120, 56, 112, 124, 124, 60, 68, 56, 28, 68, 64, 68, 68, 56, 120, 56, 120, 56, 124, 68, 68, 68, 68, 68, 124, 56, 0, 56, 16, 0, 32, 0, 64, 0, 4, 0, 24, 0, 64, 16, 16, 64, 16, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 0, 0, 24, 16, 48, 52, 0, 56, 40, 2064, 14404, 40, 8208, 4136, 0, 14404, 40, 8208, 40, 14404, 8208, 10240, 4136, 2064, 0, 60, 14404, 40, 8208, 14404, 8208, 40, 17464, 17408, 16, 24, 68, 258, 32832, 2064, 2064, 2064, 2064, 52, 13384, 24, 16, 16, 2064, 0, 16452, 16452, 16, 0, 0, 4420, 21930, 61115, 6168, 6168, 64, 0, 2160, 0, 13878, 13878, 0, 13878, 112, 20, 0, 6168, 6168, 0, 6168, 0, 6168, 60, 0, 13878, 0, 13878, 0, 13878, 0, 13878, 2064, 2064, 2064, 8208, 14404, 8208, 14404, 10240, 14404, 10240, 6168, 0, 14404, 8208, 14404, 3855, 65535, 0, 48, 13384, 13384, 124, 60, 0, 24, 13384, 13384, 56, 28, 0, 8208, 28, 0, 8208, 16, 2064, 2064, 0, 4112, 0, 0, 16, 0, 17408, 30, 12328, 6152, 0, 0, 0, 43650, 55038, 27902, 14460, 14352, 14460, 16, 65519, 14404, 51131, 1546, 17476, 7186, 8766, 10308, 24688, 6200, 14420, 27756, 21588, 16440, 0, 14420, 14420, 4112, 4104, 4128, 1799, 57568, 4112, 31800, 0, 4112, 10320, 18684, 15440, 25608, 18504, 4128, 4128, 4104, 21560, 4112, 0, 0, 0, 1032, 17484, 12304, 17412, 17412, 6184, 16448, 16448, 1028, 17476, 17476, 12336, 12336, 4128, 124, 8208, 17412, 18436, 17476, 17476, 17472, 18500, 16448, 16448, 16448, 17476, 4112, 2056, 18512, 16448, 27732, 25684, 17476, 17476, 17476, 17476, 17472, 4112, 17476, 17476, 17476, 17448, 17448, 1032, 8224, 16416, 2056, 10308, 0, 4104, 56, 16504, 60, 1084, 56, 9248, 52, 16504, 48, 48, 16452, 4112, 104, 120, 56, 88, 52, 88, 56, 8312, 68, 68, 68, 68, 68, 124, 8224, 4112, 2056, 18432, 4136, 17472, 68, 56, 56, 56, 56, 4152, 60, 56, 56, 56, 48, 48, 48, 14404, 14404, 31808, 104, 20560, 56, 56, 56, 68, 68, 68, 17476, 17476, 14404, 9248, 10256, 1028, 8224, 56, 48, 56, 68, 18432, 100, 10264, 10256, 16, 14404, 0, 18448, 18448, 0, 9288, 36936, 4420, 21930, 61115, 6168, 6168, 30788, 16448, 39080, 1080, 14070, 13878, 254, 14070, 18500, 2068, 0, 6168, 6168, 0, 6168, 0, 6168, 20560, 56, 13879, 63, 14071, 255, 13879, 255, 14071, 14352, 14404, 17476, 14404, 14404, 31808, 31808, 31808, 14352, 14352, 6168, 0, 14404, 17476, 68, 3855, 65535, 52, 18504, 14404, 56, 16416, 29812, 68, 9248, 14404, 56, 17538, 8208, 40, 14404, 8256, 14404, 14352, 4220, 17448, 68, 3090, 4112, 4096, 13384, 10256, 48, 17448, 4112, 10240, 4120, 14392, 0, 0, 65210, 33478, 65148, 65148, 65238, 65238, 14352, 51183, 17476, 48059, 12360, 14352, 4208, 8806, 65092, 30832, 30776, 4180, 27756, 13332, 17464, 31868, 4180, 4112, 4180, 31752, 31776, 3871, 61688, 14392, 14352, 0, 4096, 0, 18684, 14356, 4128, 12362, 0, 8224, 2056, 4152, 31760, 16, 31744, 48, 4128, 21604, 4112, 14400, 6148, 18556, 30724, 30788, 2064, 14404, 15364, 48, 32, 16416, 124, 2064, 2064, 13396, 31812, 30788, 16448, 17476, 30784, 30784, 23620, 31812, 4112, 2056, 24656, 16448, 17476, 19524, 17476, 30784, 17492, 30800, 14340, 4112, 17476, 17448, 21588, 4136, 4112, 4128, 8224, 4104, 2056, 0, 0, 0, 1084, 17476, 16448, 17476, 17532, 30752, 19524, 17476, 4112, 4112, 18544, 4112, 21588, 17476, 17476, 25668, 19524, 25664, 16440, 8224, 17476, 17476, 17492, 10256, 17476, 2064, 16416, 16, 1032, 0, 17476, 16448, 17476, 17532, 1084, 1084, 1084, 1084, 16448, 17532, 17532, 17532, 4112, 4112, 4112, 17532, 17532, 30784, 5180, 30800, 17476, 17476, 17476, 17476, 17476, 17476, 17476, 17476, 16452, 28704, 31760, 2064, 4104, 1084, 4112, 17476, 17476, 30788, 21580, 56, 56, 8256, 17532, 31748, 11332, 8276, 4112, 36936, 9288, 4420, 21930, 61115, 6168, 6392, 17528, 28744, 43176, 19540, 1782, 13878, 1782, 1790, 62532, 1084, 248, 6175, 6399, 255, 6175, 255, 6399, 22608, 21596, 12351, 12343, 255, 247, 12343, 255, 247, 4112, 17476, 17476, 17532, 17532, 30784, 30784, 30784, 4112, 4112, 6392, 31, 17476, 17476, 17476, 3855, 65280, 18504, 20552, 17532, 1084, 4128, 13332, 17476, 30752, 17476, 17476, 33348, 14404, 21544, 17476, 31808, 17476, 4112, 4112, 10256, 17476, 4624, 4240, 31744, 52, 0, 12288, 10256, 36944, 0, 0, 14392, 0, 0, 33410, 65278, 14352, 14352, 4152, 14460, 0, 65535, 14336, 51199, 18480, 31760, 61536, 60996, 10240, 24640, 6152, 14352, 108, 5140, 1080, 31744, 14460, 4112, 14352, 4096, 4096, 16191, 64764, 31744, 4096, 0, 16, 0, 18504, 30736, 19468, 17466, 0, 4104, 4128, 21520, 4096, 4128, 0, 12288, 16384, 17464, 4220, 16508, 17464, 2056, 1144, 17464, 4112, 17464, 1080, 12288, 8256, 4096, 0, 8192, 16, 21544, 17476, 17528, 17464, 18544, 16508, 16448, 17468, 17476, 4152, 18480, 18500, 16508, 17476, 17476, 17464, 16448, 18484, 18500, 17464, 4112, 17464, 10256, 21544, 17476, 4112, 16508, 8248, 1024, 2104, 0, 124, 0, 17468, 17528, 16444, 17468, 16440, 8224, 17468, 17476, 4112, 4112, 18500, 4104, 21588, 17476, 17464, 25688, 19508, 16448, 1080, 9240, 19508, 10256, 27716, 10308, 17468, 8316, 8216, 4112, 2096, 0, 31744, 17464, 17468, 16440, 17468, 17468, 17468, 17468, 16444, 16440, 16440, 16440, 4112, 4112, 4112, 17476, 17476, 16508, 20524, 20572, 17464, 17464, 17464, 17468, 17468, 17468, 17464, 17464, 14352, 8316, 31760, 8224, 1028, 17468, 4112, 17464, 17468, 17476, 17476, 0, 0, 17464, 17476, 1024, 2060, 7172, 4112, 9216, 36864, 4420, 21930, 61115, 6168, 6168, 16448, 18544, 51312, 25656, 13878, 13878, 13878, 0, 18544, 17464, 6168, 0, 0, 6168, 6168, 0, 6168, 20540, 20536, 0, 13878, 0, 13878, 13878, 0, 13878, 4152, 17464, 17464, 17476, 17476, 16508, 16508, 16508, 4152, 4152, 0, 6168, 17464, 17464, 17464, 3855, 0, 18484, 17496, 17476, 17468, 16508, 5140, 17528, 8316, 17464, 17464, 10478, 17464, 0, 17464, 8220, 17408, 4152, 124, 4112, 17468, 4112, 36960, 4096, 18432, 0, 0, 4112, 12304, 0, 0, 14336, 0, 0, 31744, 31744, 0, 0, 0, 0, 0, 65535, 0, 65535, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32767, 65279, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1080, 0, 0, 24576, 0, 0, 0, 0, 0, 16448, 1028, 0, 0, 0, 0, 0, 0, 0, 1080, 0, 0, 0, 0, 0, 0, 4192, 0, 0, 0, 0, 0, 0, 4192, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1080, 0, 0, 0, 0, 0, 16512, 513, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4420, 21930, 61115, 6168, 6168, 0, 16448, 32768, 16384, 13878, 13878, 13878, 0, 0, 0, 6168, 0, 0, 6168, 6168, 0, 6168, 0, 0, 0, 13878, 0, 13878, 13878, 0, 13878, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6168, 0, 0, 0, 3855, 0, 0, 0, 0, 0, 0, 0, 16512, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1080, 4112, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final int[] DEFAULT_FONT_LEM = new int[] {0x000f, 0x0808, 0x080f, 0x0808, 0x08f8, 0x0808, 0x00ff, 0x0808, 0x0808, 0x0808, 0x08ff, 0x0808, 0x00ff, 0x1414, 0xff00, 0xff08, 0x1f10, 0x1714, 0xfc04, 0xf414, 0x1710, 0x1714, 0xf404, 0xf414, 0xff00, 0xf714, 0x1414, 0x1414, 0xf700, 0xf714, 0x1417, 0x1414, 0x0f08, 0x0f08, 0x14f4, 0x1414, 0xf808, 0xf808, 0x0f08, 0x0f08, 0x001f, 0x1414, 0x00fc, 0x1414, 0xf808, 0xf808, 0xff08, 0xff08, 0x14ff, 0x1414, 0x080f, 0x0000, 0x00f8, 0x0808, 0xffff, 0xffff, 0xf0f0, 0xf0f0, 0xffff, 0x0000, 0x0000, 0xffff, 0x0f0f, 0x0f0f, 0x0000, 0x0000, 0x005f, 0x0000, 0x0300, 0x0300, 0x3e14, 0x3e00, 0x266b, 0x3200, 0x611c, 0x4300, 0x3629, 0x7650, 0x0002, 0x0100, 0x1c22, 0x4100, 0x4122, 0x1c00, 0x2a1c, 0x2a00, 0x083e, 0x0800, 0x4020, 0x0000, 0x0808, 0x0800, 0x0040, 0x0000, 0x601c, 0x0300, 0x3e41, 0x3e00, 0x427f, 0x4000, 0x6259, 0x4600, 0x2249, 0x3600, 0x0f08, 0x7f00, 0x2745, 0x3900, 0x3e49, 0x3200, 0x6119, 0x0700, 0x3649, 0x3600, 0x2649, 0x3e00, 0x0024, 0x0000, 0x4024, 0x0000, 0x0814, 0x2241, 0x1414, 0x1400, 0x4122, 0x1408, 0x0259, 0x0600, 0x3e59, 0x5e00, 0x7e09, 0x7e00, 0x7f49, 0x3600, 0x3e41, 0x2200, 0x7f41, 0x3e00, 0x7f49, 0x4100, 0x7f09, 0x0100, 0x3e49, 0x3a00, 0x7f08, 0x7f00, 0x417f, 0x4100, 0x2040, 0x3f00, 0x7f0c, 0x7300, 0x7f40, 0x4000, 0x7f06, 0x7f00, 0x7f01, 0x7e00, 0x3e41, 0x3e00, 0x7f09, 0x0600, 0x3e41, 0xbe00, 0x7f09, 0x7600, 0x2649, 0x3200, 0x017f, 0x0100, 0x7f40, 0x7f00, 0x1f60, 0x1f00, 0x7f30, 0x7f00, 0x7708, 0x7700, 0x0778, 0x0700, 0x7149, 0x4700, 0x007f, 0x4100, 0x031c, 0x6000, 0x0041, 0x7f00, 0x0201, 0x0200, 0x8080, 0x8000, 0x0001, 0x0200, 0x2454, 0x7800, 0x7f44, 0x3800, 0x3844, 0x2800, 0x3844, 0x7f00, 0x3854, 0x5800, 0x087e, 0x0900, 0x4854, 0x3c00, 0x7f04, 0x7800, 0x447d, 0x4000, 0x2040, 0x3d00, 0x7f10, 0x6c00, 0x417f, 0x4000, 0x7c18, 0x7c00, 0x7c04, 0x7800, 0x3844, 0x3800, 0x7c14, 0x0800, 0x0814, 0x7c00, 0x7c04, 0x0800, 0x4854, 0x2400, 0x043e, 0x4400, 0x3c40, 0x7c00, 0x1c60, 0x1c00, 0x7c30, 0x7c00, 0x6c10, 0x6c00, 0x4c50, 0x3c00, 0x6454, 0x4c00, 0x0836, 0x4100, 0x0077, 0x0000, 0x4136, 0x0800, 0x0201, 0x0201, 0x704c, 0x7000};
    private static final int[] DEFAULT_PALETTE = new int[] {0x000, 0x00a, 0x0a0, 0x0aa, 0xa00, 0xa0a, 0xa50, 0xaaa, 0x555, 0x55f, 0x5f5, 0x5ff, 0xf55, 0xf5f, 0xff5, 0xfff};
    // the charset of the default font (for the "Copy" function)
    private static final char[] CHARSET = " ☺☻♥♦♣♠•◘○◙♂♀♪♫☼►◄↕‼¶§▬↨↑↓→←∟↔▲▼ !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~⌂ÇüéâäàåçêëèïîÍÄÅÉæÆôöòûùÿÖÜ¢£¥╱╲áíóúñÑªº¿Á¬½¼¡«»░▒▓│┤ÞþØø╣║╗╝Ðð┐└┴┬├─┼Œœ╚╔╩╦╠═╬ÍÓÚÀÂÈÊËîÏ┘┌ÔÙ ▐▀αßÃãΣ¶µÕÒõΩδ∞Ûε∩Ì±Ýý⌠⌡÷≈°∙Ÿ√ⁿ²■ ".toCharArray();


}
