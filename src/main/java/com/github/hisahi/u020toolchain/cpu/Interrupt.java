
package com.github.hisahi.u020toolchain.cpu; 

/**
 * represents an UCPU-16 interrupt.
 * 
 * @author hisahi
 */
public class Interrupt {
    private int message;
    
    /**
     * Initializes a new Interrupt instance.
     * 
     * @param message The interrupt message. Only the low 16 bits
     *                of this parameter are taken into account.
     */
    public Interrupt(int message) {
        this.message = message & 0xFFFF;
    }
    
    /**
     * Returns the interrupt message of this interrupt.
     * 
     * @return     The interrupt message.
     */
    public int getInterruptMessage() {
        return this.message;
    }
}
