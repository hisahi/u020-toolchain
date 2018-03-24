
package com.github.hisahi.u020_toolchain.cpu.addressing; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;

public class AddressingModeRegisterIndirectPlusWord implements IAddressingMode {
    private int reg;
    public AddressingModeRegisterIndirectPlusWord(int i) {
        this.reg = i;
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
    public String format(boolean is_B, int val) {
        return "[" + String.valueOf("ABCXYZIJ".charAt(reg)) + "+" + String.format("0x%04x",val) + "]";
    }
}
