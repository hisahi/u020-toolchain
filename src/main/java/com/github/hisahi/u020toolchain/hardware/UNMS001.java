
package com.github.hisahi.u020toolchain.hardware; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UNMS001 extends Hardware {
    private int curX;
    private int curY;
    private int buttons;
    private int intmsg;

    public UNMS001(UCPU16 cpu) {
        super(cpu);
    }

    @Override
    public long hardwareId() {
        return 0xab212484L; 
    }

    @Override
    public int hardwareVersion() {
        return 0x0001;
    }

    @Override
    public long hardwareManufacturer() {
        return 0x2590a31c;
    }

    @Override
    public void hwi(UCPU16 cpu) {
        switch (cpu.readRegister(UCPU16.REG_A)) {
            case 0:
                cpu.writeRegister(UCPU16.REG_X, curX);
                cpu.writeRegister(UCPU16.REG_Y, curY);
                return;
            case 1:
                cpu.writeRegister(UCPU16.REG_C, buttons);
                break;
            case 2:
                this.intmsg = cpu.readRegister(UCPU16.REG_B);
                break;
        }
    }

    @Override
    public void reset() {
        curX = curY = buttons = 0;
    }

    @Override
    public void saveState(DataOutputStream stream) throws IOException {
        stream.writeInt(curX);
        stream.writeInt(curY);
        stream.writeInt(buttons);
        stream.writeInt(intmsg);
    }

    @Override
    public void restoreState(DataInputStream stream) throws IOException {
        curX = stream.readInt();
        curY = stream.readInt();
        buttons = stream.readInt();
        intmsg = stream.readInt();
    }
    
    public void setPos(int x, int y) {
        if (x < 0 || x >= 256 || y < 0 || y >= 192) {
            return;
        }
        if (curX != x || curY != y) {
            curX = x;
            curY = y;
            if (intmsg != 0) {
                cpu.queueInterrupt(intmsg);
            }
        }
    }
    
    public void leftDown() {
        int ob = buttons;
        buttons |= 1;
        if (ob != buttons && intmsg != 0) {
            cpu.queueInterrupt(intmsg);
        }
    }
    
    public void rightDown() {
        int ob = buttons;
        buttons |= 2;
        if (ob != buttons && intmsg != 0) {
            cpu.queueInterrupt(intmsg);
        }
    }
    
    public void leftUp() {
        int ob = buttons;
        buttons &= ~1;
        if (ob != buttons && intmsg != 0) {
            cpu.queueInterrupt(intmsg);
        }
    }
    
    public void rightUp() {
        int ob = buttons;
        buttons &= ~2;
        if (ob != buttons && intmsg != 0) {
            cpu.queueInterrupt(intmsg);
        }
    }

    public void setLeft(boolean down) {
        if (down) {
            leftDown();
        } else {
            leftUp();
        }
    }

    public void setRight(boolean down) {
        if (down) {
            rightDown();
        } else {
            rightUp();
        }
    }

    public void updateFromData(int px, int py, boolean primaryButtonDown, boolean secondaryButtonDown) {
        setPos(px, py);
        setLeft(primaryButtonDown);
        setRight(secondaryButtonDown);
    }
    
}
