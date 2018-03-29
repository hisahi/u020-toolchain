
package com.github.hisahi.u020toolchain.cpu.addressing; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;

public class AddressingModeStackPick implements IAddressingMode {

    public AddressingModeStackPick() {
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
        return cpu.getMemory().read((cpu.getSP() + addr) & 0xFFFF);
    }

    @Override
    public void write(UCPU16 cpu, int addr, int val) {
        cpu.getMemory().write((cpu.getSP() + addr) & 0xFFFF, val);
    }

    @Override
    public String format(boolean isB, int val) {
        return "[SP+" + String.format("0x%04x", val) + "]";
    }

}
