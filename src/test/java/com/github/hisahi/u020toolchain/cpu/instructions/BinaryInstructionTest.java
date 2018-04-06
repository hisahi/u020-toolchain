
package com.github.hisahi.u020toolchain.cpu.instructions;

import com.github.hisahi.u020toolchain.cpu.StandardMemory;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.cpu.addressing.AddressingMode;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class BinaryInstructionTest {
    
    public static UCPU16 cpu;
    public BinaryInstructionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        cpu = new UCPU16(new StandardMemory());
    }
    
    @Before
    public void setUp() {
        cpu.reset();
    }
    
    public static int randomWord() {
        return (int) (Math.random() * 65536);
    }

    public static int executeWithA(IInstruction instr, int b, int a) {
        cpu.writeRegister(0, b);
        instr.execute(cpu, AddressingMode.NW, AddressingMode.REG_A, a, 0);
        return cpu.readRegister(0);
    }
    
    @Test
    public void testInstructionSET() {
        int r1 = randomWord();
        assertEquals("SET did not modify the value correctly", r1, executeWithA(Instruction.SET, 0, r1));
    }
    
    @Test
    public void testInstructionSETDoesNotModifyEX() {
        int oldex = cpu.getEX();
        executeWithA(Instruction.SET, 0, 6);
        assertEquals("EX was unexpectedly modified by SET", oldex, cpu.getEX());
    }
    
    @Test
    public void testInstructionADD() {
        assertEquals("ADD did not compute the value correctly", 5, executeWithA(Instruction.ADD, 2, 3));
        assertEquals("EX was incorrectly modified by ADD", 0, cpu.getEX());
    }
    
    @Test
    public void testInstructionADDOverflow() {
        assertEquals("ADD did not compute the value correctly", 5, executeWithA(Instruction.ADD, 0xFFF2, 0x0013));
        assertEquals("EX was incorrectly modified by ADD", 1, cpu.getEX());
    }
    
    @Test
    public void testInstructionSUB() {
        assertEquals("SUB did not compute the value correctly", 3, executeWithA(Instruction.SUB, 5, 2));
        assertEquals("EX was incorrectly modified by SUB", 0, cpu.getEX());
    }
    
    @Test
    public void testInstructionSUBUnderflow() {
        assertEquals("SUB did not compute the value correctly", 0xfffd, executeWithA(Instruction.SUB, 2, 5));
        assertEquals("EX was incorrectly modified by SUB", 0xffff, cpu.getEX());
    }
    
    @Test
    public void testInstructionMULLowWord() {
        assertEquals("MUL did not compute the value correctly", 10, executeWithA(Instruction.MUL, 5, 2));
        assertEquals("EX was incorrectly modified by MUL", 0, cpu.getEX());
    }
    
    @Test
    public void testInstructionMULHighWord() {
        assertEquals("MUL did not compute the value correctly", 0x6009, executeWithA(Instruction.MUL, 0x1003, 0x1003));
        assertEquals("EX was incorrectly modified by MUL", 0x0100, cpu.getEX());
    }
    
    @Test
    public void testInstructionMLILowWord() {
        assertEquals("MLI did not compute the value correctly", 10, executeWithA(Instruction.MLI, 5, 2));
        assertEquals("EX was incorrectly modified by MLI", 0, cpu.getEX());
    }
    
    @Test
    public void testInstructionMLIHighWord() {
        assertEquals("MLI did not compute the value correctly", 0x6009, executeWithA(Instruction.MLI, 0x1003, 0x1003));
        assertEquals("EX was incorrectly modified by MLI", 0x0100, cpu.getEX());
    }
    
    @Test
    public void testInstructionMLISigned() {
        assertEquals("MLI did not compute the value correctly", 0xFD00, executeWithA(Instruction.MLI, 0xFF00, 0xFF03));
        assertEquals("EX was incorrectly modified by MLI", 0x0000, cpu.getEX());
    }
    
    @Test
    public void testInstructionDIVInteger() {
        assertEquals("DIV did not compute the value correctly", 3, executeWithA(Instruction.DIV, 6, 2));
        assertEquals("EX was incorrectly modified by DIV", 0, cpu.getEX());
    }
    
    @Test
    public void testInstructionDIVFraction() {
        assertEquals("DIV did not compute the value correctly", 2, executeWithA(Instruction.DIV, 5, 2));
        assertEquals("EX was incorrectly modified by DIV", 0x8000, cpu.getEX());
    }
    
    @Test
    public void testInstructionDIVByZero() {
        assertEquals("DIV did not compute the value correctly", 0, executeWithA(Instruction.DIV, 3, 0));
        assertEquals("EX was incorrectly modified by DIV", 0, cpu.getEX());
    }
    
    @Test
    public void testInstructionDVIInteger() {
        assertEquals("DVI did not compute the value correctly", 3, executeWithA(Instruction.DVI, 6, 2));
        assertEquals("EX was incorrectly modified by DVI", 0, cpu.getEX());
    }
    
    @Test
    public void testInstructionDVIFraction() {
        assertEquals("DVI did not compute the value correctly", 2, executeWithA(Instruction.DVI, 5, 2));
        assertEquals("EX was incorrectly modified by DVI", 0x8000, cpu.getEX());
    }
    
    @Test
    public void testInstructionDVISigned() {
        assertEquals("DVI did not compute the value correctly", 0xFDA9, executeWithA(Instruction.DVI, 0x1066, 0xFFF9));
        assertEquals("EX was incorrectly modified by DVI", 0x4925, cpu.getEX());
    }
    
    @Test
    public void testInstructionDVIByZero() {
        assertEquals("DVI did not compute the value correctly", 0, executeWithA(Instruction.DVI, 3, 0));
        assertEquals("EX was incorrectly modified by DVI", 0, cpu.getEX());
    }
    
    @Test
    public void testInstructionMOD() {
        assertEquals("MOD did not compute the value correctly", 2, executeWithA(Instruction.MOD, 8, 3));
        assertEquals("MOD did not compute the value correctly", 6, executeWithA(Instruction.MOD, 27, 7));
    }
    
    @Test
    public void testInstructionMODByZero() {
        assertEquals("MOD did not compute the value correctly", 0, executeWithA(Instruction.MOD, 11, 0));
    }
    
    @Test
    public void testInstructionMDI() {
        assertEquals("MDI did not compute the value correctly", 2, executeWithA(Instruction.MDI, 8, 3));
        assertEquals("MDI did not compute the value correctly", 6, executeWithA(Instruction.MDI, 27, 7));
    }
    
    @Test
    public void testInstructionMDISigned() {
        assertEquals("MDI did not compute the value correctly", 0xfff9, executeWithA(Instruction.MDI, 0xfff9, 16));
    }
    
    @Test
    public void testInstructionMDIByZero() {
        assertEquals("MDI did not compute the value correctly", 0, executeWithA(Instruction.MDI, 11, 0));
    }
    
    @Test
    public void testInstructionAND() {
        assertEquals("AND did not compute the value correctly", 2, executeWithA(Instruction.AND, 2, 3));
        assertEquals("AND did not compute the value correctly", 0x1504, executeWithA(Instruction.AND, 0x5555, 0xb726));
    }
    
    @Test
    public void testInstructionBOR() {
        assertEquals("BOR did not compute the value correctly", 3, executeWithA(Instruction.BOR, 2, 3));
        assertEquals("BOR did not compute the value correctly", 0xf777, executeWithA(Instruction.BOR, 0x5555, 0xb726));
    }
    
    @Test
    public void testInstructionXOR() {
        assertEquals("XOR did not compute the value correctly", 1, executeWithA(Instruction.XOR, 2, 3));
        assertEquals("XOR did not compute the value correctly", 0xe273, executeWithA(Instruction.XOR, 0x5555, 0xb726));
    }
    
    @Test
    public void testInstructionSHR() {
        assertEquals("SHR did not compute the value correctly", 2, executeWithA(Instruction.SHR, 8, 2));
        assertEquals("EX was incorrectly modified by SHR", 0, cpu.getEX());
    }
    
    @Test
    public void testInstructionSHRToEX() {
        assertEquals("SHR did not compute the value correctly", 0x0777, executeWithA(Instruction.SHR, 0xeeee, 5));
        assertEquals("EX was incorrectly modified by SHR", 0x7000, cpu.getEX());
    }
    
    @Test
    public void testInstructionSHRUnsigned() {
        assertEquals("SHR did not compute the value correctly", 0x7800, executeWithA(Instruction.SHR, 0xF001, 1));
        assertEquals("EX was incorrectly modified by SHR", 0x8000, cpu.getEX());
    }
    
    @Test
    public void testInstructionASR() {
        assertEquals("ASR did not compute the value correctly", 2, executeWithA(Instruction.ASR, 8, 2));
        assertEquals("EX was incorrectly modified by ASR", 0, cpu.getEX());
    }
    
    @Test
    public void testInstructionASRToEX() {
        assertEquals("ASR did not compute the value correctly", 0x0077, executeWithA(Instruction.ASR, 0x0eee, 5));
        assertEquals("EX was incorrectly modified by ASR", 0x7000, cpu.getEX());
    }
    
    @Test
    public void testInstructionASRSigned() {
        assertEquals("ASR did not compute the value correctly", 0xF800, executeWithA(Instruction.ASR, 0xF001, 1));
        assertEquals("EX was incorrectly modified by ASR", 0x8000, cpu.getEX());
    }
    
    @Test
    public void testInstructionSHL() {
        assertEquals("SHL did not compute the value correctly", 512, executeWithA(Instruction.SHL, 32, 4));
        assertEquals("EX was incorrectly modified by SHL", 0, cpu.getEX());
    }
    
    @Test
    public void testInstructionSHLToEX() {
        assertEquals("SHL did not compute the value correctly", 0x8000, executeWithA(Instruction.SHL, 0xc000, 1));
        assertEquals("EX was incorrectly modified by SHL", 0x0001, cpu.getEX());
    }
    
    @Test
    public void testInstructionADX() {
        cpu.setEX(0);
        assertEquals("ADX did not compute the value correctly", 5, executeWithA(Instruction.ADX, 2, 3));
        assertEquals("EX was incorrectly modified by ADX", 0, cpu.getEX());
    }
    
    @Test
    public void testInstructionADXOverflow() {
        cpu.setEX(0);
        assertEquals("ADX did not compute the value correctly", 5, executeWithA(Instruction.ADX, 0xFFF2, 0x0013));
        assertEquals("EX was incorrectly modified by ADX", 1, cpu.getEX());
    }
    
    @Test
    public void testInstructionADXOverflowWithEX() {
        cpu.setEX(1000);
        assertEquals("ADX did not compute the value correctly", 0x07cf, executeWithA(Instruction.ADX, 1000, 0xffff));
        assertEquals("EX was incorrectly modified by ADX", 1, cpu.getEX());
    }
    
    @Test
    public void testInstructionSBX() {
        cpu.setEX(0);
        assertEquals("SBX did not compute the value correctly", 3, executeWithA(Instruction.SBX, 5, 2));
        assertEquals("EX was incorrectly modified by SBX", 0, cpu.getEX());
    }
    
    @Test
    public void testInstructionSBXWithUnderflow() {
        cpu.setEX(0);
        assertEquals("SBX did not compute the value correctly", 0xfffd, executeWithA(Instruction.SBX, 2, 5));
        assertEquals("EX was incorrectly modified by SBX", 0xffff, cpu.getEX());
    }
    
    @Test
    public void testInstructionSBXWithUnderflowAndEX() {
        cpu.setEX(1);
        assertEquals("SBX did not compute the value correctly", 0xffff, executeWithA(Instruction.SBX, 1, 3));
        assertEquals("EX was incorrectly modified by SBX", 0xffff, cpu.getEX());
    }
    
    @Test
    public void testInstructionSBXUnderflowCancelledByEX() {
        cpu.setEX(0xffff);
        assertEquals("SBX did not compute the value correctly", 0x0000, executeWithA(Instruction.SBX, 2, 1));
        assertEquals("EX was incorrectly modified by SBX", 0x0000, cpu.getEX());
    }
    
    @Test
    public void testInstructionROR() {
        cpu.setEX(0);
        assertEquals("ROR did not compute the value correctly", 2, executeWithA(Instruction.ROR, 8, 2));
        assertEquals("EX was incorrectly modified by ROR", 0, cpu.getEX());
    }
    
    @Test
    public void testInstructionRORToEX() {
        cpu.setEX(0);
        assertEquals("ROR did not compute the value correctly", 0x0777, executeWithA(Instruction.ROR, 0xeeee, 5));
        assertEquals("EX was incorrectly modified by ROR", 0x7000, cpu.getEX());
    }
    
    @Test
    public void testInstructionRORUnsignedToEX() {
        cpu.setEX(0);
        assertEquals("ROR did not compute the value correctly", 0x7800, executeWithA(Instruction.ROR, 0xF001, 1));
        assertEquals("EX was incorrectly modified by ROR", 0x8000, cpu.getEX());
    }
    
    @Test
    public void testInstructionRORCarryOverFrom() {
        cpu.setEX(0);
        assertEquals("ROR did not compute the value correctly", 0x7800, executeWithA(Instruction.ROR, 0xF001, 1));
        assertEquals("EX was incorrectly modified by ROR", 0x8000, cpu.getEX());
    }
    
    @Test
    public void testInstructionRORCarryOverTo() {
        cpu.setEX(0x8000);
        assertEquals("ROR did not compute the value correctly", 0x8001, executeWithA(Instruction.ROR, 0x0003, 1));
        assertEquals("EX was incorrectly modified by ROR", 0x8000, cpu.getEX());
    }
    
    @Test
    public void testInstructionROL() {
        cpu.setEX(0);
        assertEquals("ROL did not compute the value correctly", 512, executeWithA(Instruction.ROL, 32, 4));
        assertEquals("EX was incorrectly modified by ROL", 0, cpu.getEX());
    }
    
    @Test
    public void testInstructionROLToEx() {
        cpu.setEX(0);
        assertEquals("ROL did not compute the value correctly", 0x8000, executeWithA(Instruction.ROL, 0xc000, 1));
        assertEquals("EX was incorrectly modified by ROL", 0x0001, cpu.getEX());
    }
    
    @Test
    public void testInstructionROLCarryOverFrom() {
        cpu.setEX(0);
        assertEquals("ROL did not compute the value correctly", 0x8000, executeWithA(Instruction.ROL, 0xc000, 1));
        assertEquals("EX was incorrectly modified by ROL", 0x0001, cpu.getEX());
    }
    
    @Test
    public void testInstructionROLCarryOverTo() {
        cpu.setEX(0x0001);
        assertEquals("ROL did not compute the value correctly", 0x8001, executeWithA(Instruction.ROL, 0xc000, 1));
        assertEquals("EX was incorrectly modified by ROL", 0x0001, cpu.getEX());
    }
    
    @Test
    public void testInstructionSTI() {
        cpu.writeRegister(6, 0);
        cpu.writeRegister(7, 0);
        int r1 = randomWord();
        assertEquals("STI did not modify the value correctly", r1, executeWithA(Instruction.STI, 0, r1));
    }
    
    @Test
    public void testInstructionSTIDoesNotModifyEX() {
        int oldex = cpu.getEX();
        cpu.writeRegister(6, 0);
        cpu.writeRegister(7, 0);
        executeWithA(Instruction.STI, 0, 5);
        assertEquals("EX was unexpectedly modified by STI", oldex, cpu.getEX());
    }
    
    @Test
    public void testInstructionSTIIncreasesRegs() {
        cpu.writeRegister(6, 0);
        cpu.writeRegister(7, 0);
        executeWithA(Instruction.STI, 0, 5);
        assertEquals("STI did not increase register I correctly", 1, cpu.readRegister(6));
        assertEquals("STI did not increase register J correctly", 1, cpu.readRegister(7));
    }
    
    @Test
    public void testInstructionSTD() {
        cpu.writeRegister(6, 0);
        cpu.writeRegister(7, 0);
        int r1 = randomWord();
        assertEquals("STD did not modify the value correctly", r1, executeWithA(Instruction.STD, 0, r1));
    }
    
    @Test
    public void testInstructionSTDDoesNotModifyEX() {
        int oldex = cpu.getEX();
        cpu.writeRegister(6, 0);
        cpu.writeRegister(7, 0);
        executeWithA(Instruction.STD, 0, 4);
        assertEquals("EX was unexpectedly modified by STD", oldex, cpu.getEX());
    }
    
    @Test
    public void testInstructionSTDDecreasesRegs() {
        cpu.writeRegister(6, 0);
        cpu.writeRegister(7, 0);
        executeWithA(Instruction.STD, 0, 4);
        assertEquals("STD did not decrease register I correctly", 0xffff, cpu.readRegister(6));
        assertEquals("STD did not decrease register J correctly", 0xffff, cpu.readRegister(7));
    }
}
