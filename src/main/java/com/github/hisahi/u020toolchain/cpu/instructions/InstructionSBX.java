
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 SBX instruction that subtracts two numbers and adds
 * the value of EX. The bits that won't fit in the result are stored in EX.
 * 
 * @author hisahi
 */
public class InstructionSBX implements IInstruction {

    @Override
    public int getCycles() {
        return 3;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = a.read(cpu, am);
        int ib = b.read(cpu, bm);
        int ex = Operations.signExtend16To32(cpu.getEX());
        cpu.setEX((ib - ia + ex) >> 16);
        b.write(cpu, bm, (ib - ia + ex) & 0xFFFF);
    }

    @Override
    public String getName() {
        return "SBX";
    }

}
