
package com.github.hisahi.u020_toolchain.cpu; 

public abstract class Hardware {
    private UCPU16 cpu;
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
    protected abstract long hardwareId();
    protected abstract int hardwareVersion();
    protected abstract long hardwareManufacturer();
    public abstract void hwi(UCPU16 cpu);
    public abstract void tick();
}
