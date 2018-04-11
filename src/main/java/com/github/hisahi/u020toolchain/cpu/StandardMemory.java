
package com.github.hisahi.u020toolchain.cpu; 

/**
 * Represents the standard memory module of the Univtek 020 computer.
 * It has 64 kilowords (65 536) kilowords of capacity. The class
 * allows reading and writing word by word as well as direct access
 * to the underlying array for operations that require frequent
 * reading and writing.
 * 
 * @author hisahi
 */
public class StandardMemory {
    /**
     * Represents the size of the standard memory. This is the
     * length of the array returned by array().
     */
    public final static int MEMORY_SIZE = 65536;
    
    private final static int MEMORY_MASK = 65535;
    private int[] data;
    
    /**
     * Initializes a new StandardMemory instance.
     */
    public StandardMemory() {
        this.data = new int[MEMORY_SIZE];
    }
    
    /**
     * Reads a word from the memory.
     * 
     * @param address The address to read the memory at. Only the low
     *                16 bits of this parameter are taken into account.
     * @return        The value at the specified memory address.
     */
    public int read(int address) {
        return data[address & MEMORY_MASK];
    }
    
    /**
     * Writes a word into the memory.
     * 
     * @param address The address of memory into which to write. Only the low
     *                16 bits of this parameter are taken into account.
     * @param val     The value to be stored in the memory. Only the low
     *                16 bits of this parameter are taken into account.
     */
    public void write(int address, int val) {
        data[address & MEMORY_MASK] = val & 0xFFFF;
    }
    
    /**
     * Returns the direct array that this StandardMemory instance wraps
     * around. Using this class is useful if many accesses are necessary,
     * or if one needs to use System.arraycopy or a similar function.
     * 
     * @return        The underlying array used by this StandardMemory
     *                instance, the size of which is determined by
     *                MEMORY_SIZE.
     */
    public int[] array() {
        return data;
    }
}
