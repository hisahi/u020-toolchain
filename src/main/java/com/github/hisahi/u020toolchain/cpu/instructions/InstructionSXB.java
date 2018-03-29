
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

public class InstructionSXB implements IInstruction {

    public InstructionSXB() {
    }

    @Override
    public int getCycles() {
        return 1;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = a.read(cpu, am);
        a.write(cpu, am, Operations.signExtend8To16(ia & 0xFF));
    }

    @Override
    public String getName() {
        return "SXB";
    }

}
