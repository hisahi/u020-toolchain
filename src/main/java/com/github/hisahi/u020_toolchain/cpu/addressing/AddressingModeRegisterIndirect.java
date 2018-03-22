
package com.github.hisahi.u020_toolchain.cpu.addressing; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;

public class AddressingModeRegisterIndirect implements IAddressingMode {
    private int reg;
    public AddressingModeRegisterIndirect(int i) {
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
        return cpu.getMemory().read(cpu.readRegister(reg));
    }

    @Override
    public void write(UCPU16 cpu, int addr, int val) {
        cpu.getMemory().write(cpu.readRegister(reg), val);
    }
}
