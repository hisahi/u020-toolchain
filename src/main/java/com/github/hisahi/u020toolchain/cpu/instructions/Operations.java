
package com.github.hisahi.u020toolchain.cpu.instructions; 

public class Operations {
    public static int signExtend16To32(int x) {
        return (int) ((short) (x & 0xFFFF));
    }
    public static int signExtend8To16(int x) {
        if (x >= 0x80) {
            return 0xff00 | x;
        } else {
            return x;
        }
    }
    private Operations() {}
}
