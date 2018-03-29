
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

public class InstructionASR implements IInstruction {

    public InstructionASR() {
    }

    @Override
    public int getCycles() {
        return 1;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = a.read(cpu, am);
        int ib = Operations.signExtend16To32(b.read(cpu, bm));
        cpu.setEX(((ib << 16) >>> ia) & 0xFFFF);
        b.write(cpu, bm, (ib >> ia) & 0xFFFF);
    }

    @Override
    public String getName() {
        return "ASR";
    }

}
