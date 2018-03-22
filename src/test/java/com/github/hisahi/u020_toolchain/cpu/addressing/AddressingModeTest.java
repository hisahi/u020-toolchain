/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hisahi.u020_toolchain.cpu.addressing;

import com.github.hisahi.u020_toolchain.cpu.StandardMemory;
import com.github.hisahi.u020_toolchain.cpu.UCPU16;
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
public class AddressingModeTest {
    
    private static UCPU16 cpu;
    
    public AddressingModeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new UCPU16(new StandardMemory());
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    public static int randomWord() {
        return (int) (Math.random() * 65536);
    }

    private static String hex(int r4) {
        return String.format("$%04x",r4);
    }

    @Test
    public void directRegisterAddressingModes() {
        for (int i = 0; i < 8; ++i) {
            IAddressingMode am = AddressingMode.decode(i);
            int r1 = randomWord();
            int r2 = randomWord();
            cpu.writeRegister(i, r1);
            assertEquals("direct read not working correctly (expected " + hex(r1) + ", but got " + hex(am.read(cpu, 0)) + ")", r1, am.read(cpu, 0));
            am.write(cpu, 0, r2);
            assertEquals("direct write not working correctly (expected " + hex(r2) + ", but got " + hex(cpu.readRegister(i)) + ")", r2, cpu.readRegister(i));
        }
    }

    @Test
    public void indirectRegisterAddressingModes() {
        for (int i = 0; i < 8; ++i) {
            IAddressingMode am = AddressingMode.decode(8+i);
            int r1 = randomWord();
            int r2 = randomWord();
            int r3 = randomWord();
            int r4 = randomWord();
            cpu.writeRegister(i, r1);
            cpu.getMemory().write(r1, r3);
            assertEquals("indirect read not working correctly (expected " + hex(r1) + ":" + hex(r3) + ", but got " + hex(am.read(cpu, 0)) + ")", r3, am.read(cpu, 0));
            cpu.writeRegister(i, r2);
            am.write(cpu, 0, r4);
            assertEquals("indirect write not working correctly (expected " + hex(r4) + ", but got " + hex(r2) + ":" + cpu.getMemory().read(r2) + ")", r4, cpu.getMemory().read(r2));
        }
    }

    @Test
    public void indirectRegisterPlusWordAddressingModes() {
        int nw = randomWord();
        for (int i = 0; i < 8; ++i) {
            IAddressingMode am = AddressingMode.decode(16+i);
            int r1 = randomWord();
            int r2 = randomWord();
            int r3 = randomWord();
            int r4 = randomWord();
            cpu.writeRegister(i, r1);
            cpu.getMemory().write((r1+nw)&0xFFFF, r3);
            assertEquals("indirect read + NW not working correctly (expected " + hex(r1) + ":" + hex(r3) + ", but got " + hex(am.read(cpu, nw)) + ")", r3, am.read(cpu, nw));
            cpu.writeRegister(i, r2);
            am.write(cpu, nw, r4);
            assertEquals("indirect write + NW not working correctly (expected " + hex(r4) + ", but got " + hex(r2) + ":" + hex(cpu.getMemory().read((r2+nw)&0xFFFF)) + ")", r4, cpu.getMemory().read((r2+nw)&0xFFFF));
        }
    }
    
    @Test
    public void stackPushPopAddressingMode() {
        cpu.reset();
        IAddressingMode am = AddressingMode.STACK;
        int r1 = randomWord();
        assertEquals("CPU not working properly: initial SP wrong, expected " + hex(0) + " but got " + hex(cpu.getSP()), 0, cpu.getSP());
        am.write(cpu, 0, r1);
        assertEquals("SP wrong, expected " + hex(0xFFFF) + " but got " + hex(cpu.getSP()), 0xFFFF, cpu.getSP());
        assertEquals("stack was not written to correctly: expected " + hex(r1) + " but got " + hex(cpu.getMemory().read(0xFFFF)), r1, cpu.getMemory().read(0xFFFF));
        int readval = am.read(cpu, 0);
        assertEquals("stack was not read from correctly: expected " + hex(r1) + " but got " + hex(readval), r1, readval);
        assertEquals("SP wrong, expected " + hex(0) + " but got " + hex(cpu.getSP()), 0, cpu.getSP());
    }
    
