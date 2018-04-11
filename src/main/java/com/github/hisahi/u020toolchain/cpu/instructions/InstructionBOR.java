
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 BOR instruction that performs bitwise OR
 * on two numbers.
 * 
 * @author hisahi
 */
public class InstructionBOR implements IInstruction {

    @Override
    public int getCycles() {
        return 1;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = a.read(cpu, am);
        int ib = b.read(cpu, bm);
        b.write(cpu, bm, ib | ia);
    }

    @Override
    public String getName() {
        return "BOR";
    }

}
