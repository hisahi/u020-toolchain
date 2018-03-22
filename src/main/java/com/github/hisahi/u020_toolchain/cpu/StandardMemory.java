
package com.github.hisahi.u020_toolchain.cpu; 

public class StandardMemory {
    private final static int MEMORY_SIZE = 65536;
    int[] data;
    public StandardMemory() {
        this.data = new int[MEMORY_SIZE];
    }
    public int read(int address) {
        return data[address & (MEMORY_SIZE - 1)];
    }
    public void write(int address, int val) {
        data[address & (MEMORY_SIZE - 1)] = val;
    }
}
