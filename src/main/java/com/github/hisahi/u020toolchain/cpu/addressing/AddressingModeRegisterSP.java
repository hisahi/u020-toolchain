
package com.github.hisahi.u020toolchain.cpu.addressing; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;

public class AddressingModeRegisterSP implements IAddressingMode {

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
    public String format(boolean isB, int val) {
        return "SP";
    }

}
