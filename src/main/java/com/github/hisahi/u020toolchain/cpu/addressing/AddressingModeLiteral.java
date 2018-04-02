
package com.github.hisahi.u020toolchain.cpu.addressing; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;

public class AddressingModeLiteral implements IAddressingMode {

    private int val;
    
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
            return String.valueOf(this.val);
        }
    }

}
