
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 INT instruction that triggers an
 * software interrupt.
 * 
 * @author hisahi
 */
public class InstructionINT implements IInstruction {

    @Override
    public int getCycles() {
        return 4;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        cpu.queueInterrupt(a.read(cpu, am));
    }

    @Override
    public String getName() {
        return "INT";
    }

}
