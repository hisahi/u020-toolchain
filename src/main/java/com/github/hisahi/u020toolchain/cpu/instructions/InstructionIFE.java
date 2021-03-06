
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 IFE instruction that executes the following 
 * instruction only if the parameters equal each other.
 * 
 * If the condition is false, the following instruction is skipped.
 * If the following instruction is too a conditional branch, the instruction
 * after it is also skipped. This process is repeated as many times as there
 * are consecutive conditional branch instructions.
 * 
 * @author hisahi
 */
public class InstructionIFE extends InstructionBranch {

    @Override
    public boolean take(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = a.read(cpu, am);
        int ib = b.read(cpu, bm);
        return ia == ib;
    }

    @Override
    public String getName() {
        return "IFE";
    }

}
