
package com.github.hisahi.u020toolchain.cpu.addressing; 

import com.github.hisahi.u020toolchain.cpu.Register;
import com.github.hisahi.u020toolchain.cpu.UCPU16;

/**
 * Implements the UCPU-16 register addressing mode, in which the accesses
 * are targeted at one of the main eight UCPU-16 registers (A, B, C, X, 
 * Y, Z, I or J).
 * 
 * @author hisahi
 */
public class AddressingModeRegister implements IAddressingMode {
    private Register reg;
    
    /**
     * Initializes a new AddressingModeRegister instance.
     * 
     * @param reg The register this literal addressing mode represents.
     */
    public AddressingModeRegister(Register reg) {
        this.reg = reg;
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
        return cpu.readRegister(reg);
    }

    @Override
    public void write(UCPU16 cpu, int addr, int val) {
        cpu.writeRegister(reg, val);
    }

    @Override
    public String format(boolean isB, int val, String label) {
        return String.valueOf("ABCXYZIJ".charAt(reg.ordinal()));
    }
}
