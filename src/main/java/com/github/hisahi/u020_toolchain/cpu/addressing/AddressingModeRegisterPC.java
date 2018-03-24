
package com.github.hisahi.u020_toolchain.cpu.addressing; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;

public class AddressingModeRegisterPC implements IAddressingMode {

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
    public String format(boolean is_B, int val) {
        return "PC";
    }

}
