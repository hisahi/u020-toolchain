
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 IAS instruction that writes to the interrupt
 * handler address (IA) register.
 * 
 * @author hisahi
 */
public class InstructionIAS implements IInstruction {

    @Override
    public int getCycles() {
        return 1;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        cpu.setIA(a.read(cpu, am));
    }

    @Override
    public String getName() {
        return "IAS";
    }

}
