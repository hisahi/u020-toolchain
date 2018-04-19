
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 DBG instruction that requests a debugger.
 * 
 * @author hisahi
 */
public class InstructionDBG implements IInstruction {

    @Override
    public int getCycles() {
        return 1;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        cpu.debugger("DBG instruction");
        cpu.pause();
    }

    @Override
    public String getName() {
        return "DBG!";
    }

}
