
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 MLI instruction that multiplies two signed
 * numbers. The bits that won't fit in the result are stored in EX.
 * 
 * @author hisahi
 */
public class InstructionMLI implements IInstruction {

    @Override
    public int getCycles() {
        return 2;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = Operations.signExtend16To32(a.read(cpu, am));
        int ib = Operations.signExtend16To32(b.read(cpu, bm));
        cpu.setEX(((ib * ia) >> 16) & 0xFFFF);
        b.write(cpu, bm, (ib * ia) & 0xFFFF);
    }

    @Override
    public String getName() {
        return "MLI";
    }

}
