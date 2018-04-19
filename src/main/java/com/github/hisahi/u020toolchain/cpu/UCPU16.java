
package com.github.hisahi.u020toolchain.cpu; 

import com.github.hisahi.u020toolchain.hardware.Hardware;
import com.github.hisahi.u020toolchain.cpu.addressing.AddressingMode;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;
import com.github.hisahi.u020toolchain.cpu.instructions.IInstruction;
import com.github.hisahi.u020toolchain.cpu.instructions.Instruction;
import com.github.hisahi.u020toolchain.cpu.instructions.InstructionBranch;
import com.github.hisahi.u020toolchain.hardware.Clock;
import com.github.hisahi.u020toolchain.hardware.Keyboard;
import com.github.hisahi.u020toolchain.hardware.M35FD;
import com.github.hisahi.u020toolchain.hardware.UNAC810;
import com.github.hisahi.u020toolchain.hardware.UNCD321;
import com.github.hisahi.u020toolchain.hardware.UNEM192;
import com.github.hisahi.u020toolchain.hardware.UNMS001;
import com.github.hisahi.u020toolchain.hardware.UNTM200;
import com.github.hisahi.u020toolchain.logic.HighResolutionTimer;
import com.github.hisahi.u020toolchain.logic.ITickable;
import com.github.hisahi.u020toolchain.ui.EmulatorMain;
import com.github.hisahi.u020toolchain.ui.I18n;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the CPU and main module of the Univtek 020 computer. 
 * The CPU is named UCPU-16 after the DCPU-16, a CPU architecture
 * that it is heavily influenced by and based on. The CPU module
 * does most of the work in running the computer: running the
 * instructions, handling interrupts and passing signals to
 * connected hardware.
 * 
 * @author hisahi
 */
public class UCPU16 implements ITickable {

    private boolean paused;
    private boolean interruptHandled;
    private boolean breakpointsEnabled;
    public Set<Integer> breakpoints;
    boolean queueInterrupts;
    Queue<Interrupt> interruptQueue;
    StandardMemory mem;
    long cyclesLeft;
    long totalCycles;
    boolean halt;
    boolean skipBranches;
    private EmulatorMain main;
    private HighResolutionTimer clock;
    List<Hardware> devices;
    List<ITickable> tickables;
    // registers
    int rA;
    int rB;
    int rC;
    int rX;
    int rY;
    int rZ;
    int rI;
    int rJ;
    int pc;
    int sp;
    int ex;
    int ia;
    
    /**
     * Initializes a new UCPU16 instance.
     * 
     * @param mem A StandardMemory instance that represents the central memory.
     */
    public UCPU16(StandardMemory mem) {
        this.mem = mem;
        this.devices = new ArrayList<>();
        this.tickables = new ArrayList<>();
        this.main = null;
        this.breakpointsEnabled = false;
        this.breakpoints = new HashSet<>();
        this.reset(true);
    }
    
    /**
     * Initializes a new UCPU16 instance with an EmulatorMain.
     * 
     * @param mem A StandardMemory instance that represents the central memory.
     * @param main An EmulatorMain instance.
     */
    public UCPU16(StandardMemory mem, EmulatorMain main) {
        this(mem);
        this.main = main;
    }

    /**
     * Resets the CPU. The contents of the ROM are not written into the RAM,
     * but all devices will be reinitialized.
     */
    public void reset() {
        reset(false, true);
    }

    /**
     * Resets the CPU. All devices will be reinitialized.
     * 
     * @param rewriteROM   Whether to rewrite the contents of the ROM into RAM.
     */
    public void reset(boolean rewriteROM) {
        reset(rewriteROM, true);
    }
    
    /**
     * Resets the CPU.
     * 
     * @param rewriteROM   Whether to rewrite the contents of the ROM into RAM.
     * @param resetDevices Whether to reinitialize all devices.
     */
    public void reset(boolean rewriteROM, boolean resetDevices) {
        boolean running = false;
        if (this.clock != null) {
            running = this.clock.isRunning();
            this.clock.stop();
        }
        if (resetDevices && main != null) {
            this.devices.clear();
            main.initDevices();
            main.reloadConfig();
        }
        this.rA = this.rB = this.rC = this.rX = this.rY = this.rZ
                = this.rI = this.rJ = 0;
        this.pc = this.sp = this.ex = this.ia = 0;
        this.cyclesLeft = this.totalCycles = 0L;
        this.interruptQueue = new ArrayDeque<>();
        this.halt = this.queueInterrupts = this.interruptHandled = this.skipBranches = false;
        if (rewriteROM) {
            this.rewriteFromROM();
        }
        this.paused = false;
        if (running) {
            this.clock.start();
        }
    }
    
