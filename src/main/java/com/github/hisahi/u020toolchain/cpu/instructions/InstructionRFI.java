
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.Register;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 RFI instruction that disables interrupt queueing,
 * pops A from the stack and pops PC from the stack in a single atomic
 * instruction that can be used to return from interrupt handlers.
 * 
 * @author hisahi
 */
public class InstructionRFI implements IInstruction {

    @Override
    public int getCycles() {
        return 3;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        cpu.setInterruptQueueingEnabled(false);
        cpu.writeRegister(Register.A, cpu.stackPop());
        cpu.setPC(cpu.stackPop());
    }

    @Override
    public String getName() {
        return "RFI";
    }

}
