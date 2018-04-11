
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 ROL instruction that performs a logical
 * shift left on the first parameter the number of times specified by
 * the second parameter and performs a bitwise OR on the final value with
 * the old value of the EX register. Bits shifted over are stored in EX.
 * 
 * @author hisahi
 */
public class InstructionROL implements IInstruction {

    @Override
    public int getCycles() {
        return 2;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = a.read(cpu, am);
        int ib = b.read(cpu, bm);
        int ex = cpu.getEX();
        cpu.setEX(((ib << ia) >> 16) & 0xFFFF);
        b.write(cpu, bm, ((ib << ia) | ex) & 0xFFFF);
    }

    @Override
    public String getName() {
        return "ROL";
    }

}
