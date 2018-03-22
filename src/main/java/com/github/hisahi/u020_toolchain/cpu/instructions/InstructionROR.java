
package com.github.hisahi.u020_toolchain.cpu.instructions; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;
import com.github.hisahi.u020_toolchain.cpu.addressing.IAddressingMode;

public class InstructionROR implements IInstruction {

    public InstructionROR() {
    }

    @Override
    public int getCycles() {
        return 2;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = a.read(cpu, am);
        int ib = b.read(cpu, bm);
        int ex = cpu.getEX();
        cpu.setEX(((ib << 16) >> ia) & 0xFFFF);
        b.write(cpu, bm, ((ib >>> ia) | ex) & 0xFFFF);
    }

}
