
package com.github.hisahi.u020toolchain.hardware; 

import com.github.hisahi.u020toolchain.cpu.Register;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import javafx.scene.input.KeyCode;

/**
 * Implements the Generic Keyboard peripheral. Includes the logic
 * used to decode keys from the JavaFX KeyCode format to the key codes
 * used by the Univtek 020.
 * 
 * The keyboard is only optimized for the US keyboard layout.
 * 
 * @author hisahi
 */
public class Keyboard extends Hardware {
    private static final int KEY_QUEUE_SIZE = 32;
    private Queue<Integer> queue;
    private boolean[] keydown;
    private int intmsg;
    
    /**
     * Initializes a new Keyboard instance.
     * 
     * @param cpu The UCPU16 instance. 
     */
    public Keyboard(UCPU16 cpu) {
        super(cpu);
        this.reset();
    }

    @Override
    public long hardwareId() {
        return 0x30cf7406L;
    }

    @Override
    public int hardwareVersion() {
        return 1;
    }

    @Override
    public long hardwareManufacturer() {
        return 0x55AA55AAL;
    }

    @Override
    public void hwi(UCPU16 cpu) {
        switch (cpu.readRegister(Register.A)) {
            case 0:
                this.queue.clear();
                break;
            case 1:
                if (this.queue.isEmpty()) {
                    cpu.writeRegister(Register.C, 0);
                } else {
                    cpu.writeRegister(Register.C, this.queue.poll());
                }
                break;
            case 2: 
            {
                int b = cpu.readRegister(Register.B);
                cpu.writeRegister(Register.C, isKeyDown(b) ? 1 : 0);
                break;
            }
            case 3: 
                intmsg = cpu.readRegister(Register.B);
                break;
            case 0x2739:
                coldReset();
                break;
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
    
    /**
     * Tests whether a virtual keyboard key is down.
     * 
     * @param b     The internal key code to test.
     * @return      Whether the requested key is down.
     */
    public boolean isKeyDown(int b) {
        if (b >= 0x200) {
            return false;
        }
        return keydown[b];
    }

    /**
     * Presses a virtual keyboard key.
     * 
     * @param code  The internal key code of the key that is to be pressed down.
     */
    public void keyDown(int code) {
        // 0x2739 is a hardcoded keycode that triggers a cold reset
        // (Ctrl + Univtek)
        if (code == 0x2739) {
            coldReset();
            return;
        }
        if (code != 0) {
            this.keydown[code] = true;
            if (this.queue.size() < KEY_QUEUE_SIZE) {
                this.queue.add(code);
            }
            if (intmsg != 0) {
                this.cpu.queueInterrupt(intmsg);
            }
        }
    }

    /**
     * Releases a virtual keyboard key.
     * 
     * @param code  The internal key code of the key that is to be released.
     */
    public void keyUp(int code) {
        // 0x2739 is a hardcoded keycode that triggers a cold reset
        // (Ctrl + Univtek)
        if (code == 0x2739) {
            return;
        }
        if (code != 0) {
            this.keydown[code] = false;
            if (intmsg != 0) {
                this.cpu.queueInterrupt(intmsg);
            }
        }
    }
    
    /**
     * Adds a keypress into the keyboard queue. This is used to feed
     * input from pasted text.
     * 
     * @param code The key code to be added.
     */
    public void addToKeyQueueFromPaste(int code) {
        if (this.queue.size() < 16384) {
            this.queue.add(code);
        }
    }

    /**
     * This function is to be called when a keyboard key is pressed.
     * 
     * @param code  The JavaFX KeyCode of the key that is to be pressed down.
     * @param shift Whether the Shift modifier key is down.
     * @param ctrl  Whether the Ctrl modifier key is down.
     */
    public void keyDown(KeyCode code, boolean shift, boolean ctrl) {
        keyDown(convertKey(code, shift, ctrl));
    }

    /**
     * This function is to be called when a keyboard key is released.
     * 
     * @param code  The JavaFX KeyCode of the key that is to be released.
     * @param shift Whether the Shift modifier key is down.
     * @param ctrl  Whether the Ctrl modifier key is down.
     */
    public void keyUp(KeyCode code, boolean shift, boolean ctrl) {
        keyUp(convertKey(code, shift, ctrl));
    }

    private static int convertKey(KeyCode code, boolean shift, boolean ctrl) {
        switch (code) {
            case BACK_QUOTE:        return !shift ? 0x60 : 0x7e;   
            case MINUS:             return !shift ? 0x2d : 0x5f;   
            case DIGIT1:            return !shift ? 0x31 : 0x21;  
            case DIGIT2:            return !shift ? 0x32 : 0x22;  
            case DIGIT3:            return !shift ? 0x33 : 0x23;  
            case DIGIT4:            return !shift ? 0x34 : 0x24;  
            case DIGIT5:            return !shift ? 0x35 : 0x25;  
            case DIGIT6:            return !shift ? 0x36 : 0x5e;  
            case DIGIT7:            return !shift ? 0x37 : 0x26;  
            case DIGIT8:            return !shift ? 0x38 : 0x2a;  
            case DIGIT9:            return !shift ? 0x39 : 0x28;  
            case DIGIT0:            return !shift ? 0x30 : 0x29;   
            case EQUALS:            return !shift ? 0x3d : 0x2b;   
            case Q:                 return !shift ? 0x51 : 0x71;
            case W:                 return !shift ? 0x57 : 0x77;
            case E:                 return !shift ? 0x45 : 0x65;
            case R:                 return !shift ? 0x52 : 0x72;
            case T:                 return !shift ? 0x54 : 0x74;
            case Y:                 return !shift ? 0x59 : 0x79;
            case U:                 return !shift ? 0x55 : 0x75;
            case I:                 return !shift ? 0x49 : 0x69;
            case O:                 return !shift ? 0x4f : 0x6f;
            case P:                 return !shift ? 0x50 : 0x70;        
            case BACK_SPACE:        return 0x10;   
            case OPEN_BRACKET:      return !shift ? 0x5b : 0x7b;   
            case CLOSE_BRACKET:     return !shift ? 0x5d : 0x7d;   
            case BACK_SLASH:        return !shift ? 0x5c : 0x7c;   
            case ENTER:             return 0x11;   
            case A:                 return !shift ? 0x41 : 0x61;
            case S:                 return !shift ? 0x53 : 0x73;
            case D:                 return !shift ? 0x44 : 0x64;
            case F:                 return !shift ? 0x46 : 0x66;
            case G:                 return !shift ? 0x47 : 0x67;
            case H:                 return !shift ? 0x48 : 0x68;
            case J:                 return !shift ? 0x4a : 0x6a;
            case K:                 return !shift ? 0x4b : 0x6b;
            case L:                 return !shift ? 0x4c : 0x6c;
            case SEMICOLON:         return !shift ? 0x3b : 0x3a;   
            case QUOTE:             return !shift ? 0x27 : 0x2a;     
            case Z:                 return !shift ? 0x5a : 0x7a;
            case X:                 return !shift ? 0x58 : 0x78;
            case C:                 return !shift ? 0x43 : 0x63;
            case V:                 return !shift ? 0x56 : 0x76;
            case B:                 return !shift ? 0x42 : 0x62;
            case N:                 return !shift ? 0x4e : 0x6e;
            case M:                 return !shift ? 0x4d : 0x6d;
            case COMMA:             return !shift ? 0x2c : 0x3c;     
            case PERIOD:            return !shift ? 0x2e : 0x3e;     
            case SLASH:             return !shift ? 0x2f : 0x3f;     
            case SHIFT:             return 0x190;     
            case CONTROL:           return 0x191;     
            case INSERT:            return 0x12;     
            case DELETE:            return 0x13;     
            case SPACE:             return 0x20;
            case UP:                return 0x180;     
            case DOWN:              return 0x181;     
            case LEFT:              return 0x182;     
            case RIGHT:             return 0x183;     
            case HOME:              return ctrl ? 0x2739 : 0x192;     
            default:                return 0;
        }
    }

    private void coldReset() {
        cpu.reset(true);
    }

    @Override
    public void reset() {
        this.queue = new ArrayDeque<>();
        this.keydown = new boolean[0x200];
        this.intmsg = 0;
    }

    @Override
    public void saveState(DataOutputStream stream) throws IOException {
        // write the keyboard queue
        stream.writeInt(queue.size());
        for (int i: queue) {
            stream.writeInt(i);
        }
    }

    @Override
    public void restoreState(DataInputStream stream) throws IOException {
        // read the keyboard queue
        int count = stream.readInt();
        for (int i = 0; i < count; ++i) {
            queue.add(stream.readInt());
        }
    }

}
