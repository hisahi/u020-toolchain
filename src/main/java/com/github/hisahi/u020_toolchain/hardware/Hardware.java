
package com.github.hisahi.u020_toolchain.hardware; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Hardware {
    protected UCPU16 cpu;
    public Hardware(UCPU16 cpu) {
        this.cpu = cpu;
    }
    public int getIdLow() {
        return (int) (hardwareId() & 0xFFFF);
    }
    public int getIdHigh() {
        return (int) ((hardwareId() >> 16) & 0xFFFF);
    }
    public int getVersion() {
        return hardwareVersion();
    }
    public int getManufacturerLow() {
        return (int) (hardwareManufacturer() & 0xFFFF);
    }
    public int getManufacturerHigh() {
        return (int) ((hardwareManufacturer() >> 16) & 0xFFFF);
    }
    public abstract long hardwareId();
    public abstract int hardwareVersion();
    public abstract long hardwareManufacturer();
    public abstract void hwi(UCPU16 cpu);
    public abstract void reset();
    public abstract void saveState(DataOutputStream stream) throws IOException;
    public abstract void restoreState(DataInputStream stream) throws IOException;
}
