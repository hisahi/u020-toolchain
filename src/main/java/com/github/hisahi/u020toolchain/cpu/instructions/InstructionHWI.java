
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

public class InstructionHWI implements IInstruction {

    public InstructionHWI() {
    }

    @Override
    public int getCycles() {
        return 4;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = a.read(cpu, am);
        if (ia >= cpu.getDevices().size()) {
            return;
        }
        cpu.getDevices().get(ia).hwi(cpu);
    }

    @Override
    public String getName() {
        return "HWI";
    }

}
