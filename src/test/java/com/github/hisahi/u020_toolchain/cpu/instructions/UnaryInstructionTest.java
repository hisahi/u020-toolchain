/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hisahi.u020_toolchain.cpu.instructions;

import com.github.hisahi.u020_toolchain.cpu.Hardware;
import com.github.hisahi.u020_toolchain.cpu.StandardMemory;
import com.github.hisahi.u020_toolchain.cpu.UCPU16;
import com.github.hisahi.u020_toolchain.cpu.addressing.AddressingMode;
import static com.github.hisahi.u020_toolchain.cpu.instructions.BinaryInstructionTest.cpu;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hopea
 */
public class UnaryInstructionTest {
    
    public static UCPU16 cpu;
    public UnaryInstructionTest() {
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

    public static int executeWithA(IInstruction instr, int a) {
        cpu.writeRegister(0, a);
        instr.execute(cpu, AddressingMode.REG_A, AddressingMode.NW, 0, 0);
        return cpu.readRegister(0);
    }
    
    public static int randomWord() {
        return (int) (Math.random() * 65536);
    }

    @Test
    public void testJSR() {
        cpu.setPC(0x2000);
        executeWithA(Instruction.JSR, 0x4000);
        assertEquals("JSR pushes 0 or >1 values", 0xFFFF, cpu.getSP());
        assertEquals("JSR pushes wrong value", 0x2000, cpu.getMemory().read(cpu.getSP()));
        assertEquals("JSR doesn't set PC correctly", 0x4000, cpu.getPC());
    }

    @Test
    public void testBSR() {
        cpu.setPC(0x2000);
        executeWithA(Instruction.BSR, 0x4000);
        assertEquals("BSR pushes 0 or >1 values", 0xFFFF, cpu.getSP());
        assertEquals("BSR pushes wrong value", 0x2000, cpu.getMemory().read(cpu.getSP()));
        assertEquals("BSR doesn't set PC correctly", 0x6000, cpu.getPC());
    }

    @Test
    public void testIAG() {
        int r1 = randomWord();
        cpu.setIA(r1);
        assertEquals("IAG does not get IA correctly", cpu.getIA(), executeWithA(Instruction.IAG, 0));
    }

    @Test
    public void testIAS() {
        int r1 = randomWord();
        executeWithA(Instruction.IAS, r1);
        assertEquals("IAS does not set IA correctly", cpu.getIA(), r1);
        assertEquals("IAS modifies the register which it shouldn't", cpu.readRegister(0), r1);
    }

    @Test
    public void testRFI() {
        int oldpc = randomWord();
        int olda = randomWord();
        cpu.stackPush(oldpc);
        cpu.stackPush(olda);
        executeWithA(Instruction.RFI, 0);
        assertEquals("RFI should pop 2 values", 0, cpu.getSP());
        assertEquals("RFI does not pop PC correctly", oldpc, cpu.getPC());
        assertEquals("RFI does not pop A correctly", olda, cpu.readRegister(0));
        assertFalse("RFI does not disable interrupt queueing", cpu.areInterruptsBeingQueued());
    }

    @Test
    public void testIAQ() {
        executeWithA(Instruction.IAQ, 0);
        assertFalse("IAQ does not disable interrupt queueing correctly", cpu.areInterruptsBeingQueued());
        executeWithA(Instruction.IAQ, 1);
        assertTrue("IAQ does not enable interrupt queueing correctly", cpu.areInterruptsBeingQueued());
    }

    @Test
    public void testHWN() {
        DummyHardware hw = new DummyHardware(cpu);
        cpu.addDevice(hw);
        assertEquals("HWN does not return correct number of devices", 1, executeWithA(Instruction.HWN, 0));
    }

    @Test
    public void testHWQ() {
        DummyHardware hw = new DummyHardware(cpu);
        cpu.addDevice(hw);
        executeWithA(Instruction.HWQ, 0);
        assertEquals("HWQ fails to set register A", 0x0000, cpu.readRegister(0));
        assertEquals("HWQ fails to set register B", 0x1111, cpu.readRegister(1));
        assertEquals("HWQ fails to set register C", 0x2222, cpu.readRegister(2));
        assertEquals("HWQ fails to set register X", 0x3333, cpu.readRegister(3));
        assertEquals("HWQ fails to set register Y", 0x4444, cpu.readRegister(4));
    }

    @Test
    public void testHWI() {
        DummyHardware hw = new DummyHardware(cpu);
        cpu.addDevice(hw);
        executeWithA(Instruction.HWI, 0);
        assertEquals("HWI fails to call device", 1, hw.called);
    }

    @Test
    public void testSXB() {
        assertEquals("SXB fails to sign-extend", 0x0000, executeWithA(Instruction.SXB, 0x00));
        assertEquals("SXB fails to sign-extend", 0x007F, executeWithA(Instruction.SXB, 0x7F));
        assertEquals("SXB fails to sign-extend", 0xFF80, executeWithA(Instruction.SXB, 0x80));
        assertEquals("SXB fails to sign-extend", 0xFFC2, executeWithA(Instruction.SXB, 0xC2));
        assertEquals("SXB fails to sign-extend", 0xFFFF, executeWithA(Instruction.SXB, 0xFF));
        assertEquals("SXB fails to sign-extend", 0x0000, executeWithA(Instruction.SXB, 0xFF00));
        assertEquals("SXB fails to sign-extend", 0x007F, executeWithA(Instruction.SXB, 0xFF7F));
        assertEquals("SXB fails to sign-extend", 0xFF80, executeWithA(Instruction.SXB, 0xFF80));
        assertEquals("SXB fails to sign-extend", 0xFFC2, executeWithA(Instruction.SXB, 0xFFC2));
        assertEquals("SXB fails to sign-extend", 0xFFFF, executeWithA(Instruction.SXB, 0xFFFF));
    }

    @Test
    public void testSWP() {
        assertEquals("SWP fails to swap bytes", 0x0000, executeWithA(Instruction.SWP, 0x0000));
        assertEquals("SWP fails to swap bytes", 0xFF00, executeWithA(Instruction.SWP, 0x00FF));
        assertEquals("SWP fails to swap bytes", 0x00FF, executeWithA(Instruction.SWP, 0xFF00));
        assertEquals("SWP fails to swap bytes", 0x3412, executeWithA(Instruction.SWP, 0x1234));
    }
    
    class DummyHardware extends Hardware {
        int called;
        
        public DummyHardware(UCPU16 cpu) {
            super(cpu);
            this.called = 0;
        }
        
        @Override
        protected long hardwareId() {
            return 0x11110000;
        }

        @Override
        protected int hardwareVersion() {
            return 0x2222;
        }

        @Override
        protected long hardwareManufacturer() {
            return 0x44443333;
        }

        @Override
        public void hwi(UCPU16 cpu) {
            ++this.called;
        }

        @Override
        public void tick() {
        }
        
    }
}
