
package com.github.hisahi.u020toolchain.logic; 

/**
 * Represents a single machine code instruction with a disassembled
 * assembly instruction as well as the address the instruction is located
 * in.
 * 
 * @author hisahi
 */
public class AssemblyListing {
    public int pos;
    public int[] hex;
    public String code;
    
    /**
     * Initializes a new AssemblyListing instance.
     * 
     * @param pos  The address of the instruction.
     * @param hex  The instruction in binary machine code form.
     * @param code The instruction in symbolic assembly form.
     */
    public AssemblyListing(int pos, int[] hex, String code) {
        this.pos = pos;
        this.hex = hex;
        this.code = code;
    }
    
    /**
     * Returns the address of the instruction.
     * 
     * @return     The address of the instruction in memory.
     */
    public int getPos() {
        return this.pos;
    }
    
    /**
     * Returns the binary machine code representation of the instruction.
     * 
     * @return     The instruction in binary machine code form.
     */
    public int[] getHex() {
        return this.hex;
    }
    
    /**
     * Returns the symbolic assembly representation of the instruction.
     * 
     * @return     The instruction in symbolic assembly form.
     */
    public String getCode() {
        return this.code;
    }
    
    /**
     * Returns a formatted version of this instruction for listings. The
     * resulting string will contain the address as well as the binary
     * and symbolic representations of this instruction.
     * 
     * @return     The formatted instruction listing.
     */
    public String formatted() {
        StringBuilder hexb = new StringBuilder("");
        for (int i = 0; i < Math.min(5, hex.length); ++i) {
            hexb.append(String.format("%04x", (hex[i] & 0xFFFF)));
            hexb.append(" ");
        }
        String hexf;
        if (hexb.length() > 15) {
            hexf = hexb.toString().substring(0, 15) + "...";
        } else {
            hexf = hexb.toString();
        }
        return String.format("%04x    %-20s    %s", (pos & 0xFFFF), hexf, code);
    }
}
