
package com.github.hisahi.u020_toolchain.cpu.addressing;

import com.github.hisahi.u020_toolchain.cpu.UCPU16;

public interface IAddressingMode {
    public boolean takesNextWord();
    public int getCycles();
    public int read(UCPU16 cpu, int addr);
    public void write(UCPU16 cpu, int addr, int val);
}
