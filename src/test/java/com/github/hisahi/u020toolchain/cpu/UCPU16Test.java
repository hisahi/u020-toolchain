
package com.github.hisahi.u020toolchain.cpu;

import com.github.hisahi.u020toolchain.logic.HighResolutionTimer;
import com.github.hisahi.u020toolchain.ui.EmulatorMain;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the UCPU-16 CPU.
 * 
 * @author hisahi
 */
public class UCPU16Test {
    
    private UCPU16 cpu;
    public UCPU16Test() {
    }
    
    @Before
    public void setUp() {
        cpu = new UCPU16(new StandardMemory());
    }

    @Test
    public void initialStateTest() {
        cpu.reset();
        for (int i = 0; i < 8; ++i) {
            assertEquals(0, cpu.readRegister(Register.values()[i]));
        }
        assertEquals(0, cpu.getPC());
        assertEquals(0, cpu.getSP());
        assertEquals(0, cpu.getEX());
        assertEquals(0, cpu.getIA());
    }
    
    @Test
    public void restoreSavedStateWithoutExceptionTest() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream daos = new DataOutputStream(baos);
        HighResolutionTimer timer = new HighResolutionTimer(1, cpu);
        cpu.setClock(timer);
        EmulatorMain.initDevicesForTesting(cpu);
        cpu.saveState(daos);
        int oldDeviceCount = cpu.getDevices().size();
        String oldRegDump = cpu.dumpRegisters();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dais = new DataInputStream(bais);
        UCPU16 cpu2 = new UCPU16(new StandardMemory());
        cpu2.setClock(timer);
        cpu2.restoreState(dais);
        assertEquals("CPU restore state didn't restore devices correctly", oldDeviceCount, cpu2.getDevices().size());
        assertEquals("CPU restore state didn't restore registers correctly", oldRegDump, cpu2.dumpRegisters());
    }
    
    @Test
    public void interruptQueueTestIaNonzero() {
        cpu.writeRegister(Register.A, 0);
        int r = (int) (Math.random() * 65536);
        cpu.setIA(1);
        cpu.queueInterrupts = false;
        cpu.queueInterrupt(r);
        assertEquals("interrupt was not queued", 1, cpu.interruptQueue.size());
    }
    
    @Test
    public void interruptQueueTestIaZero() {
        cpu.writeRegister(Register.A, 0);
        int r = (int) (Math.random() * 65536);
        cpu.setIA(0);
        cpu.queueInterrupts = false;
        cpu.queueInterrupt(r);
        assertEquals("interrupt was not queued", 1, cpu.interruptQueue.size());
    }
    
    @Test
    public void interruptHandleIaNonzeroTest() {
        int r = (int) (Math.random() * 65536);
        cpu.writeRegister(Register.A, 0);
        cpu.setIA(1);
        cpu.queueInterrupts = false;
        cpu.queueInterrupt(r);
        while (cpu.cyclesLeft > 0) {
            cpu.tick();
        }
        cpu.tick();
        assertEquals("interrupt message not written to A", r, cpu.readRegister(Register.A));
        assertTrue("interrupt queueing was not enabled", cpu.queueInterrupts);
    }
    
    @Test
    public void interruptHandleIaZeroTest() {
        int r = (int) (Math.random() * 65536);
        cpu.writeRegister(Register.A, 0);
        cpu.reset();
        cpu.setIA(0);
        cpu.queueInterrupts = false;
        cpu.queueInterrupt(r);
        while (cpu.cyclesLeft > 0) {
            cpu.tick();
        }
        cpu.tick();
        assertEquals("interrupt should not be handled as IA = 0", 0, cpu.readRegister(Register.A));
    }
    
    @Test
    public void pauseTest() {
        cpu.pause();
        assertTrue("CPU was not paused", cpu.isPaused());
    }
    
    @Test
    public void resumeTest() {
        cpu.pause();
        assertTrue("CPU was not paused", cpu.isPaused());
        cpu.resume();
        assertFalse("CPU was not resumed", cpu.isPaused());
    }
}
