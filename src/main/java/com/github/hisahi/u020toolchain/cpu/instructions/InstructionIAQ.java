
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 IAQ instruction that either enables
 * or disables interrupt queueing based on the parameter.
 * 
 * @author hisahi
 */
public class InstructionIAQ implements IInstruction {

    @Override
    public int getCycles() {
        return 2;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        cpu.setInterruptQueueingEnabled(a.read(cpu, am) != 0);
    }

    @Override
    public String getName() {
        return "IAQ";
    }

}
