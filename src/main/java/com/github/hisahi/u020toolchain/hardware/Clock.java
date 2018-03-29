
package com.github.hisahi.u020toolchain.hardware; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Clock extends Hardware implements ITickable {
    
    private boolean on;
    private long interval;
    private long lastTick;
    private int counter;
    private int intmsg;

    public Clock(UCPU16 cpu) {
        super(cpu);
        this.reset();
    }
    
    @Override
    public long hardwareId() {
        return 0x12d0b402;
    }

    @Override
    public int hardwareVersion() {
        return 1;
    }

    @Override
    public long hardwareManufacturer() {
        return 0x55AA55AAL;
    }

    @Override
    public void hwi(UCPU16 cpu) {
        switch (cpu.readRegister(UCPU16.REG_A)) {
            case 0: 
            {
                int b = cpu.readRegister(UCPU16.REG_B);
                if (b == 0) {
                    this.on = false;
                } else {
                    this.interval = Math.round(60000000000. / b);
                    this.on = true;
                    this.counter = 0;
                    this.lastTick = System.nanoTime();
                }
                break;
            }
            case 1:
                cpu.writeRegister(UCPU16.REG_C, this.counter);
                this.counter = 0;
                break;
            case 2: 
                this.intmsg = cpu.readRegister(UCPU16.REG_B);
                break;
        }
    }

    @Override
    public void reset() {
        this.on = false;
        this.interval = this.lastTick = 0L;
        this.counter = 0;
    }

    @Override
    public void saveState(DataOutputStream stream) throws IOException {
        stream.write(on ? 1 : 0);
        stream.writeInt(counter);
        stream.writeLong(interval);
        stream.writeLong(System.nanoTime() - lastTick);
    }

    @Override
    public void restoreState(DataInputStream stream) throws IOException {
        this.reset();
        this.on = stream.read() > 0;
        this.counter = stream.readInt();
        this.interval = stream.readLong();
        this.lastTick = System.nanoTime() - stream.readLong();
    }

    @Override
    public void tick() {
        if (!this.on) {
            return;
        }
        long now = System.nanoTime();
        while (now - lastTick >= interval) {
            ++counter;
            lastTick += interval;
            if (this.intmsg != 0) {
                this.cpu.queueInterrupt(this.intmsg);
            }
        }
        counter = counter & 0xFFFF;
    }

}
