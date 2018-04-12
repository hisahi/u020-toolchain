
package com.github.hisahi.u020toolchain.hardware; 

import com.github.hisahi.u020toolchain.cpu.Register;
import com.github.hisahi.u020toolchain.cpu.StandardMemory;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.logic.ITickable;
import com.github.hisahi.u020toolchain.ui.EmulatorMain;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Implements the M35FD peripheral that serves as the main floppy
 * peripheral for Univtek 020.
 * 
 * @author hisahi
 */
public class M35FD extends Hardware implements ITickable {
    
    private static final int NS_PER_WORD = 32573; // 1 s / 32573 ns ~= 30.7 KW/s
    private static final int NS_PER_TRACK = 2400000; // 2.4 ms
    private static final int SECTOR_COUNT = 1440;
    private static final int WORDS_PER_SECTOR = 512;
    private static final int SECTORS_PER_TRACK = 18;
    public static final int DISK_SIZE = 737280; // 737280 KW
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
    private long nextTick;
    private int wordsLeft;
    private int memaddr;
    private int diskaddr;
    private int state;
    private int error;
    private int intmsg;
    private int diskid;
    private int opdiskid;
    private EmulatorMain main;
    
    /**
     * Initializes a new M35FD instance.
     * 
     * @param cpu     The UCPU16 instance.
     * @param driveid The drive number.
     */
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
        switch (cpu.readRegister(Register.A)) {
            case 0:
                cpu.writeRegister(Register.B, state);
                cpu.writeRegister(Register.C, error);
                error = ERROR_NONE;
                break;
            case 1:
                this.intmsg = cpu.readRegister(Register.B);
                break;
            case 2: 
            {
                opdiskid = diskid;
                cpu.writeRegister(Register.B, 0);
                if (this.operationRunning) { // busy
                    this.setError(ERROR_BUSY);
                    break;
                } else if (!this.inserted) { // R/W but no disk
                    this.setError(ERROR_NO_MEDIA);
                    break;
                }
                int x = cpu.readRegister(Register.X);
                int y = cpu.readRegister(Register.Y);
                if (x >= SECTOR_COUNT) {
                    this.setError(ERROR_BAD_SECTOR);
                    break;
                }
                seektotrack = x / SECTORS_PER_TRACK;
                diskaddr = x * WORDS_PER_SECTOR;
                memaddr = cpu.readRegister(Register.Y);
                cpu.writeRegister(Register.B, 1);
                this.operationIsWrite = false;
                this.wordsLeft = WORDS_PER_SECTOR;
                this.nextTick = System.nanoTime();
                this.setStateAndError(STATE_BUSY, ERROR_NONE);
                this.operationRunning = true;
                break;
            }
            case 3: 
            {
                opdiskid = diskid;
                cpu.writeRegister(Register.B, 0);
                if (this.operationRunning) { // busy
                    this.setError(ERROR_BUSY);
                    break;
                } else if (this.writeProtected) { // write but wp
                    this.setError(ERROR_PROTECTED);
                    break;
                } else if (!this.inserted) { // R/W but no disk
                    this.setError(ERROR_NO_MEDIA);
                    break;
                } 
                int x = cpu.readRegister(Register.X);
                int y = cpu.readRegister(Register.Y);
                if (x >= SECTOR_COUNT) {
                    this.setError(ERROR_BAD_SECTOR);
                    break;
                }
                seektotrack = x / SECTORS_PER_TRACK;
                diskaddr = x * WORDS_PER_SECTOR;
                memaddr = cpu.readRegister(Register.Y);
                cpu.writeRegister(Register.B, 1);
                this.operationIsWrite = true;
                this.wordsLeft = WORDS_PER_SECTOR;
                this.nextTick = System.nanoTime();
                this.setStateAndError(STATE_BUSY, ERROR_NONE);
                this.operationRunning = true;
                break;
            }
        }
    }

    /**
     * Inserts a disk into the drive.
     * 
     * @param data The disk image to be inserted.
     */
    public void insert(int[] data) {
        if (data.length != DISK_SIZE) {
            throw new IllegalArgumentException("disk must be exactly 720 KW");
        }
        this.inserted = true;
        this.disk = Arrays.copyOf(data, DISK_SIZE);
        this.diskid = (this.diskid + 1) & 65535;
        this.setState(getValidDiskState());
    }

    /**
     * Ejects the disk from the drive.
     */
    public void eject() {
        this.writeBack();
        this.inserted = false;
        this.diskid = -1;
        this.setState(getValidDiskState());
    }

    private void writeBack() {
        if (this.main != null) {
            this.main.writeBack(this);
        }
    }

    /**
     * Sets the EmulatorMain instance for this drive. This is used to call
     * {@link EmulatorMain#writeBack} when a disk is ejected.
     * 
     * @param main The new EmulatorMain instance.
     */
    public void setWriteBackTarget(EmulatorMain main) {
        this.main = main;
    }
    
    /**
     * Update whether the disk is write-protected.
     * 
     * @param wp Whether the disk is write-protected.
     */
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
        this.track = this.sector = this.wordsLeft = 
                this.intmsg = this.memaddr = this.diskaddr = 
                this.state = this.error = 0;
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
    
    /**
     * Updates the drive state.
     * 
     * @param state The new drive state.
     */
    public void setState(int state) {
        this.state = state;
        if (this.intmsg != 0) {
            cpu.queueInterrupt(this.intmsg);
        }
    }
    
    /**
     * Updates the drive error.
     * 
     * @param error The new drive error.
     */
    public void setError(int error) {
        this.error = error;
        if (this.intmsg != 0) {
            cpu.queueInterrupt(this.intmsg);
        }
    }
    
    /**
     * Updates the drive state and error.
     * 
     * @param state The new drive state.
     * @param error The new drive error.
     */
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
        stream.writeInt(state);
        stream.writeInt(error);
        stream.writeInt(intmsg);
        stream.writeInt(diskid);
        stream.writeInt(opdiskid);
        stream.writeLong(System.nanoTime() - nextTick);
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
        state = stream.readInt();
        error = stream.readInt();
        intmsg = stream.readInt();
        diskid = stream.readInt();
        opdiskid = stream.readInt();
        nextTick = System.nanoTime() - stream.readLong();
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
            long now = System.nanoTime();
            while (now >= this.nextTick) {
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
                    this.nextTick += NS_PER_TRACK;
                    continue;
                }
                if (this.track < this.seektotrack) {
                    ++this.track;
                    this.nextTick += NS_PER_TRACK;
                    continue;
                }
                // read or write
                this.nextTick += NS_PER_WORD;
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
                    return;
                }
            }
        }
    }

    /**
     * Returns the drive number, usually 0 or 1.
     * 
     * @return The drive number.
     */
    public int getDriveId() {
        return this.driveid;
    }

    /**
     * Tests whether a floppy disk is inserted inside the drive.
     * 
     * @return Whether a floppy disk is inside the drive.
     */
    public boolean hasMedia() {
        return this.inserted;
    }
    
    /**
     * Returns the contents of the media currently inserted into the drive 
     * as an array.
     * 
     * @return The raw disk data as an integer array, or null if
     *         {@link #hasMedia} would return false.
     */
    public int[] getRawMedia() {
        if (!this.hasMedia()) {
            return null;
        }
        return this.disk;
    }

    static final int STATE_NO_MEDIA = 0;
    static final int STATE_READY = 1;
    static final int STATE_READY_WP = 2;
    static final int STATE_BUSY = 3;
    
    static final int ERROR_NONE = 0;
    static final int ERROR_BUSY = 1;
    static final int ERROR_NO_MEDIA = 2;
    static final int ERROR_PROTECTED = 3;
    static final int ERROR_EJECT = 4;
    static final int ERROR_BAD_SECTOR = 5;
}
