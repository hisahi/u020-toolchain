
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 SET instruction that copies the value of the
 * second parameter into the first parameter.
 * 
 * @author hisahi
 */
public class InstructionSET implements IInstruction {

    @Override
    public int getCycles() {
        return 1;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        b.write(cpu, bm, a.read(cpu, am));
    }

    @Override
    public String getName() {
        return "SET";
    }

}
