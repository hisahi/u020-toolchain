
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

public class InstructionIFU extends InstructionBranch {

    @Override
    public boolean take(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = Operations.signExtend16To32(a.read(cpu, am));
        int ib = Operations.signExtend16To32(b.read(cpu, bm));
        return ib < ia;
    }

    @Override
    public String getName() {
        return "IFU";
    }

}
