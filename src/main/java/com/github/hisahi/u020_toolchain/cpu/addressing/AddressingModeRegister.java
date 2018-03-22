
package com.github.hisahi.u020_toolchain.cpu.addressing; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;

public class AddressingModeRegister implements IAddressingMode {
    private int reg;
    public AddressingModeRegister(int i) {
        this.reg = i;
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
}
