
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 JSR instruction that performs s subroutine call
 * to an absolute address. The current value of the PC register is pushed
 * into the stack and then an absolute jump is performed, in which the PC
 * is set to the value of the parameter.
 * 
 * @author hisahi
 */
public class InstructionJSR implements IInstruction {

    @Override
    public int getCycles() {
        return 3;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = a.read(cpu, am);
        cpu.stackPush(cpu.getPC());
        cpu.setPC(ia);
    }

    @Override
    public String getName() {
        return "JSR";
    }

}
