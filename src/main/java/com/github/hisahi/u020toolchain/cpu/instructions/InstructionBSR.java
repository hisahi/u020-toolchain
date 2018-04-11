
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 BSR instruction that behaves like JSR;
 * but performs a relative jump rather than an absolute one. This
 * instruction is primarily designed for position independent code.
 * 
 * @author hisahi
 */
public class InstructionBSR implements IInstruction {

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
