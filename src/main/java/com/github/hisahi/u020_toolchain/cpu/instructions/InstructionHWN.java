
package com.github.hisahi.u020_toolchain.cpu.instructions; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;
import com.github.hisahi.u020_toolchain.cpu.addressing.IAddressingMode;

public class InstructionHWN implements IInstruction {

    public InstructionHWN() {
    }

    @Override
    public int getCycles() {
        return 2;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        a.write(cpu, am, cpu.getDevices().size());
    }

}