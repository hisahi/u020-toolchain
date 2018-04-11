
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 MUL instruction that multiplies two unsigned
 * numbers. The bits that won't fit in the result are stored in EX.
 * 
 * @author hisahi
 */
public class InstructionMUL implements IInstruction {

    @Override
    public int getCycles() {
        return 2;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = a.read(cpu, am);
        int ib = b.read(cpu, bm);
        cpu.setEX(((ib * ia) >> 16) & 0xFFFF);
        b.write(cpu, bm, (ib * ia) & 0xFFFF);
    }

    @Override
    public String getName() {
        return "MUL";
    }

}