    /**
     * Adds a peripheral into the CPU.
     * 
     * @param hw The hardware peripheral to add.
     */
    public void addDevice(Hardware hw) {
        assert hw != null;
        for (Hardware h: devices) {
            if (h.hardwareId() == hw.hardwareId()) {
                throw new IllegalArgumentException("hardware with ID " + String.format("%08x", hw.hardwareId()) + " is already attached!");
            }
        }
        if (hw instanceof ITickable) {
            this.tickables.add((ITickable) hw);
        }
        this.devices.add(hw);
    }
    
    /**
     * Returns the list of inserted hardware peripherals.
     * 
     * @return The list of devices that are currently plugged in to the CPU.
     */
    public List<Hardware> getDevices() {
        return this.devices;
    }
    
    // these variables are used by tick() only but avoids allocation every time
    private int ibin;
    private int am;
    private int bm;
    private int ta;
    private int tb;
    private int to;
    private IAddressingMode tia;
    private IAddressingMode tib;
    private IInstruction tinstr;
    
    @Override
    public void tick() {
        if (halt || paused) {
            return;
        }
        for (ITickable hw: tickables) {
            hw.tick();
        }
        if (cyclesLeft == 0) {
            if (this.breakpointsEnabled) {
                if (breakpoints.contains(pc)) {
                    this.paused = true;
                    debugger(I18n.format("debugger.breakpoint"));
                    return;
                }
            }
            ibin = readMemoryAtPC();
            // extract a, b, o fields from the instruction
            ta = (ibin >> 10) & 0b111111;
            tb = (ibin >> 5) & 0b11111;
            to = (ibin) & 0b11111;
            am = 0;
            bm = 0;
            tia = AddressingMode.decode(ta);
            tib = null;
            if (to != 0) {
                tib = AddressingMode.decode(tb);
            }
            tinstr = Instruction.decode(ta, tb, to);
            if (tinstr == null) {
                this.halt = true;
                debugger(I18n.format("error.illegalinstruction"));
                --cyclesLeft;
                return;
            }
            if (tia.takesNextWord()) {
                am = readMemoryAtPC();
            }
            if (tib != null && tib.takesNextWord()) {
                bm = readMemoryAtPC();
            }
            if (this.skipBranches) {
                // skip multiple branch instructions if consecutive
                this.skipBranches = tinstr instanceof InstructionBranch;
                return;
            }
            if (!this.queueInterrupts && !interruptQueue.isEmpty()) {
                interrupt(interruptQueue.poll());
                this.interruptHandled = true;
                --cyclesLeft;
                return;
            }
            this.interruptHandled = false;
            this.cyclesLeft += tia.getCycles() + tinstr.getCycles()
                            + (tib != null ? tib.getCycles() : 0);
            tinstr.execute(this, tia, tib, am, bm);
        }
        --cyclesLeft;
    }
    
    private void interrupt(Interrupt i) {
        this.cyclesLeft += 4;
        if (this.ia == 0) {
            return;
        }
        this.queueInterrupts = true;
        this.stackPush(pc);
        this.stackPush(rA);
        pc = ia;
        rA = i.getInterruptMessage();
    }
    
    /**
     * Adds a number of cycles to the CPU, effectively delaying it.
     * 
     * @param c The number of cycles to add.
     */
    public void addCycles(int c) {
        this.cyclesLeft += c;
    }
    
    /**
     * Queues an interrupt into the interrupt queue of this CPU.
     * 
     * @param msg The interrupt message of the interrupt to queue.
     */
    public void queueInterrupt(int msg) {
        if (interruptQueue.size() >= 256) {
            this.halt = true;
            debugger(I18n.format("error.intqueuefull"));
            return;
        }
        interruptQueue.add(new Interrupt(msg));
    }

    private int readMemoryAtPC() {
        int v = mem.read(pc);
        pc = (pc + 1) & 0xFFFF;
        return v;
    }

    /**
     * Pushes a value into the CPU stack.
     * 
     * @param val The value to push.
     */
    public void stackPush(int val) {
        sp = (sp - 1) & 0xFFFF;
        mem.write(sp, val);
    }
    
