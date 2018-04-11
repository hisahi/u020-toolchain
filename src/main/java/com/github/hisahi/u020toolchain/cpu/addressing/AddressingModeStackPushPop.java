
package com.github.hisahi.u020toolchain.cpu.addressing; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;

/**
 * Implements the UCPU-16 stack push / pop addressing mode.
 * 
 * Reads will read the word in memory currently pointed to by the stack 
 * pointer register (SP) and then increases this register.
 * 
 * Writes will decrease the stack pointer (SP) register and write the word
 * at the memory location pointed to by the new value of the SP register.
 * 
 * @author hisahi
 */
public class AddressingModeStackPushPop implements IAddressingMode {

    /**
     * Initializes a new AddressingModeStackPushPop instance.
     */
    public AddressingModeStackPushPop() {
    }

    @Override
    public boolean takesNextWord() {
        return false;
    }

    @Override
    public int getCycles() {
        return 0;
    }

    @Override
    public int read(UCPU16 cpu, int addr) {
        return cpu.stackPop();
    }

    @Override
    public void write(UCPU16 cpu, int addr, int val) {
        cpu.stackPush(val);
    }

    @Override
    public String format(boolean isB, int val, String label) {
        return isB ? "PUSH" : "POP";
    }

}
