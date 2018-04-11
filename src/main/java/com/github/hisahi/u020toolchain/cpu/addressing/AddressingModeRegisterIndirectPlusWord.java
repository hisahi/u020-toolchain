
package com.github.hisahi.u020toolchain.cpu.addressing; 

import com.github.hisahi.u020toolchain.cpu.Register;
import com.github.hisahi.u020toolchain.cpu.UCPU16;

/**
 * Implements the UCPU-16 indirect register plus word addressing mode, in
 * which the accesses are targeted at a memory location chosen by the value
 * in one of the main eight UCPU-16 registers (A, B, C, X, Y, Z, I or J)
 * with the parameter word added to the value of the register to decide
 * the final address.
 * 
 * @author hisahi
 */
public class AddressingModeRegisterIndirectPlusWord implements IAddressingMode {
    private Register reg;
    
    /**
     * Initializes a new AddressingModeRegisterIndirectPlusWord instance.
     * 
     * @param reg The register this literal addressing mode represents.
     */
    public AddressingModeRegisterIndirectPlusWord(Register reg) {
        this.reg = reg;
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
        return cpu.getMemory().read((cpu.readRegister(reg) + addr) & 0xFFFF);
    }

    @Override
    public void write(UCPU16 cpu, int addr, int val) {
        cpu.getMemory().write((cpu.readRegister(reg) + addr) & 0xFFFF, val);
    }

    @Override
    public String format(boolean isB, int val, String label) {
        if (label != null) {
            return "[" + String.valueOf("ABCXYZIJ".charAt(reg.ordinal())) + "+" + label + "]";
        } else {
            return "[" + String.valueOf("ABCXYZIJ".charAt(reg.ordinal())) + "+" + String.format("0x%04x", val) + "]";
        }
    }
}