    /**
     * Pops a value from the CPU stack.
     * 
     * @return The value that was popped from the stack.
     */
    public int stackPop() {
        int val = mem.read(sp);
        sp = (sp + 1) & 0xffff;
        return val;
    }

    /**
     * Reads the value of a general-purpose CPU register.
     * 
     * @param reg The register to read from.
     * @return    The value in the register.
     */
    public int readRegister(Register reg) {
        switch (reg) {
            case A: return rA;
            case B: return rB;
            case C: return rC;
            case X: return rX;
            case Y: return rY;
            case Z: return rZ;
            case I: return rI;
            case J: return rJ;
        }
        return 0;
    }

    /**
     * Writes a value into a general-purpose CPU register.
     * 
     * @param reg The register to write to.
     * @param val The value to write.
     */
    public void writeRegister(Register reg, int val) {
        switch (reg) {
            case A: 
                rA = (val & 0xFFFF); 
                break;
            case B: 
                rB = (val & 0xFFFF); 
                break;
            case C: 
                rC = (val & 0xFFFF); 
                break;
            case X: 
                rX = (val & 0xFFFF); 
                break;
            case Y: 
                rY = (val & 0xFFFF); 
                break;
            case Z: 
                rZ = (val & 0xFFFF); 
                break;
            case I: 
                rI = (val & 0xFFFF); 
                break;
            case J: 
                rJ = (val & 0xFFFF); 
                break;
        }
    }

    /**
     * Returns the current memory the CPU is using.
     * 
     * @return The StandardMemory instance.
     */
    public StandardMemory getMemory() {
        return this.mem;
    }
    
    /**
     * Returns the current value of the SP (stack pointer) register.
     * 
     * @return The value currently in the SP register.
     */
    public int getSP() {
        return this.sp;
    }
    
    /**
     * Sets the new value of the SP (stack pointer) register.
     * 
     * @param val The new value to assign.
     */
    public void setSP(int val) {
        this.sp = val & 0xFFFF;
    }
    
    /**
     * Returns the current value of the PC (program counter) register.
     * 
     * @return The value currently in the PC register.
     */
    public int getPC() {
        return this.pc;
    }
    
    /**
     * Sets the new value of the PC (program counter) register.
     * 
     * @param val The new value to assign.
     */
    public void setPC(int val) {
        this.pc = val & 0xFFFF;
    }
    
    /**
     * Returns the current value of the EX (extra bits, overflow) register.
     * 
     * @return The value currently in the EX register.
     */
    public int getEX() {
        return this.ex;
    }
    
    /**
     * Sets the new value of the EX (extra bits, overflow) register.
     * 
     * @param val The new value to assign.
     */
    public void setEX(int val) {
        this.ex = val & 0xFFFF;
    }
    
    /**
     * Returns the current value of the IA (interrupt handler address) register.
     * 
     * @return The value currently in the IA register.
     */
    public int getIA() {
        return this.ia;
    }
    
    /**
     * Sets the new value of the IA (interrupt handler address) register.
     * 
     * @param val The new value to assign.
     */
    public void setIA(int val) {
        this.ia = val & 0xFFFF;
    }

    /**
     * Sets the next instruction to be skipped.
     */
    public void skipConditional() {
        this.skipBranches = true;
    }
    
    /**
     * Returns whether the next instruction is to be skipped.
     * 
     * @return true if the next instruction will be skipped, false if not.
     */
    public boolean willSkip() {
        return this.skipBranches;
    }

    /**
     * Increments the I and J registers.
     */
    public void increaseIJ() {
        rI = (rI + 1) & 0xFFFF;
        rJ = (rJ + 1) & 0xFFFF;
    }

    /**
     * Decrements the I and J registers.
     */
    public void decreaseIJ() {
        rI = (rI - 1) & 0xFFFF;
        rJ = (rJ - 1) & 0xFFFF;
    }

    /**
     * Triggers the debugger.
     * 
     * @param reason The cause for the debugger to be triggered, such as the
     *               type of invalid state entered by the CPU
     */
    public void debugger(String reason) {
        main.showDebugger(reason);
    }

    /**
     * Enables or disables interrupt queueing.
     * 
     * @param b true to enable interrupt queueing, false to disable it.
     */
    public void setInterruptQueueingEnabled(boolean b) {
        this.queueInterrupts = b;
    }

    /**
     * Tests whether interrupt queueing is enabled. 
     * 
     * @return true if interrupts are being queued, false otherwise.
     */
    public boolean areInterruptsBeingQueued() {
        return this.queueInterrupts;
    }

