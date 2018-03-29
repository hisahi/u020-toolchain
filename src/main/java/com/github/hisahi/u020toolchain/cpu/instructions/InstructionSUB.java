
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

public class InstructionSUB implements IInstruction {

    public InstructionSUB() {
    }

    @Override
    public int getCycles() {
        return 2;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = a.read(cpu, am);
        int ib = b.read(cpu, bm);
        cpu.setEX((ib - ia) >> 16);
        b.write(cpu, bm, (ib - ia) & 0xFFFF);
    }

    @Override
    public String getName() {
        return "SUB";
    }

}
