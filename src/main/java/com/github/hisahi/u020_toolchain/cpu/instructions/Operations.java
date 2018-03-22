
package com.github.hisahi.u020_toolchain.cpu.instructions; 

public class Operations {
    public static int signExtend16_32(int x) {
        return (int) ((short) (x & 0xFFFF));
    }
    public static int signExtend8_16(int x) {
        if (x >= 0x80) {
            return 0xff00 | x;
        } else {
            return x;
        }
    }
    private Operations() {}
}
