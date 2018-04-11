
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 SHR instruction that performs a logical
 * shift right on the first parameter the number of times specified by
 * the second parameter. Bits shifted over are stored in EX.
 * 
 * @author hisahi
 */
public class InstructionSHR implements IInstruction {

    @Override
    public int getCycles() {
        return 1;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = a.read(cpu, am);
        int ib = b.read(cpu, bm);
        cpu.setEX(((ib << 16) >> ia) & 0xFFFF);
        b.write(cpu, bm, (ib >>> ia) & 0xFFFF);
    }

    @Override
    public String getName() {
        return "SHR";
    }

}
