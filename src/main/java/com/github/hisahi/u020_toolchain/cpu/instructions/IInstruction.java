
package com.github.hisahi.u020_toolchain.cpu.instructions;

import com.github.hisahi.u020_toolchain.cpu.UCPU16;
import com.github.hisahi.u020_toolchain.cpu.addressing.IAddressingMode;

public interface IInstruction {
    public int getCycles();
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm);
}
