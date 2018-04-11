
package com.github.hisahi.u020toolchain.hardware; 

import com.github.hisahi.u020toolchain.cpu.Register;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.logic.ITickable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Implements the UNTM200 peripheral that behaves as a
 * high resolution timer for the machine, ticking at a specified
 * rate given in CPU cycles.
 * 
 * @author hisahi
 */
public class UNTM200 extends Hardware implements ITickable {
    private boolean on;
    private long interval;
    private long progress;
    private int counter;
    private int intmsg;

    /**
     * Initializes a new UNTM200 instance.
     * 
     * @param cpu The UCPU16 instance.
     */
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
        switch (cpu.readRegister(Register.A)) {
            case 0: 
            {
                int b = cpu.readRegister(Register.B);
                int c = cpu.readRegister(Register.C);
                if (b == 0 && c == 0) {
                    this.on = false;
                } else {
                    this.interval = (c << 16) + b;
                    this.on = true;
                    this.counter = 0;
                    this.progress = 0;
                }
                break;
            }
            case 1:
                cpu.writeRegister(Register.C, this.counter);
                this.counter = 0;
                break;
            case 2: 
                this.intmsg = cpu.readRegister(Register.B);
                break;
            case 3:
                cpu.writeRegister(Register.B, (int) (this.interval & 0xFFFF));
                cpu.writeRegister(Register.C, (int) ((this.interval >> 16) & 0xFFFF));
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
    public void pause() {
    }

    @Override
    public void resume() {
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
        if (!this.on) {
            return;
        }
        ++this.progress;
        if (this.progress >= this.interval) {
            this.progress = 0;
            this.counter = (this.counter + 1) & 0xFFFF;
            if (this.intmsg != 0) {
                this.cpu.queueInterrupt(this.intmsg);
            }
        }
    }
}
