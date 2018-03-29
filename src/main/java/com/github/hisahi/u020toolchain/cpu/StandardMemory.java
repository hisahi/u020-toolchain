
package com.github.hisahi.u020toolchain.cpu; 

public class StandardMemory {
    public final static int MEMORY_SIZE = 65536;
    private final static int MEMORY_MASK = 65535;
    private int[] data;
    public StandardMemory() {
        this.data = new int[MEMORY_SIZE];
    }
    public int read(int address) {
        return data[address & MEMORY_MASK];
    }
    public void write(int address, int val) {
        data[address & MEMORY_MASK] = val;
    }
    public int[] array() {
        return data;
    }
}
