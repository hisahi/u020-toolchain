
package com.github.hisahi.u020toolchain.cpu.instructions;

import com.github.hisahi.u020toolchain.cpu.StandardMemory;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.AddressingMode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class BranchInstructionTest {
    public static UCPU16 cpu;
    public BranchInstructionTest() {
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

    public static boolean checkBranchWithLiterals(IInstruction instr, int b, int a) {
        cpu.reset();
        instr.execute(cpu, AddressingMode.NW, AddressingMode.NW, a, b);
        return !cpu.willSkip();
    }
    
    @Test
    public void testIFB() {
        assertTrue(checkBranchWithLiterals(Instruction.IFB, 5, 3));
        assertFalse(checkBranchWithLiterals(Instruction.IFB, 4, 1));
    }
    
    @Test
    public void testIFC() {
        assertFalse(checkBranchWithLiterals(Instruction.IFC, 5, 3));
        assertTrue(checkBranchWithLiterals(Instruction.IFC, 4, 1));
    }
    
    @Test
    public void testIFE() {
        assertTrue(checkBranchWithLiterals(Instruction.IFE, 5, 5));
        assertFalse(checkBranchWithLiterals(Instruction.IFE, 0, 1));
    }
    
    @Test
    public void testIFN() {
        assertFalse(checkBranchWithLiterals(Instruction.IFN, 5, 5));
        assertTrue(checkBranchWithLiterals(Instruction.IFN, 0, 1));
    }
    
    @Test
    public void testIFG() {
        assertTrue(checkBranchWithLiterals(Instruction.IFG, 5, 3));
        assertTrue(checkBranchWithLiterals(Instruction.IFG, 0xffff, 3));
        assertFalse(checkBranchWithLiterals(Instruction.IFG, 0x00ff, 0xffff));
        assertFalse(checkBranchWithLiterals(Instruction.IFG, 5, 5));
    }
    
    @Test
    public void testIFA() {
        assertTrue(checkBranchWithLiterals(Instruction.IFA, 5, 3));
        assertFalse(checkBranchWithLiterals(Instruction.IFA, 0xffff, 3));
        assertTrue(checkBranchWithLiterals(Instruction.IFA, 0x00ff, 0xffff));
        assertFalse(checkBranchWithLiterals(Instruction.IFA, 5, 5));
    }
    
    @Test
    public void testIFL() {
        assertFalse(checkBranchWithLiterals(Instruction.IFL, 5, 3));
        assertFalse(checkBranchWithLiterals(Instruction.IFL, 0xffff, 3));
        assertTrue(checkBranchWithLiterals(Instruction.IFL, 0x00ff, 0xffff));
        assertFalse(checkBranchWithLiterals(Instruction.IFL, 5, 5));
    }
    
    @Test
    public void testIFU() {
        assertFalse(checkBranchWithLiterals(Instruction.IFU, 5, 3));
        assertTrue(checkBranchWithLiterals(Instruction.IFU, 0xffff, 3));
        assertFalse(checkBranchWithLiterals(Instruction.IFU, 0x00ff, 0xffff));
        assertFalse(checkBranchWithLiterals(Instruction.IFU, 5, 5));
    }
}
