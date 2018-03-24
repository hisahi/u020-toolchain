
package com.github.hisahi.u020_toolchain.cpu.instructions; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;
import com.github.hisahi.u020_toolchain.cpu.addressing.IAddressingMode;

public class InstructionSTI implements IInstruction {

    public InstructionSTI() {
    }

    @Override
    public int getCycles() {
        return 2;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        b.write(cpu, bm, a.read(cpu, am));
        cpu.increaseIJ();
    }

    @Override
    public String getName() {
        return "STI";
    }

}
