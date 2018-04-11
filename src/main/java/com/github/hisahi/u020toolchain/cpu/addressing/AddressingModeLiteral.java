
package com.github.hisahi.u020toolchain.cpu.addressing; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;

/**
 * Implements the UCPU-16 literal addressing mode, in which the accesses
 * are targeted at a small literal in the range -1..30. 
 * 
 * Writes will silently fail.
 * 
 * @author hisahi
 */
public class AddressingModeLiteral implements IAddressingMode {

    private int val;
    
    /**
     * Initializes a new AddressingModeLiteral instance.
     * 
     * @param i The value this literal addressing mode represents.
     */
    public AddressingModeLiteral(int i) {
        this.val = i;
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
        return this.val & 0xFFFF;
    }

    @Override
    public void write(UCPU16 cpu, int addr, int val) {
    }

    @Override
    public String format(boolean isB, int val, String label) {
        if (label != null) {
            return label;
        } else {
            if (this.val < 0) {
                return String.format("0x%04x", this.val & 0xFFFF);
            }
            return String.valueOf(this.val);
        }
    }

}
