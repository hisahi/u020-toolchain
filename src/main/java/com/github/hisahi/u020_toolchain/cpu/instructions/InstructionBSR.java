
package com.github.hisahi.u020_toolchain.cpu.instructions; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;
import com.github.hisahi.u020_toolchain.cpu.addressing.IAddressingMode;

public class InstructionBSR implements IInstruction {

    public InstructionBSR() {
    }

    @Override
    public int getCycles() {
        return 4;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = a.read(cpu, am);
        cpu.stackPush(cpu.getPC());
        cpu.setPC((cpu.getPC() + ia) & 0xFFFF);
    }

    @Override
    public String getName() {
        return "BSR";
    }

}
