
package com.github.hisahi.u020_toolchain.cpu.instructions; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;
import com.github.hisahi.u020_toolchain.cpu.addressing.IAddressingMode;

public class InstructionINT implements IInstruction {

    public InstructionINT() {
    }

    @Override
    public int getCycles() {
        return 4;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        cpu.queueInterrupt(a.read(cpu, am));
    }

}
