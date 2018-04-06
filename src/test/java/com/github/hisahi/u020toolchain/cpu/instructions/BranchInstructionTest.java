
package com.github.hisahi.u020toolchain.cpu.instructions;

import com.github.hisahi.u020toolchain.cpu.StandardMemory;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.AddressingMode;
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

    public static boolean checkBranchWithLiterals(IInstruction instr, int b, int a) {
        cpu.reset();
        instr.execute(cpu, AddressingMode.NW, AddressingMode.NW, a, b);
        return !cpu.willSkip();
    }
    
    @Test
    public void testIFBTrue() {
        assertTrue(checkBranchWithLiterals(Instruction.IFB, 5, 3));
    }
    
    @Test
    public void testIFBFalse() {
        assertFalse(checkBranchWithLiterals(Instruction.IFB, 4, 1));
    }
    
    @Test
    public void testIFCFalse() {
        assertFalse(checkBranchWithLiterals(Instruction.IFC, 5, 3));
    }
    
    @Test
    public void testIFCTrue() {
        assertTrue(checkBranchWithLiterals(Instruction.IFC, 4, 1));
    }
    
    @Test
    public void testIFETrue() {
        assertTrue(checkBranchWithLiterals(Instruction.IFE, 5, 5));
    }
    
    @Test
    public void testIFEFalse() {
        assertFalse(checkBranchWithLiterals(Instruction.IFE, 0, 1));
    }
    
    @Test
    public void testIFNFalse() {
        assertFalse(checkBranchWithLiterals(Instruction.IFN, 5, 5));
    }
    
    @Test
    public void testIFNTrue() {
        assertTrue(checkBranchWithLiterals(Instruction.IFN, 0, 1));
    }
    
    @Test
    public void testIFGTrue() {
        assertTrue(checkBranchWithLiterals(Instruction.IFG, 5, 3));
    }
    
    @Test
    public void testIFGIsUnsignedTrue() {
        assertTrue(checkBranchWithLiterals(Instruction.IFG, 0xffff, 3));
    }
    
    @Test
    public void testIFGIsUnsignedFalse() {
        assertFalse(checkBranchWithLiterals(Instruction.IFG, 0x00ff, 0xffff));
    }
    
    @Test
    public void testIFGIfEqualThenFalse() {
        assertFalse(checkBranchWithLiterals(Instruction.IFG, 5, 5));
    }
    
    @Test
    public void testIFATrue() {
        assertTrue(checkBranchWithLiterals(Instruction.IFA, 5, 3));
    }
    
    @Test
    public void testIFAIsUnsignedTrue() {
        assertTrue(checkBranchWithLiterals(Instruction.IFA, 0x00ff, 0xffff));
    }
    
    @Test
    public void testIFAIsUnsignedFalse() {
        assertFalse(checkBranchWithLiterals(Instruction.IFA, 0xffff, 3));
    }
    
    @Test
    public void testIFAIfEqualThenFalse() {
        assertFalse(checkBranchWithLiterals(Instruction.IFA, 5, 5));
    }
    
    @Test
    public void testIFLIsSignedTrue() {
        assertTrue(checkBranchWithLiterals(Instruction.IFL, 0x00ff, 0xffff));
    }
    
    @Test
    public void testIFLFalse() {
        assertFalse(checkBranchWithLiterals(Instruction.IFL, 5, 3));
    }
    
    @Test
    public void testIFLIsSignedFalse() {
        assertFalse(checkBranchWithLiterals(Instruction.IFL, 0xffff, 3));
    }
    
    @Test
    public void testIFLIfEqualThenFalse() {
        assertFalse(checkBranchWithLiterals(Instruction.IFL, 5, 5));
    }
    
    @Test
    public void testIFUIsSignedTrue() {
        assertTrue(checkBranchWithLiterals(Instruction.IFU, 0xffff, 3));
    }
    
    @Test
    public void testIFUFalse() {
        assertFalse(checkBranchWithLiterals(Instruction.IFU, 5, 3));
    }
    
    @Test
    public void testIFUIsSignedFalse() {
        assertFalse(checkBranchWithLiterals(Instruction.IFU, 0x00ff, 0xffff));
    }
    
    @Test
    public void testIFUIfEqualThenFalse() {
        assertFalse(checkBranchWithLiterals(Instruction.IFU, 5, 5));
    }
}
