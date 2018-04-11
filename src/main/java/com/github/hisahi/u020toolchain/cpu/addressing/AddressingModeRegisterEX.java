
package com.github.hisahi.u020toolchain.cpu.addressing; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;

/**
 * Implements the UCPU-16 register addressing mode, in which the accesses
 * are targeted at the EX register used primarily to store leftover bits of
 * results too large to fit into the primary result register.
 * 
 * @author hisahi
 */
public class AddressingModeRegisterEX implements IAddressingMode {

    /**
     * Initializes a new AddressingModeRegisterEX instance.
     */
    public AddressingModeRegisterEX() {
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
        return cpu.getEX();
    }

    @Override
    public void write(UCPU16 cpu, int addr, int val) {
        cpu.setEX(val);
    }

    @Override
    public String format(boolean isB, int val, String label) {
        return "EX";
    }

}
