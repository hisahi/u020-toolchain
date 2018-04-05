
package com.github.hisahi.u020toolchain.cpu;

import com.github.hisahi.u020toolchain.logic.HighResolutionTimer;
import com.github.hisahi.u020toolchain.ui.EmulatorMain;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class UCPU16Test {
    
    private UCPU16 cpu;
    public UCPU16Test() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        cpu = new UCPU16(new StandardMemory());
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void initialStateTest() {
        cpu.reset();
        for (int i = 0; i < 8; ++i) {
            assertEquals(0, cpu.readRegister(i));
        }
        assertEquals(0, cpu.getPC());
        assertEquals(0, cpu.getSP());
        assertEquals(0, cpu.getEX());
        assertEquals(0, cpu.getIA());
    }

    @Test
    public void testProgram1() {
        cpu.reset();
        /*          SET A, 0x30              ; 7c01 0030
                    SET [0x1000], 0x20       ; 7fc1 0020 1000
                    SUB A, [0x1000]          ; 7803 1000
                    IFN A, 0x10              ; c413
                        SET [0x4000], 1      ; 8bc1 4000
                    SET I, 10                ; acc1
                    SET A, 0x2000            ; 7c01 2000
        :loop       SET [0x2000+I], I        ; 1ac1 2000
                    SUB I, 1                 ; 88c3
                    IFN I, 0                 ; 84d3
                        SET PC, loop         ; 7f81 000d
                    SET X, 0x4               ; 9461
                    JSR testsub              ; 7c20 0017
                    SET PC, 0x1000           ; 7f81 1000 (-> end program)
        :testsub    SHL X, 4                 ; 946f
                    SET PC, POP              ; 6381             */
        final int[] code = new int[] {0x7c01, 0x0030, 0x7fc1, 0x0020, 0x1000, 0x7803, 0x1000, 0xc413, 0x8bc1, 0x4000, 0xacc1, 0x7c01, 0x2000, 0x1ac1, 0x2000, 0x88c3, 0x84d3, 0x7f81, 0x000d, 0x9461, 0x7c20, 0x0018, 0x7f81, 0x1000, 0x946f, 0x6381};
        for (int i = 0; i < code.length; ++i) {
            cpu.getMemory().write(i, code[i]);
        }
        cpu.getMemory().write(0x4000, 0);
        while (cpu.getPC() < 0x1000) {
            cpu.tick();
        }
        assertEquals("memory assignment did not work correctly", 0x20, cpu.getMemory().read(0x1000));
        assertEquals("IFN condition did not work correctly", 0, cpu.getMemory().read(0x4000));
        for (int i = 1; i <= 10; ++i) {
            assertEquals("loop section did not work correctly", i, cpu.getMemory().read(0x2000 + i));
        }
        assertEquals("subroutine or shifting did not work correctly", 0x40, cpu.readRegister(UCPU16.REG_X));
    }
    
    @Test
    public void basicFpTest() {
        /// TODO if BASIC updates, this vector needs to be changed
        final int FP0_sqrt = 0x37e9;
        // try to compute the square root of 0.5 using the BASIC ROM
        // floating point functions (CPU test)
        final int FP0 = 0x80;
        cpu.reset(true);
        // JSR FP0_sqrt
        cpu.getMemory().write(0x00, 0x7c20);
        cpu.getMemory().write(0x01, FP0_sqrt);
        // 0x0080 0x0000 0x8000 0x0000 0x0000 = 0.5
        cpu.getMemory().write(FP0+0, 0x0080);
        cpu.getMemory().write(FP0+1, 0x0000);
        cpu.getMemory().write(FP0+2, 0x8000);
        cpu.getMemory().write(FP0+3, 0x0000);
        cpu.getMemory().write(FP0+4, 0x0000);
        // run until JSR returns
        while (cpu.getPC() != 2) {
            cpu.tick();
        }
        // 0x0080 0x0000 0xb504 0xf333 0xf9da ~= 0.70710678
        assertEquals(0x0080, cpu.getMemory().read(FP0+0));
        assertEquals(0x0000, cpu.getMemory().read(FP0+1));
        assertEquals(0xb504, cpu.getMemory().read(FP0+2));
        assertEquals(0xf333, cpu.getMemory().read(FP0+3));
        assertEquals(0xf9da, cpu.getMemory().read(FP0+4));
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
        cpu.writeRegister(UCPU16.REG_A, 0);
        int r = (int) (Math.random() * 65536);
        cpu.setIA(1);
        cpu.queueInterrupts = false;
        cpu.queueInterrupt(r);
        assertEquals("interrupt was not queued", 1, cpu.interruptQueue.size());
    }
    
    @Test
    public void interruptQueueTestIaZero() {
        cpu.writeRegister(UCPU16.REG_A, 0);
        int r = (int) (Math.random() * 65536);
        cpu.setIA(0);
        cpu.queueInterrupts = false;
        cpu.queueInterrupt(r);
        assertEquals("interrupt was not queued", 1, cpu.interruptQueue.size());
    }
    
    @Test
    public void interruptHandleIaNonzeroTest() {
        int r = (int) (Math.random() * 65536);
        cpu.writeRegister(UCPU16.REG_A, 0);
        cpu.setIA(1);
        cpu.queueInterrupts = false;
        cpu.queueInterrupt(r);
        while (cpu.cyclesLeft > 0) {
            cpu.tick();
        }
        cpu.tick();
        assertEquals("interrupt message not written to A", r, cpu.readRegister(UCPU16.REG_A));
        assertTrue("interrupt queueing was not enabled", cpu.queueInterrupts);
    }
    
    @Test
    public void interruptHandleIaZeroTest() {
        int r = (int) (Math.random() * 65536);
        cpu.writeRegister(UCPU16.REG_A, 0);
        cpu.reset();
        cpu.setIA(0);
        cpu.queueInterrupts = false;
        cpu.queueInterrupt(r);
        while (cpu.cyclesLeft > 0) {
            cpu.tick();
        }
        cpu.tick();
        assertEquals("interrupt should not be handled as IA = 0", 0, cpu.readRegister(UCPU16.REG_A));
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
