
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

public class InstructionSTD implements IInstruction {

    public InstructionSTD() {
    }

    @Override
    public int getCycles() {
        return 2;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        b.write(cpu, bm, a.read(cpu, am));
        cpu.decreaseIJ();
    }

    @Override
    public String getName() {
        return "STD";
    }

}
