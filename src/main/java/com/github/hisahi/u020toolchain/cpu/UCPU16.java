
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
import com.github.hisahi.u020toolchain.hardware.UNCD321;
import com.github.hisahi.u020toolchain.hardware.UNEM192;
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

public class UCPU16 implements ITickable {

    public static final int REG_A = 0;
    public static final int REG_B = 1;
    public static final int REG_C = 2;
    public static final int REG_X = 3;
    public static final int REG_Y = 4;
    public static final int REG_Z = 5;
    public static final int REG_I = 6;
    public static final int REG_J = 7;
    public boolean paused;
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
    public UCPU16(StandardMemory mem) {
        this.mem = mem;
        this.devices = new ArrayList<>();
        this.tickables = new ArrayList<>();
        this.main = null;
        this.breakpointsEnabled = false;
        this.breakpoints = new HashSet<>();
        this.reset(true);
    }
    public UCPU16(StandardMemory mem, EmulatorMain main) {
        this(mem);
        this.main = main;
    }

    public void reset() {
        reset(false);
    }
    
    public void reset(boolean rewriteROM) {
        boolean running = false;
        if (this.clock != null) {
            running = this.clock.isRunning();
            this.clock.stop();
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
        if (main != null) {
            this.devices.clear();
            main.initDevices();
        }
        this.paused = false;
        if (running) {
            this.clock.start();
        }
    }
    
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
    
    public List<Hardware> getDevices() {
        return this.devices;
    }

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
            int ibin = readMemoryAtPC();
            int a = (ibin >> 10) & 0b111111;
            int b = (ibin >> 5) & 0b11111;
            int o = (ibin) & 0b11111;
            int am = 0;
            int bm = 0;
            IAddressingMode ia = AddressingMode.decode(a);
            IAddressingMode ib = null;
            if (o != 0) {
                ib = AddressingMode.decode(b);
            }
            IInstruction instr = Instruction.decode(a, b, o);
            if (instr == null) {
                this.halt = true;
                debugger(I18n.format("error.illegalinstruction"));
                --cyclesLeft;
                return;
            }
            if (ia.takesNextWord()) {
                am = readMemoryAtPC();
            }
            if (ib != null) {
                if (ib.takesNextWord()) {
                    bm = readMemoryAtPC();
                }
            }
            if (this.skipBranches) {
                // skip multiple branch instructions if consecutive
                this.skipBranches = instr instanceof InstructionBranch;
                return;
            }
            if (!this.queueInterrupts && !interruptQueue.isEmpty()) {
                interrupt(interruptQueue.poll());
                this.interruptHandled = true;
                --cyclesLeft;
                return;
            }
            this.interruptHandled = false;
            this.cyclesLeft += ia.getCycles() + instr.getCycles();
            if (ib != null) {
                this.cyclesLeft += ib.getCycles();
            }
            instr.execute(this, ia, ib, am, bm);
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
    
    public void addCycles(int c) {
        this.cyclesLeft += c;
    }
    
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

    public void stackPush(int val) {
        sp = (sp - 1) & 0xFFFF;
        mem.write(sp, val);
    }
    
    public int stackPop() {
        int val = mem.read(sp);
        sp = (sp + 1) & 0xffff;
        return val;
    }

    public int readRegister(int reg) {
        switch (reg) {
            case 0: return rA;
            case 1: return rB;
            case 2: return rC;
            case 3: return rX;
            case 4: return rY;
            case 5: return rZ;
            case 6: return rI;
            case 7: return rJ;
        }
        return 0;
    }

    public void writeRegister(int reg, int val) {
        switch (reg) {
            case 0: 
                rA = (val & 0xFFFF); 
                break;
            case 1: 
                rB = (val & 0xFFFF); 
                break;
            case 2: 
                rC = (val & 0xFFFF); 
                break;
            case 3: 
                rX = (val & 0xFFFF); 
                break;
            case 4: 
                rY = (val & 0xFFFF); 
                break;
            case 5: 
                rZ = (val & 0xFFFF); 
                break;
            case 6: 
                rI = (val & 0xFFFF); 
                break;
            case 7: 
                rJ = (val & 0xFFFF); 
                break;
        }
    }

    public StandardMemory getMemory() {
        return this.mem;
    }
    
    public int getSP() {
        return this.sp;
    }
    public void setSP(int val) {
        this.sp = val & 0xFFFF;
    }
    public int getPC() {
        return this.pc;
    }
    public void setPC(int val) {
        this.pc = val & 0xFFFF;
    }
    public int getEX() {
        return this.ex;
    }
    public void setEX(int val) {
        this.ex = val & 0xFFFF;
    }
    public int getIA() {
        return this.ia;
    }
    public void setIA(int val) {
        this.ia = val & 0xFFFF;
    }

    public void skipConditional() {
        this.skipBranches = true;
    }
    public boolean willSkip() {
        return this.skipBranches;
    }

    public void increaseIJ() {
        rI = (rI + 1) & 0xFFFF;
        rJ = (rJ + 1) & 0xFFFF;
    }

    public void decreaseIJ() {
        rI = (rI - 1) & 0xFFFF;
        rJ = (rJ - 1) & 0xFFFF;
    }

    public void debugger(String reason) {
        main.showDebugger(reason);
    }

    public void setInterruptQueueingEnabled(boolean b) {
        this.queueInterrupts = b;
    }

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
    
    public void saveState(DataOutputStream stream) throws IOException {
        boolean restart = this.clock.isRunning();
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
            case 0x4fd524c5:
                return new M35FD(this, 0);
            case 0x4fd524c6:
                return new M35FD(this, 1);
        }
        return null;
    }

    public void restoreState(DataInputStream stream) throws IOException {
        boolean restart = this.clock.isRunning();
        this.clock.stop();
        this.interruptQueue.clear();
        this.devices.clear();
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
        if (uncd321 == null 
                || keyboard == null 
                || unem192 == null 
                || clock == null 
                || untm200 == null) {
            throw new IOException("missing devices: savestate invalid");
        } else {
            main.loadDevicesFromState(uncd321, keyboard, unem192, clock, untm200);
        }
        if (restart) {
            this.clock.start();
        }
    }

    public HighResolutionTimer getClock() {
        return this.clock;
    }

    public void setClock(HighResolutionTimer cpuclock) {
        this.clock = cpuclock;
    }

    public boolean isHalted() {
        return halt;
    }

    public int getCyclesLeft() {
        return (int) this.cyclesLeft;
    }
    
    public boolean wasInterruptHandled() {
        return this.interruptHandled;
    }

    public void enableBreakpoints() {
        this.breakpointsEnabled = true;
    }

    public void disableBreakpoints() {
        this.breakpointsEnabled = false;
    }
}
