
package com.github.hisahi.u020_toolchain.cpu; 

import com.github.hisahi.u020_toolchain.cpu.addressing.AddressingMode;
import com.github.hisahi.u020_toolchain.cpu.addressing.IAddressingMode;
import com.github.hisahi.u020_toolchain.cpu.instructions.IInstruction;
import com.github.hisahi.u020_toolchain.cpu.instructions.Instruction;
import com.github.hisahi.u020_toolchain.cpu.instructions.InstructionBranch;
import com.github.hisahi.u020_toolchain.logic.ITickable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class UCPU16 implements ITickable {
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
    boolean queueInterrupts;
    Queue<Interrupt> interruptQueue;
    StandardMemory mem;
    long cyclesLeft;
    long totalCycles;
    boolean halt;
    boolean skipBranches;
    List<Hardware> devices;
    public UCPU16(StandardMemory mem) {
        this.mem = mem;
        this.devices = new ArrayList<>();
        this.reset();
    }
    
    public void reset() {
        this.rA = this.rB = this.rC = this.rX = this.rY = this.rZ
                = this.rI = this.rJ = 0;
        this.pc = this.sp = this.ex = this.ia = 0;
        this.cyclesLeft = this.totalCycles = 0L;
        this.queueInterrupts = false;
        this.interruptQueue = new ArrayDeque<>();
        this.halt = false;
        this.skipBranches = false;
    }
    
    public void addDevice(Hardware hw) {
        this.devices.add(hw);
    }
    
    public List<Hardware> getDevices() {
        return this.devices;
    }

    @Override
    public void tick() {
        if (halt) return;
        for (Hardware hw: devices) {
            hw.tick();
        }
        if (cyclesLeft == 0) {
            nextInstruction();
        }
        --cyclesLeft;
    }

    private void nextInstruction() {
        if (!this.queueInterrupts && !interruptQueue.isEmpty()) {
            interrupt(interruptQueue.poll());
            return;
        }
        int ibin = readMemoryAtPC();
        int a = (ibin >> 10) & 0b111111;
        int b = (ibin >> 5) & 0b11111;
        int o = (ibin) & 0b11111;
        int am = 0;
        int bm = 0;
        IAddressingMode ia = AddressingMode.decode(a);
        if (ia.takesNextWord()) {
            am = readMemoryAtPC();
        }
        IAddressingMode ib = AddressingMode.LITERAL[1];
        if (o != 0) {
            ib = AddressingMode.decode(b);
            if (ib.takesNextWord()) {
                bm = readMemoryAtPC();
            }
        }
        IInstruction instr = Instruction.decode(a, b, o);
        if (instr == null) {
            debugger("illegal instruction");
            return;
        }
        if (this.skipBranches) {
            this.cyclesLeft += 1;
            // skip multiple branch instructions if consecutive
            this.skipBranches = !(instr instanceof InstructionBranch);
            return;
        }
        this.cyclesLeft += ia.getCycles();
        this.cyclesLeft += ib.getCycles();
        this.cyclesLeft += instr.getCycles();
        instr.execute(this, ia, ib, am, bm);
    }

    private void interrupt(Interrupt i) {
        this.cyclesLeft = 4;
        if (this.ia == 0)
            return;
        this.queueInterrupts = true;
        this.stackPush(pc);
        this.stackPush(rA);
        pc = ia;
        rA = i.getInterruptMessage();
    }
    
    public void queueInterrupt(int msg) {
        if (interruptQueue.size() >= 256) {
            debugger("interrupt queue full");
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
            case 0: rA = (val & 0xFFFF); break;
            case 1: rB = (val & 0xFFFF); break;
            case 2: rC = (val & 0xFFFF); break;
            case 3: rX = (val & 0xFFFF); break;
            case 4: rY = (val & 0xFFFF); break;
            case 5: rZ = (val & 0xFFFF); break;
            case 6: rI = (val & 0xFFFF); break;
            case 7: rJ = (val & 0xFFFF); break;
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
        this.halt = true;
        /// TODO bring up debugger here!
    }

    public void setInterruptQueueingEnabled(boolean b) {
        this.queueInterrupts = b;
    }

    public boolean areInterruptsBeingQueued() {
        return this.queueInterrupts;
    }
    
}
