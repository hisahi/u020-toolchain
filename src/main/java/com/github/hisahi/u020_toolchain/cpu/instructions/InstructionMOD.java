
package com.github.hisahi.u020_toolchain.cpu.instructions; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;
import com.github.hisahi.u020_toolchain.cpu.addressing.IAddressingMode;

public class InstructionMOD implements IInstruction {

    public InstructionMOD() {
    }

    @Override
    public int getCycles() {
        return 3;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = a.read(cpu, am);
        int ib = b.read(cpu, bm);
        if (ia == 0) {
            b.write(cpu, bm, 0);
            return;
        }
        b.write(cpu, bm, (ib % ia) & 0xFFFF);
    }

}
