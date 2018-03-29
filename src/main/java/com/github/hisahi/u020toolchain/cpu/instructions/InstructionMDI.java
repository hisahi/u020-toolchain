
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

public class InstructionMDI implements IInstruction {

    public InstructionMDI() {
    }

    @Override
    public int getCycles() {
        return 3;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = Operations.signExtend16To32(a.read(cpu, am));
        int ib = Operations.signExtend16To32(b.read(cpu, bm));
        if (ia == 0) {
            b.write(cpu, bm, 0);
            return;
        }
        b.write(cpu, bm, (ib % ia) & 0xFFFF);
    }

    @Override
    public String getName() {
        return "MDI";
    }

}
