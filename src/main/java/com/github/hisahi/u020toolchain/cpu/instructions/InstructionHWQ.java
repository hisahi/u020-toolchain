
package com.github.hisahi.u020toolchain.cpu.instructions; 

import com.github.hisahi.u020toolchain.cpu.Register;
import com.github.hisahi.u020toolchain.hardware.Hardware;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;

/**
 * Implements the UCPU-16 HWQ instruction that returns the hardware ID,
 * version and manufacturer information of a connected peripheral.
 * 
 * @author hisahi
 */
public class InstructionHWQ implements IInstruction {

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
        cpu.writeRegister(Register.A, device.getIdLow());
        cpu.writeRegister(Register.B, device.getIdHigh());
        cpu.writeRegister(Register.C, device.getVersion());
        cpu.writeRegister(Register.X, device.getManufacturerLow());
        cpu.writeRegister(Register.Y, device.getManufacturerHigh());
    }

    @Override
    public String getName() {
        return "HWQ";
    }

}
