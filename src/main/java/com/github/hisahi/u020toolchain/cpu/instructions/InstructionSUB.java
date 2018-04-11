
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 SUB instruction that subtracts two numbers.
 * The bits that won't fit in the result are stored in EX.
 * 
 * @author hisahi
 */
public class InstructionSUB implements IInstruction {

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
