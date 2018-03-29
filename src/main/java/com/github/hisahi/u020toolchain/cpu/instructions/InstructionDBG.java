
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

public class InstructionDBG implements IInstruction {

    public InstructionDBG() {
    }

    @Override
    public int getCycles() {
        return 1;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        cpu.debugger("DBG instruction");
    }

    @Override
    public String getName() {
        return "DBG!";
    }

}