    private void rewriteFromROM() {
        try (DataInputStream dis = new DataInputStream(this.getClass().getResourceAsStream("/basicrom.bin"))) {
            int ptr = 0;
            while (ptr < 65536) {
                mem.write(ptr++, dis.readUnsignedShort());
            }
        } catch (EOFException e) {
        } catch (IOException ex) {
            Logger.getLogger(UCPU16.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Dumps the values of all registers into a textual format for debugging.
     * 
     * @return A text describing the contents of all registers.
     */
    public String dumpRegisters() {
        StringBuilder sb = new StringBuilder();
        sb.append(".");
        sb.append("  A:" + String.format("%04x", rA) + " ");
        sb.append("  B:" + String.format("%04x", rB) + " ");
        sb.append("  C:" + String.format("%04x", rC) + " ");
        sb.append("  X:" + String.format("%04x", rX) + "\n");
        sb.append("  Y:" + String.format("%04x", rY) + " ");
        sb.append("  Z:" + String.format("%04x", rZ) + " ");
        sb.append("  I:" + String.format("%04x", rI) + " ");
        sb.append("  J:" + String.format("%04x", rJ) + "\n");
        sb.append(" PC:" + String.format("%04x", pc) + " ");
        sb.append(" SP:" + String.format("%04x", sp) + " ");
        sb.append(" EX:" + String.format("%04x", ex) + " ");
        sb.append(" IA:" + String.format("%04x", ia) + "\n");
        return sb.toString().trim().substring(1);
    }
    
    /**
     * Saves the CPU state into a stream.
     * 
     * @param stream       The stream to write to.
     * @throws IOException if the stream cannot be written to due to an I/O error
     */
    public void saveState(DataOutputStream stream) throws IOException {
        boolean restart = this.clock.isRunning();
        // save number of cycles left, CPU halt state, 
        // whether instruction will be skipped, interrupt queueing,
        // all registers, full memory state, interrupt queue,
        // hardware
        this.clock.stop();
        stream.writeLong(cyclesLeft);
        stream.writeInt(halt ? 1 : 0);
        stream.writeInt(skipBranches ? 1 : 0);
        stream.writeInt(queueInterrupts ? 1 : 0);
        stream.writeInt(rA);
        stream.writeInt(rB);
        stream.writeInt(rC);
        stream.writeInt(rX);
        stream.writeInt(rY);
        stream.writeInt(rZ);
        stream.writeInt(rI);
        stream.writeInt(rJ);
        stream.writeInt(pc);
        stream.writeInt(sp);
        stream.writeInt(ex);
        stream.writeInt(ia);
        for (int i = 0; i < StandardMemory.MEMORY_SIZE; ++i) {
            stream.writeShort((short) mem.array()[i]);
        }
        stream.writeInt(interruptQueue.size());
        for (Interrupt i: interruptQueue) {
            stream.writeInt(i.getInterruptMessage());
        }
        stream.writeInt(devices.size());
        for (Hardware hw: devices) {
            stream.writeInt((int) hw.hardwareId());
            hw.saveState(stream);
        }
        if (restart) {
            this.clock.start();
        }
    }
    
    /**
     * Converts a hardware ID into an instance of that hardware implementation.
     * 
     * @param d The hardware ID.
     * @return  An instance of the implementation of that hardware.
     */
    public Hardware identifyDeviceFromId(long d) {
        switch ((int) d) {
            case 0x30cf7406:
                return new Keyboard(this);
            case 0xdb7b373e:
                return new UNCD321(this, main);
            case 0xca1c4b47:
                return new UNEM192(this);
            case 0x12d0b402:
                return new Clock(this);
            case 0x8f1705a6:
                return new UNTM200(this);
            case 0xab212484:
                return new UNMS001(this);
            case 0x2feaccd6:
                return new UNAC810(this);
            case 0x4fd524c5:
                return new M35FD(this, 0);
            case 0x4fd524c6:
                return new M35FD(this, 1);
        }
        return null;
    }

    /**
     * Restores the CPU state from a stream.
     * 
     * @param stream       The stream to read from.
     * @throws IOException if the data is corrupted, invalid or truncated
     *                     or if the stream cannot be read due to an I/O error
     */
    public void restoreState(DataInputStream stream) throws IOException {
        boolean restart = this.clock.isRunning();
        this.clock.stop();
        this.interruptQueue.clear();
        this.devices.clear();
        // restore number of cycles left, CPU halt state, 
        // whether instruction will be skipped, interrupt queueing,
        // all registers, full memory state, interrupt queue,
        // hardware
        cyclesLeft = stream.readLong();
        halt = stream.readInt() != 0;
        skipBranches = stream.readInt() != 0;
        queueInterrupts = stream.readInt() != 0;
        rA = stream.readInt();
        rB = stream.readInt();
        rC = stream.readInt();
        rX = stream.readInt();
        rY = stream.readInt();
        rZ = stream.readInt();
        rI = stream.readInt();
        rJ = stream.readInt();
        pc = stream.readInt();
        sp = stream.readInt();
        ex = stream.readInt();
        ia = stream.readInt();
        for (int i = 0; i < StandardMemory.MEMORY_SIZE; ++i) {
            mem.array()[i] = (int) stream.readShort() & 0xFFFF;
        }
        int iq = stream.readInt();
        for (int i = 0; i < iq; ++i) {
            queueInterrupt(stream.readInt());
        }
        int dn = stream.readInt();
        UNCD321 uncd321 = null;
        Keyboard keyboard = null;
        UNEM192 unem192 = null;
        Clock clock = null;
        UNTM200 untm200 = null;
        for (int i = 0; i < dn; ++i) {
            long id = (long) stream.readInt() & 0xFFFFFFFFL;
            Hardware hw = identifyDeviceFromId(id);
            if (hw != null) {
                if (hw instanceof UNCD321) {
                    uncd321 = (UNCD321) hw;
                } else if (hw instanceof Keyboard) {
                    keyboard = (Keyboard) hw;
                } else if (hw instanceof UNEM192) {
                    unem192 = (UNEM192) hw;
                } else if (hw instanceof Clock) {
                    clock = (Clock) hw;
                } else if (hw instanceof UNTM200) {
                    untm200 = (UNTM200) hw;
                }
                this.addDevice(hw);
                hw.restoreState(stream);
                if (hw instanceof M35FD) {
                    ((M35FD) hw).setWriteBackTarget(main);
                }
            }
        }
        if (main != null) {
            if (uncd321 == null 
                    || keyboard == null 
                    || unem192 == null 
                    || clock == null 
                    || untm200 == null) {
                throw new IOException("missing devices: savestate invalid");
            } else {
                main.loadDevicesFromState(uncd321, keyboard, unem192, clock, untm200);
            }
        }
        if (restart) {
            this.clock.start();
        }
    }

    /**
     * Returns the currently used CPU clock.
     * 
     * @return The current CPU clock.
     */
    public HighResolutionTimer getClock() {
        return this.clock;
    }

    /**
     * Changes the CPU clock. This is used to pause and resume the timer
     * when the CPU is paused or resumed.
     * 
     * @param cpuclock The new CPU clock.
     */
    public void setClock(HighResolutionTimer cpuclock) {
        this.clock = cpuclock;
    }

    /**
     * Tests whether the CPU execution is halted due to an invalid state.
     * 
     * @return true if the CPU is halted, false if not.
     */
    public boolean isHalted() {
        return halt;
    }

    /**
     * Returns the number of CPU cycles left until the next instruction is executed.
     * 
     * @return The number of CPU cycles before the next instruction is fetched.
     */
    public int getCyclesLeft() {
        return (int) this.cyclesLeft;
    }
    
    /**
     * Tests whether the CPU is currently burning cycles to handle an interrupt.
     * 
     * @return Whether the CPU is handling the interrupt right now.
     */
    public boolean wasInterruptHandled() {
        return this.interruptHandled;
    }

    /**
     * Enables the checking for breakpoints.
     */
    public void enableBreakpoints() {
        this.breakpointsEnabled = true;
    }

    /**
     * Disables the checking for breakpoints.
     */
    public void disableBreakpoints() {
        this.breakpointsEnabled = false;
    }
    
    /**
     * Returns whether the CPU is paused.
     * 
     * @return true if the CPU is paused, false if not.
     */
    public boolean isPaused() {
        return this.paused;
    }
    
    /**
     * Pauses the execution of the CPU.
     */
    public void pause() {
        this.paused = true;
        for (Hardware hw: this.devices) {
            hw.pause();
        }
        if (main != null) {
            main.updatePause();
        }
    }
    
    /**
     * Resumes the execution of the CPU.
     */
    public void resume() {
        for (Hardware hw: this.devices) {
            hw.resume();
        }
        this.paused = false;
        if (main != null) {
            main.updatePause();
        }
    }
}
