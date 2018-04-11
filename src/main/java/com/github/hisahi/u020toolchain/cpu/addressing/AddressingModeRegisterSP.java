
package com.github.hisahi.u020toolchain.cpu.addressing; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;

/**
 * Implements the UCPU-16 register addressing mode, in which the accesses
 * are targeted at the SP used as the stack pointer, pointing to the first
 * element in the stack growing downwards from the end of the main memory.
 * 
 * @author hisahi
 */
public class AddressingModeRegisterSP implements IAddressingMode {

    /**
     * Initializes a new AddressingModeRegisterSP instance.
     */
    public AddressingModeRegisterSP() {
    }

    @Override
    public boolean takesNextWord() {
        return false;
    }

    @Override
    public int getCycles() {
        return 0;
    }

    @Override
    public int read(UCPU16 cpu, int addr) {
        return cpu.getSP();
    }

    @Override
    public void write(UCPU16 cpu, int addr, int val) {
        cpu.setSP(val);
    }

    @Override
    public String format(boolean isB, int val, String label) {
        return "SP";
    }

}
