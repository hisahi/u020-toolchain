
package com.github.hisahi.u020toolchain.cpu.addressing;

import com.github.hisahi.u020toolchain.cpu.UCPU16;

/**
 * All UCPU-16 addressing mode classes must extend this interface.
 * 
 * @author hisahi
 */
public interface IAddressingMode {
    /**
     * Returns whether this addressing mode requires a parameter.
     * If the parameter is required, it is read from memory after
     * the main instruction word.
     * 
     * @return         Whether a parameter should be read.
     */
    public boolean takesNextWord();
    
    /**
     * Returns how many additional cycles this addressing mode requires 
     * for parsing.
     * 
     * @return         The number of cycles this addressing mode takes.
     *                 The cost is only taken once per instruction, not once
     *                 per access.
     */
    public int getCycles();
    
    /**
     * Reads a value.
     * 
     * @param cpu      The UCPU16 instance.
     * @param addr     The additional parameter, or 0 if the addressing
     *                 mode didn't request one.
     * @return         The read value.
     */
    public int read(UCPU16 cpu, int addr);
    
    /**
     * Writes a value.
     * 
     * @param cpu      The UCPU16 instance.
     * @param addr     The additional parameter, or 0 if the addressing
     *                 mode didn't request one.
     * @param val      The value to be written.
     */
    public void write(UCPU16 cpu, int addr, int val);
    
    /**
     * Formats this addressing mode into a string. This is used for
     * the debugger and disassembler.
     * 
     * @param isB      Whether this is the first parameter in an biary
     *                 instruction (B).
     * @param val      The additional parameter, or 0 if the addressing
     *                 mode doesn't require one.
     * @param label    If the addressing mode involves an additional
     *                 parameter, this may be the label that is being
     *                 referred to by the value. If no label is referenced,
     *                 this will be null.
     * @return         The addressing mode access formatted into a string.
     */
    public String format(boolean isB, int val, String label);
}
