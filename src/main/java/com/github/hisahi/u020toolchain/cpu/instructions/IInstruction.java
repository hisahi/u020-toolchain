
package com.github.hisahi.u020toolchain.cpu.instructions;

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * All UCPU-16 instruction classes must extend this interface.
 * 
 * @author hisahi
 */
public interface IInstruction {
    /**
     * Returns how many cycles this instruction needs to execute.
     * 
     * @return      The number of cycles this instruction takes.
     */
    public int getCycles();
    
    /**
     * Executes the instruction under a given environment.
     * 
     * @param cpu   The UCPU16 instance.
     * @param a     The second parameter of a binary instruction as an addressing mode,
     *              or the only parameter of an unary instruction.
     * @param b     The first parameter of a binary instruction as an addressing mode.
     * @param am    The additional argument for a.
     * @param bm    The additional argument for b.
     */
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm);
    
    /**
     * Returns the symbolic name of this operation for the debugger
     * and disassembler.
     * 
     * @return      The symbolic name of this instruction operation.
     */
    public String getName();
}