    @Test
    public void stackPeekAddressingMode() {
        IAddressingMode am = AddressingMode.STACK_PEEK;
        int r1 = randomWord();
        int r2 = randomWord();
        cpu.stackPush(r1);
        assertEquals("PEEK reading failed: expected " + hex(r1) + " but got " + hex(am.read(cpu, 0)), r1, am.read(cpu, 0));
        am.write(cpu, 0, r2);
        int popval = cpu.stackPop();
        assertEquals("PEEK writing failed: expected " + hex(r2) + " but got " + hex(popval), r2, popval);
    }
    
    @Test
    public void stackPickAddressingMode() {
        int offset = ((int)(Math.random() * 512) + 1);
        int nw = ((int)(Math.random() * offset) + 1);
        IAddressingMode am = AddressingMode.STACK_PICK;
        int r1 = randomWord();
        int r2 = randomWord();
        for (int i = 0; i < offset; ++i)
            cpu.stackPush((r1 + (offset - i - 1)) & 0xFFFF);
        assertEquals("PICK reading failed: expected " + hex((r1 + nw) & 0xFFFF) + " but got " + hex(am.read(cpu, nw)), (r1 + nw) & 0xFFFF, am.read(cpu, nw));
        am.write(cpu, nw, r2);
        assertEquals("PICK writing failed: expected " + hex(r2) + " but got " + hex(cpu.getMemory().read(cpu.getSP() + nw)), r2, cpu.getMemory().read(cpu.getSP() + nw));
        cpu.reset();
    }
    
    @Test
    public void spAddressingMode() {
        IAddressingMode amsp = AddressingMode.REG_SP;
        int r1sp = randomWord();
        int r2sp = randomWord();
        cpu.setSP(r1sp);
        assertEquals("SP reading failed: expected " + hex(r1sp) + " but got " + hex(amsp.read(cpu, 0)), r1sp, amsp.read(cpu, 0));
        amsp.write(cpu, 0, r2sp);
        assertEquals("SP writing failed: expected " + hex(r2sp) + " but got " + hex(cpu.getSP()), r2sp, cpu.getSP());
        cpu.reset();
    }
    
    @Test
    public void pcAddressingMode() {
        IAddressingMode ampc = AddressingMode.REG_PC;
        int r1pc = randomWord();
        int r2pc = randomWord();
        cpu.setPC(r1pc);
        assertEquals("PC reading failed: expected " + hex(r1pc) + " but got " + hex(ampc.read(cpu, 0)), r1pc, ampc.read(cpu, 0));
        ampc.write(cpu, 0, r2pc);
        assertEquals("PC writing failed: expected " + hex(r2pc) + " but got " + hex(cpu.getPC()), r2pc, cpu.getPC());
        cpu.reset();
    }
    
    @Test
    public void exAddressingMode() {
        IAddressingMode amex = AddressingMode.REG_EX;
        int r1ex = randomWord();
        int r2ex = randomWord();
        cpu.setEX(r1ex);
        assertEquals("EX reading failed: expected " + hex(r1ex) + " but got " + hex(amex.read(cpu, 0)), r1ex, amex.read(cpu, 0));
        amex.write(cpu, 0, r2ex);
        assertEquals("EX writing failed: expected " + hex(r2ex) + " but got " + hex(cpu.getEX()), r2ex, cpu.getEX());
        cpu.reset();
    }
    
    @Test
    public void indirectWordAddressingMode() {
        IAddressingMode am = AddressingMode.IND_NW;
        int r1 = randomWord();
        int r2 = randomWord();
        int r3 = randomWord();
        cpu.getMemory().write(r1, r2);
        assertEquals("indirect word reading failed: expected " + hex(r2) + " but got " + hex(am.read(cpu, r1)), r2, am.read(cpu, r1));
        am.write(cpu, r1, r3);
        assertEquals("indirect word writing failed: expected " + hex(r3) + " but got " + hex(cpu.getMemory().read(r1)), r3, cpu.getMemory().read(r1));
    }
    
    @Test
    public void directWordAddressingMode() {
        IAddressingMode am = AddressingMode.NW;
        int r1 = randomWord();
        assertEquals("direct word reading failed: expected " + hex(r1) + " but got " + hex(am.read(cpu, r1)), r1, am.read(cpu, r1));
    }
    
    @Test
    public void literalAddressingModes() {
        for (int i = 0; i < 32; ++i) {
            IAddressingMode am = AddressingMode.LITERAL[i];
            int exp = (i - 1) & 0xFFFF;
            assertEquals("small literal reading failed: expected " + hex(exp) + " but got " + hex(am.read(cpu, 0)), exp, am.read(cpu, 0));
        }
    }
}
