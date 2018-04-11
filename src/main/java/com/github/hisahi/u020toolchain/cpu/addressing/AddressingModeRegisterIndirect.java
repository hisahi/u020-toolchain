
package com.github.hisahi.u020toolchain.cpu.addressing; 

import com.github.hisahi.u020toolchain.cpu.Register;
import com.github.hisahi.u020toolchain.cpu.UCPU16;

/**
 * Implements the UCPU-16 indirect register addressing mode, in which the
 * accesses are targeted at a memory location chosen by the value in one
 * of the main eight UCPU-16 registers (A, B, C, X, Y, Z, I or J).
 * 
 * @author hisahi
 */
public class AddressingModeRegisterIndirect implements IAddressingMode {
    private Register reg;
    
    /**
     * Initializes a new AddressingModeRegisterIndirect instance.
     * 
     * @param reg The register this literal addressing mode represents.
     */
    public AddressingModeRegisterIndirect(Register reg) {
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
        return cpu.getMemory().read(cpu.readRegister(reg));
    }

    @Override
    public void write(UCPU16 cpu, int addr, int val) {
        cpu.getMemory().write(cpu.readRegister(reg), val);
    }

    @Override
    public String format(boolean isB, int val, String label) {
        return "[" + String.valueOf("ABCXYZIJ".charAt(reg.ordinal())) + "]";
    }
}
