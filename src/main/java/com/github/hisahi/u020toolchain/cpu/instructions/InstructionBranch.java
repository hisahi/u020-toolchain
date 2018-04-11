
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * All IF_ instructions (conditional instructions, or branches) must
 * extend this class. The extending classes only need to implement
 * getName and whether to take the branch in a given situation.
 * 
 * @author hisahi
 */
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
    
    /**
     * Whether to take the branch.
     * 
     * @param cpu   The UCPU16 instance.
     * @param a     The second parameter of a binary instruction as an addressing mode,
     *              or the only parameter of an unary instruction.
     * @param b     The first parameter of a binary instruction as an addressing mode.
     * @param am    The additional argument for a.
     * @param bm    The additional argument for b.
     * @return      Whether to take the branch. If false, the following instruction
     *              (or instructions, as additional immediately following branch
     *              instructions and their final target non-branch instruction)
     *              will be skipped.
     */
    public abstract boolean take(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm);

}
