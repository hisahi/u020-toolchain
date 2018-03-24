
package com.github.hisahi.u020_toolchain.hardware; 

import com.github.hisahi.u020_toolchain.cpu.UCPU16;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UNTM200 extends Hardware implements ITickable {
    private boolean on;
    private long interval;
    private long progress;
    private int counter;
    private int intmsg;

    public UNTM200(UCPU16 cpu) {
        super(cpu);
        this.reset();
    }

    @Override
    public long hardwareId() {
        return 0x8f1705a6L;
    }

    @Override
    public int hardwareVersion() {
        return 0xc801;
    }

    @Override
    public long hardwareManufacturer() {
        return 0x2590a31c;
    }

    @Override
    public void hwi(UCPU16 cpu) {
        switch (cpu.readRegister(UCPU16.REG_A)) {
        case 0: {
            int b = cpu.readRegister(UCPU16.REG_B);
            int c = cpu.readRegister(UCPU16.REG_C);
            if (b == 0 && c == 0) {
                this.on = false;
            } else {
                this.interval = (c << 16) + b;
                this.on = true;
                this.counter = 0;
                this.progress = 0;
            }
        }
            break;
        case 1:
            cpu.writeRegister(UCPU16.REG_C, this.counter);
            this.counter = 0;
            break;
        case 2: 
            this.intmsg = cpu.readRegister(UCPU16.REG_B);
            break;
        case 3:
            cpu.writeRegister(UCPU16.REG_B, this.counter & 0xFFFF);
            cpu.writeRegister(UCPU16.REG_C, (this.counter >> 16) & 0xFFFF);
            break;
        }
    }

    @Override
    public void reset() {
        this.on = false;
        this.interval = this.progress = 0L;
        this.counter = 0;
    }

    @Override
    public void saveState(DataOutputStream stream) throws IOException {
        stream.write(on ? 1 : 0);
        stream.writeInt(counter);
        stream.writeLong(interval);
        stream.writeLong(progress);
    }

    @Override
    public void restoreState(DataInputStream stream) throws IOException {
        this.reset();
        this.on = stream.read() > 0;
        this.counter = stream.readInt();
        this.interval = stream.readLong();
        this.progress = stream.readLong();
    }

    @Override
    public void tick() {
        if (!this.on) return;
        ++this.progress;
        if (this.progress >= this.interval) {
            this.progress = 0;
            if (this.intmsg != 0) {
                this.cpu.queueInterrupt(this.intmsg);
            }
        }
    }
}