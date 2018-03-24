
package com.github.hisahi.u020_toolchain.cpu.addressing; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;

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
    public String format(boolean is_B, int val) {
        return String.valueOf(this.val);
    }

}
