
package com.github.hisahi.u020toolchain.cpu.instructions; 

/**
 * A class for some math helper functions used by the instructions.
 * 
 * @author hisahi
 */
public class Operations {
    
    /**
     * Sign extends a 16-bit number into a 32-bit one.
     * 
     * @param x The 16-bit number to be sign-extended.
     * @return  The given number sign-extended to 32 bits.
     */
    public static int signExtend16To32(int x) {
        return (int) ((short) (x & 0xFFFF));
    }
    
    /**
     * Sign extends a 8-bit number into a 16-bit one.
     * 
     * @param x The 8-bit number to be sign-extended.
     * @return  The given number sign-extended to 16 bits.
     */
    public static int signExtend8To16(int x) {
        if (x >= 0x80) {
            return 0xff00 | x;
        } else {
            return x;
        }
    }
    
    private Operations() {}
}
