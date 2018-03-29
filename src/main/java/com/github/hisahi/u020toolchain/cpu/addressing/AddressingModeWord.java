
package com.github.hisahi.u020toolchain.cpu.addressing; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;

public class AddressingModeWord implements IAddressingMode {

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
    public String format(boolean isB, int val) {
        return String.format("0x%04x", val);
    }

}
