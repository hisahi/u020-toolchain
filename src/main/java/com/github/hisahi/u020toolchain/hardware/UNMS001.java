
package com.github.hisahi.u020toolchain.hardware; 

import com.github.hisahi.u020toolchain.cpu.Register;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Implements the UNMS001 peripheral that serves as a mouse for
 * the computer. The mouse has two buttons and can be moved
 * around the screen.
 * 
 * @author hisahi
 */
public class UNMS001 extends Hardware {
    private int curX;
    private int curY;
    private int buttons;
    private int intmsg;

    /**
     * Initializes a new UNMS001 instance.
     * 
     * @param cpu The UCPU16 instance.
     */
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
        switch (cpu.readRegister(Register.A)) {
            case 0:
                cpu.writeRegister(Register.X, curX);
                cpu.writeRegister(Register.Y, curY);
                return;
            case 1:
                cpu.writeRegister(Register.C, buttons);
                break;
            case 2:
                this.intmsg = cpu.readRegister(Register.B);
                break;
        }
    }

    @Override
    public void reset() {
        curX = curY = buttons = 0;
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
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
    
    /**
     * Sets the new mouse cursor position.
     * 
     * @param x The X coordinate of the new cursor position.
     * @param y The Y coordinate of the new cursor position.
     */
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
    
    /**
     * Presses and holds the primary mouse button.
     */
    public void leftDown() {
        int ob = buttons;
        buttons |= 1;
        if (ob != buttons && intmsg != 0) {
            cpu.queueInterrupt(intmsg);
        }
    }
    
    /**
     * Presses and holds the secondary mouse button.
     */
    public void rightDown() {
        int ob = buttons;
        buttons |= 2;
        if (ob != buttons && intmsg != 0) {
            cpu.queueInterrupt(intmsg);
        }
    }
    
    /**
     * Releases the primary mouse button.
     */
    public void leftUp() {
        int ob = buttons;
        buttons &= ~1;
        if (ob != buttons && intmsg != 0) {
            cpu.queueInterrupt(intmsg);
        }
    }
    
    /**
     * Releases the secondary mouse button.
     */
    public void rightUp() {
        int ob = buttons;
        buttons &= ~2;
        if (ob != buttons && intmsg != 0) {
            cpu.queueInterrupt(intmsg);
        }
    }

    /**
     * Updates whether the primary mouse button is down.
     * 
     * @param down                Whether the primary mouse button is down.
     */
    public void setLeft(boolean down) {
        if (down) {
            leftDown();
        } else {
            leftUp();
        }
    }

    /**
     * Updates whether the secondary mouse button is down.
     * 
     * @param down                Whether the secondary mouse button is down.
     */
    public void setRight(boolean down) {
        if (down) {
            rightDown();
        } else {
            rightUp();
        }
    }

    /**
     * Updates the mouse state from the given data.
     * 
     * @param px                  The X coordinate of the new cursor position.
     * @param py                  The Y coordinate of the new cursor position.
     * @param primaryButtonDown   Whether the primary mouse button is down.
     * @param secondaryButtonDown Whether the secondary mouse button is down.
     */
    public void updateFromData(int px, int py, boolean primaryButtonDown, boolean secondaryButtonDown) {
        setPos(px, py);
        setLeft(primaryButtonDown);
        setRight(secondaryButtonDown);
    }
    
}
