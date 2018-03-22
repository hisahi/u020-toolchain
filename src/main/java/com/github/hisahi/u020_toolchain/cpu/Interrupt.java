
package com.github.hisahi.u020_toolchain.cpu; 

public class Interrupt {
    private int message;
    public Interrupt(int message) {
        this.message = message;
    }
    public int getInterruptMessage() {
        return this.message;
    }
}
