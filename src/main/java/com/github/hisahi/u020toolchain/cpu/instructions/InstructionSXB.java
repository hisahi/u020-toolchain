
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 SXB instruction that sign-extends the low
 * byte of a word to the word.
 * 
 * @author hisahi
 */
public class InstructionSXB implements IInstruction {

    @Override
    public int getCycles() {
        return 1;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = a.read(cpu, am);
        a.write(cpu, am, Operations.signExtend8To16(ia & 0xFF));
    }

    @Override
    public String getName() {
        return "SXB";
    }

}
