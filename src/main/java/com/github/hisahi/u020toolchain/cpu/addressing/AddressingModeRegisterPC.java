
package com.github.hisahi.u020toolchain.cpu.addressing; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;

/**
 * Implements the UCPU-16 register addressing mode, in which the accesses
 * are targeted at the PC register used to store the location at which
 * the UCPU-16 is currently executing code.
 * 
 * @author hisahi
 */
public class AddressingModeRegisterPC implements IAddressingMode {

    /**
     * Initializes a new AddressingModeRegisterPC instance.
     */
    public AddressingModeRegisterPC() {
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
        return cpu.getPC();
    }

    @Override
    public void write(UCPU16 cpu, int addr, int val) {
        cpu.setPC(val);
    }

    @Override
    public String format(boolean isB, int val, String label) {
        return "PC";
    }

}
