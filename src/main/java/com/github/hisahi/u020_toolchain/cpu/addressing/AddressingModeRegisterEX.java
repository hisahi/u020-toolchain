
package com.github.hisahi.u020_toolchain.cpu.addressing; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;

public class AddressingModeRegisterEX implements IAddressingMode {

    public AddressingModeRegisterEX() {
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
        return cpu.getEX();
    }

    @Override
    public void write(UCPU16 cpu, int addr, int val) {
        cpu.setEX(val);
    }

    @Override
    public String format(boolean is_B, int val) {
        return "EX";
    }

}
