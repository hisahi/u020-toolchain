
package com.github.hisahi.u020toolchain.cpu.addressing; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;

public class AddressingModeStackPeek implements IAddressingMode {

    public AddressingModeStackPeek() {
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
        return cpu.getMemory().read(cpu.getSP());
    }

    @Override
    public void write(UCPU16 cpu, int addr, int val) {
        cpu.getMemory().write(cpu.getSP(), val);
    }

    @Override
    public String format(boolean isB, int val, String label) {
        return "[SP]";
    }

}
