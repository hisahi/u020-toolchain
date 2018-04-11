
package com.github.hisahi.u020toolchain.cpu.addressing; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;

/**
 * Implements the UCPU-16 indirect word addressing mode, in which the
 * accesses are targeted at a location in memory chosen by the parameter
 * word.
 * 
 * @author hisahi
 */
public class AddressingModeIndirectWord implements IAddressingMode {

    /**
     * Initializes a new AddressingModeIndirectWord instance.
     */
    public AddressingModeIndirectWord() {
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
        return cpu.getMemory().read(addr);
    }

    @Override
    public void write(UCPU16 cpu, int addr, int val) {
        cpu.getMemory().write(addr, val);
    }

    @Override
    public String format(boolean isB, int val, String label) {
        if (label != null) {
            return String.format("[%s]", label);
        } else {
            return String.format("[0x%04x]", val);
        }
    }

}
