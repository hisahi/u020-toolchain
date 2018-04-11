
package com.github.hisahi.u020toolchain.cpu.addressing; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;

/**
 * Implements the UCPU-16 stack pick addressing mode, in which the accesses
 * are targeted at the word at the top of the stack, or the word at the
 * address currently pointed to by the SP, or stack pointer, register, 
 * with the parameter word added to this value to decide the final address.
 * 
 * @author hisahi
 */
public class AddressingModeStackPick implements IAddressingMode {

    /**
     * Initializes a new AddressingModeStackPick instance.
     */
    public AddressingModeStackPick() {
    }

    @Override
    public boolean takesNextWord() {
        return true;
    }

    @Override
    public int getCycles() {
        return 1;
    }

    @Override
    public int read(UCPU16 cpu, int addr) {
        return cpu.getMemory().read((cpu.getSP() + addr) & 0xFFFF);
    }

    @Override
    public void write(UCPU16 cpu, int addr, int val) {
        cpu.getMemory().write((cpu.getSP() + addr) & 0xFFFF, val);
    }

    @Override
    public String format(boolean isB, int val, String label) {
        return "[SP+" + String.format("0x%04x", val) + "]";
    }

}
