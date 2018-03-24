
package com.github.hisahi.u020_toolchain.cpu.instructions; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;
import com.github.hisahi.u020_toolchain.cpu.addressing.IAddressingMode;

public abstract class InstructionBranch implements IInstruction {

    @Override
    public int getCycles() {
        return 2;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        if (!take(cpu, a, b, am, bm)) {
            cpu.skipConditional();
        }
    }
    
    public abstract boolean take(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm);
    public abstract String getName();

}
