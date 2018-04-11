
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 IAG instruction that reads the interrupt
 * handler address (IA) register.
 * 
 * @author hisahi
 */
public class InstructionIAG implements IInstruction {

    @Override
    public int getCycles() {
        return 1;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        a.write(cpu, am, cpu.getIA());
    }

    @Override
    public String getName() {
        return "IAG";
    }

}
