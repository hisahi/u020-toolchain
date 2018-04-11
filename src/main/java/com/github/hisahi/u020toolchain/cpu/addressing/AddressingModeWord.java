
package com.github.hisahi.u020toolchain.cpu.addressing; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;

/**
 * Implements the UCPU-16 direct word addressing mode, in which the accesses 
 * are targeted at the parameter word. 
 * 
 * Writes will silently fail.
 * 
 * @author hisahi
 */
public class AddressingModeWord implements IAddressingMode {

    /**
     * Initializes a new AddressingModeWord instance.
     */
    public AddressingModeWord() {
    }

    @Override
    public boolean takesNextWord() {
        return true;
    }

    @Override
    public int getCycles() {
        return 1;
    }

    @Override
    public int read(UCPU16 cpu, int addr) {
        return addr;
    }

    @Override
    public void write(UCPU16 cpu, int addr, int val) {
    }

    @Override
    public String format(boolean isB, int val, String label) {
        if (label != null) {
            return label;
        } else {
            return String.format("0x%04x", val);
        }
    }

}
