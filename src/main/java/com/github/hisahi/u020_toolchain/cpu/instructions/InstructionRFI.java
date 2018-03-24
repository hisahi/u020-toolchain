
package com.github.hisahi.u020_toolchain.cpu.instructions; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;
import com.github.hisahi.u020_toolchain.cpu.addressing.IAddressingMode;

public class InstructionRFI implements IInstruction {

    public InstructionRFI() {
    }

    @Override
    public int getCycles() {
        return 3;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        cpu.setInterruptQueueingEnabled(false);
        cpu.writeRegister(0, cpu.stackPop());
        cpu.setPC(cpu.stackPop());
    }

    @Override
    public String getName() {
        return "RFI";
    }

}
