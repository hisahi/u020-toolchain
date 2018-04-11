
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 HWN instruction that reports the total number
 * of connected hardware peripherals.
 * 
 * @author hisahi
 */
public class InstructionHWN implements IInstruction {

    @Override
    public int getCycles() {
        return 2;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        a.write(cpu, am, cpu.getDevices().size());
    }

    @Override
    public String getName() {
        return "HWN";
    }

}
