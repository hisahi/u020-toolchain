
package com.github.hisahi.u020toolchain.hardware; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An abstract class that all UCPU-16 peripheral hardware classes
 * must extend. Extending classes must implement functions to return 
 * the hardware ID, version and manufacturer information, handle reseting, 
 * pausing and resuming, saving and loading states and hardware 
 * interrupts (HWIs).
 * 
 * @author hisahi
 */
public abstract class Hardware {
    protected UCPU16 cpu;
    
    /**
     * Initializes a new Hardware instance.
     * 
     * @param cpu The UCPU16 instance.
     */
    public Hardware(UCPU16 cpu) {
        this.cpu = cpu;
    }
    
    /**
     * Gets the low word of the hardware ID.
     * 
     * @return The low word (16 bits) of the hardware ID.
     */
    public int getIdLow() {
        return (int) (hardwareId() & 0xFFFF);
    }
    
    /**
     * Gets the high word of the hardware ID.
     * 
     * @return The high word (16 bits) of the hardware ID.
     */
    public int getIdHigh() {
        return (int) ((hardwareId() >> 16) & 0xFFFF);
    }
    
    /**
     * Gets the word that corresponds to the hardware version.
     * 
     * @return The hardware version as a word.
     */
    public int getVersion() {
        return hardwareVersion();
    }
    
    /**
     * Gets the low word of the hardware manufacturer ID.
     * 
     * @return The low word (16 bits) of the manufacturer hardware ID.
     */
    public int getManufacturerLow() {
        return (int) (hardwareManufacturer() & 0xFFFF);
    }
    
    /**
     * Gets the high word of the hardware manufacturer ID.
     * 
     * @return The high word (16 bits) of the manufacturer hardware ID.
     */
    public int getManufacturerHigh() {
        return (int) ((hardwareManufacturer() >> 16) & 0xFFFF);
    }
    
    /**
     * Returns the hardware ID, which is a 32-bit word unique for
     * every instance of the hardware. Most peripherals will only have
     * a single copy plugged into the computer at any given time, and
     * in those cases it's sufficient to return an ID unique for 
     * this hardware.
     * 
     * @return The hardware ID as a 32-bit word.
     */
    public abstract long hardwareId();
    
    /**
     * Returns the hardware version, which is a 16-bit word.
     * 
     * @return The hardware version as a 16-bit word.
     */
    public abstract int hardwareVersion();
    
    /**
     * Returns the hardware manufacturer ID.
     * 
     * @return The hardware manufacturer ID as a 32-bit word.
     */
    public abstract long hardwareManufacturer();
    
    /**
     * Called when a hardware interrupt is triggered for this hardware.
     * 
     * @param cpu The UCPU16 instance that triggered the hardware interrupt.
     */
    public abstract void hwi(UCPU16 cpu);
    
    /**
     * Called when the CPU execution is reset.
     */
    public abstract void reset();
    
    /**
     * Called when the CPU execution is paused.
     */
    public abstract void pause();
    
    /**
     * Called when the CPU execution is resumed.
     */
    public abstract void resume();
    
    /**
     * Called when a CPU state is saved. The hardware is expected to 
     * write all necessary data into the stream.
     * 
     * @param stream The stream to write to.
     * @throws IOException Generic I/O if the stream cannot be written to.
     */
    public abstract void saveState(DataOutputStream stream) throws IOException;
    
    /**
     * Called when a CPU state is restored. The hardware is expected to 
     * read all necessary data from the stream.
     * 
     * @param stream The stream to read from.
     * @throws IOException Generic I/O if the stream cannot be read from. Also
     *                     used if the data is invalid, corrupted or truncated.
     */
    public abstract void restoreState(DataInputStream stream) throws IOException;
}
