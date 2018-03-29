
package com.github.hisahi.u020toolchain.hardware; 

import com.github.hisahi.u020toolchain.cpu.StandardMemory;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.ui.EmulatorMain;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class M35FD extends Hardware implements ITickable {
    
    private static final int CYCLES_PER_WORD = 65; // 2 MHz / 65 ~= 30.7 KW/s
    private static final int CYCLES_PER_TRACK = 4800; // 4800 / 2 MHz ~= 2.4 ms
    private static final int SECTOR_COUNT = 1440;
    private static final int WORDS_PER_SECTOR = 512;
    private static final int SECTORS_PER_TRACK = 18;
    private static final int DISK_SIZE = 737280; // 737280 KW
    private int driveid;
    private int track;
    private int seektotrack;
    private int sector;
    private int seektosector; // unused
    private boolean inserted;
    private int[] disk;
    private boolean writeProtected;
    private boolean operationRunning;
    private boolean operationIsWrite;
    private int wordsLeft;
    private int cyclesLeft;
    private int memaddr;
    private int diskaddr;
    private int state;
    private int error;
    private int intmsg;
    private int diskid;
    private int opdiskid;
    private EmulatorMain main;
    
    public M35FD(UCPU16 cpu, int driveid) {
        super(cpu);
        this.driveid = driveid;
        this.inserted = this.writeProtected = false;
        this.disk = new int[DISK_SIZE];
        this.diskid = this.opdiskid = 0;
        this.main = null;
        this.reset();
    }

    @Override
    public long hardwareId() {
        return 0x4fd524c5 + driveid;
    }

    @Override
    public int hardwareVersion() {
        return 0x000b;
    }

    @Override
    public long hardwareManufacturer() {
        return 0x1eb37e91;
    }

    @Override
    public void hwi(UCPU16 cpu) {
        switch (cpu.readRegister(UCPU16.REG_A)) {
            case 0:
                cpu.writeRegister(UCPU16.REG_B, state);
                cpu.writeRegister(UCPU16.REG_C, error);
                error = ERROR_NONE;
                break;
            case 1:
                this.intmsg = cpu.readRegister(UCPU16.REG_B);
                break;
            case 2: 
            {
                opdiskid = diskid;
                cpu.writeRegister(UCPU16.REG_B, 0);
                if (this.operationRunning) { // busy
                    this.setError(ERROR_BUSY);
                    break;
                } else if (!this.operationRunning) { // R/W but no disk
                    this.setError(ERROR_NO_MEDIA);
                    break;
                }
                int x = cpu.readRegister(UCPU16.REG_X);
                int y = cpu.readRegister(UCPU16.REG_Y);
                if (x >= SECTOR_COUNT) {
                    this.setError(ERROR_BAD_SECTOR);
                    break;
                }
                seektotrack = x / SECTORS_PER_TRACK;
                diskaddr = x * WORDS_PER_SECTOR;
                memaddr = cpu.readRegister(UCPU16.REG_Y);
                cpu.writeRegister(UCPU16.REG_B, 1);
                this.operationIsWrite = false;
                this.setStateAndError(STATE_BUSY, ERROR_NONE);
                this.operationRunning = true;
                break;
            }
            case 3: 
            {
                opdiskid = diskid;
                cpu.writeRegister(UCPU16.REG_B, 0);
                if (this.operationRunning) { // busy
                    this.setError(ERROR_BUSY);
                    break;
                } else if (this.writeProtected) { // write but wp
                    this.setError(ERROR_PROTECTED);
                    break;
                } else if (!this.operationRunning) { // R/W but no disk
                    this.setError(ERROR_NO_MEDIA);
                    break;
                } 
                int x = cpu.readRegister(UCPU16.REG_X);
                int y = cpu.readRegister(UCPU16.REG_Y);
                if (x >= SECTOR_COUNT) {
                    this.setError(ERROR_BAD_SECTOR);
                    break;
                }
                seektotrack = x / SECTORS_PER_TRACK;
                diskaddr = x * WORDS_PER_SECTOR;
                memaddr = cpu.readRegister(UCPU16.REG_Y);
                cpu.writeRegister(UCPU16.REG_B, 1);
                this.operationIsWrite = true;
                this.setStateAndError(STATE_BUSY, ERROR_NONE);
                this.operationRunning = true;
                break;
            }
        }
    }

    public void insert(int[] data) {
        if (data.length != DISK_SIZE) {
            throw new IllegalArgumentException("disk must be exactly 720 KW");
        }
        this.inserted = true;
        this.disk = Arrays.copyOf(data, DISK_SIZE);
        this.diskid = (this.diskid + 1) & 65535;
        this.setState(getValidDiskState());
    }

    public void eject() {
        this.writeBack();
        this.inserted = false;
        this.diskid = -1;
        this.setState(getValidDiskState());
    }

    private void writeBack() {
        if (this.main != null) {
            this.main.writeBack(this, this.diskid);
        }
    }

    public void setWriteBackTarget(EmulatorMain main) {
        this.main = main;
    }
    
    public void setWriteProtected(boolean wp) {
        this.writeProtected = wp;
        if (this.inserted) {
            this.setState(this.getValidDiskState());
        }
    }

    private int getValidDiskState() {
        return this.inserted ? (this.writeProtected ? STATE_READY_WP : STATE_READY) : STATE_NO_MEDIA;
    }
    
    @Override
    public void reset() {
        this.operationRunning = this.operationIsWrite = false;
        this.track = this.sector = this.wordsLeft = this.cyclesLeft = 
                this.intmsg = this.memaddr = this.diskaddr = 
                this.state = this.error = 0;
    }
    
    public void setState(int state) {
        this.state = state;
        if (this.intmsg != 0) {
            cpu.queueInterrupt(this.intmsg);
        }
    }
    
    public void setError(int error) {
        this.error = error;
        if (this.intmsg != 0) {
            cpu.queueInterrupt(this.intmsg);
        }
    }
    
    public void setStateAndError(int state, int error) {
        this.state = state;
        this.error = error;
        if (this.intmsg != 0) {
            cpu.queueInterrupt(this.intmsg);
        }
    }

    @Override
    public void saveState(DataOutputStream stream) throws IOException {
        stream.write(driveid);
        stream.write(track);
        stream.write(seektotrack);
        stream.write(operationRunning ? 1 : 0);
        stream.write(operationIsWrite ? 1 : 0);
        stream.writeShort(sector);
        stream.writeShort(seektosector);
        stream.writeInt(memaddr);
        stream.writeInt(diskaddr);
        stream.writeInt(wordsLeft);
        stream.writeInt(cyclesLeft);
        stream.writeInt(state);
        stream.writeInt(error);
        stream.writeInt(intmsg);
        stream.writeInt(diskid);
        stream.writeInt(opdiskid);
        stream.write(inserted ? 1 : 0);
        if (inserted) {
            for (int i = 0; i < DISK_SIZE; ++i) {
                stream.writeShort(disk[i]);
            }
        }
    }

    @Override
    public void restoreState(DataInputStream stream) throws IOException {
        driveid = stream.read();
        track = stream.read();
        seektotrack = stream.read();
        operationRunning = stream.read() != 0;
        operationIsWrite = stream.read() != 0;
        sector = (int) stream.readShort() & 0xFFFF;
        seektosector = (int) stream.readShort() & 0xFFFF;
        memaddr = stream.readInt();
        diskaddr = stream.readInt();
        wordsLeft = stream.readInt();
        cyclesLeft = stream.readInt();
        state = stream.readInt();
        error = stream.readInt();
        intmsg = stream.readInt();
        diskid = stream.readInt();
        opdiskid = stream.readInt();
        inserted = stream.read() != 0;
        if (inserted) {
            for (int i = 0; i < DISK_SIZE; ++i) {
                disk[i] = (int) stream.readShort() & 0xFFFF;
            }
        }
    }

    @Override
    public void tick() {
        if (this.operationRunning) {
            --this.cyclesLeft;
            if (this.cyclesLeft <= 0) {
                // see if disk has been ejected
                if (this.diskid != this.opdiskid || !this.inserted) {
                    this.setStateAndError(getValidDiskState(), ERROR_EJECT);
                    this.operationRunning = false;
                    return;
                }
                if (this.writeProtected && this.operationIsWrite) {
                    this.setStateAndError(getValidDiskState(), ERROR_PROTECTED);
                    this.operationRunning = false;
                    return;
                }
                // seek
                if (this.track > this.seektotrack) {
                    --this.track;
                    this.cyclesLeft = CYCLES_PER_TRACK;
                    return;
                }
                if (this.track < this.seektotrack) {
                    ++this.track;
                    this.cyclesLeft = CYCLES_PER_TRACK;
                    return;
                }
                // read or write
                this.cyclesLeft = CYCLES_PER_WORD;
                --this.wordsLeft;
                if (this.operationIsWrite) {
                    this.disk[this.diskaddr++] = cpu.getMemory().read(this.memaddr++);
                } else {
                    cpu.getMemory().write(this.memaddr++, this.disk[this.diskaddr++]);
                }
                this.diskaddr %= DISK_SIZE;
                this.memaddr %= StandardMemory.MEMORY_SIZE;
                if (this.wordsLeft <= 0) {
                    this.setState(getValidDiskState());
                    this.operationRunning = false;
                    this.writeBack();
                }
            }
        }
    }

    public int getDriveId() {
        return this.driveid;
    }

    private static final int STATE_NO_MEDIA = 0;
    private static final int STATE_READY = 1;
    private static final int STATE_READY_WP = 2;
    private static final int STATE_BUSY = 3;
    
    private static final int ERROR_NONE = 0;
    private static final int ERROR_BUSY = 1;
    private static final int ERROR_NO_MEDIA = 2;
    private static final int ERROR_PROTECTED = 3;
    private static final int ERROR_EJECT = 4;
    private static final int ERROR_BAD_SECTOR = 5;
}
