
package com.github.hisahi.u020_toolchain.cpu.instructions; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;
import com.github.hisahi.u020_toolchain.cpu.addressing.IAddressingMode;

public class InstructionIFU extends InstructionBranch {

    @Override
    public boolean take(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = Operations.signExtend16_32(a.read(cpu, am));
        int ib = Operations.signExtend16_32(b.read(cpu, bm));
        return ib < ia;
    }

}
