
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.hardware.Hardware;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

public class InstructionHWQ implements IInstruction {

    public InstructionHWQ() {
    }

    @Override
    public int getCycles() {
        return 4;
    }

    @Override
    public void execute(UCPU16 cpu, IAddressingMode a, IAddressingMode b, int am, int bm) {
        int ia = a.read(cpu, am);
        if (ia >= cpu.getDevices().size()) {
            return;
        }
        Hardware device = cpu.getDevices().get(ia);
        cpu.writeRegister(0, device.getIdLow());
        cpu.writeRegister(1, device.getIdHigh());
        cpu.writeRegister(2, device.getVersion());
        cpu.writeRegister(3, device.getManufacturerLow());
        cpu.writeRegister(4, device.getManufacturerHigh());
    }

    @Override
    public String getName() {
        return "HWQ";
    }

}
