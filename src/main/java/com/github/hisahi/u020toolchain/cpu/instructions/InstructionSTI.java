
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 STI instruction that behaves like SET,
 * but increments the I and J registers after the operation.
 * 
 * @author hisahi
 */
public class InstructionSTI implements IInstruction {

    @Override
    public int getCycles() {
        return 2;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        b.write(cpu, bm, a.read(cpu, am));
        cpu.increaseIJ();
    }

    @Override
    public String getName() {
        return "STI";
    }

}
