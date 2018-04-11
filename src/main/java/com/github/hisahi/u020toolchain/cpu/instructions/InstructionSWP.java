
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 SWP instruction that swaps the low and
 * high bytes of the parameter word.
 * 
 * @author hisahi
 */
public class InstructionSWP implements IInstruction {

    @Override
    public int getCycles() {
        return 1;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = a.read(cpu, am);
        a.write(cpu, am, (((ia << 8) & 0xFF00) | (ia >> 8)) & 0xFFFF);
    }

    @Override
    public String getName() {
        return "SWP";
    }

}
