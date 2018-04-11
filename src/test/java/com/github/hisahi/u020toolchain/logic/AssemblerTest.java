
package com.github.hisahi.u020toolchain.logic;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the UCPU-16 assembler.
 * 
 * @author hisahi
 */
public class AssemblerTest {
    
    public AssemblerTest() {
    }
    
    @Test
    public void assembleEmptyTest() {
        assertArrayEquals(new int[] {}, Assembler.assemble("").getBinary());
    }
    
    @Test
    public void assembleRegisterAddressingModeTest() {
        assertArrayEquals(new int[] {0x0020}, Assembler.assemble("JSR A").getBinary());
    }
    
    @Test
    public void assembleRegisterIndirectAddressingModeTest() {
        assertArrayEquals(new int[] {0x2a80}, Assembler.assemble("SXB [C]").getBinary());
    }
    
    @Test
    public void assembleRegisterIndirectPlusWordAddressingModeTest() {
        assertArrayEquals(new int[] {0x0a01, 0x0002}, Assembler.assemble("SET [A+2], C").getBinary());
    }
    
    @Test
    public void assembleStackPushAddressingModeTest() {
        assertArrayEquals(new int[] {0x0301}, Assembler.assemble("SET PUSH, A").getBinary());
    }
    
    @Test
    public void assembleStackPopAddressingModeTest() {
        assertArrayEquals(new int[] {0x6381}, Assembler.assemble("SET PC, POP").getBinary());
    }
    
    @Test
    public void assembleStackPeekAddressingModeTest() {
        assertArrayEquals(new int[] {0x9b22}, Assembler.assemble("ADD PEEK, 5").getBinary());
    }
    
    @Test
    public void assembleStackPeekAddressingModeAltSyntaxTest() {
        assertArrayEquals(new int[] {0x9b22}, Assembler.assemble("ADD [SP], 5").getBinary());
    }
    
    @Test
    public void assembleStackPickAddressingModeTest() {
        assertArrayEquals(new int[] {0x9344, 0x0001}, Assembler.assemble("MUL PICK 1, 3").getBinary());
    }
    
    @Test
    public void assembleStackPickAddressingModeAltSyntaxTest() {
        assertArrayEquals(new int[] {0x9344, 0x0001}, Assembler.assemble("MUL [SP+1], 3").getBinary());
    }
    
    @Test
    public void assembleSPAddressingModeTest() {
        assertArrayEquals(new int[] {0xa763}, Assembler.assemble("SUB SP, 8").getBinary());
    }
    
    @Test
    public void assemblePCAddressingModeTest() {
        assertArrayEquals(new int[] {0x7f81, 0x42a0}, Assembler.assemble("SET PC, 0x42a0").getBinary());
    }
    
    @Test
    public void assembleEXAddressingModeTest() {
        assertArrayEquals(new int[] {0x87a1}, Assembler.assemble("SET EX, 0").getBinary());
    }
    
    @Test
    public void assembleWordIndirectAddressingModeTest() {
        assertArrayEquals(new int[] {0x7820, 0xa000}, Assembler.assemble("JSR [0xa000]").getBinary());
    }
    
    @Test
    public void assembleWordAddressingModeTest() {
        assertArrayEquals(new int[] {0x87ec, 0x0000}, Assembler.assemble("XOR 0x0000, 0").getBinary());
    }
    
    @Test
    public void binaryBAfterATest() {
        assertArrayEquals(new int[] {0x7fc2, 0x1234, 0x5678}, Assembler.assemble("ADD [0x5678], 0x1234").getBinary());
    }
    
    @Test
    public void assembleRegisterIndirectPlusWordAddressingModeWithLabelTest() {
        assertArrayEquals(new int[] {0x0a01, 0x0006}, Assembler.assemble(":BTMP\nSET [A+BTMP+6], C").getBinary());
    }
    
    @Test
    public void assembleWordIndirectAddressingModeWithLabelTest() {
        assertArrayEquals(new int[] {0x7820, 0x0002}, Assembler.assemble(":BTMP\nJSR [BTMP+2]").getBinary());
    }
    
    @Test
    public void assembleWordAddressingModeWithLabelTest() {
        assertArrayEquals(new int[] {0x7c01, 0x2000}, Assembler.assemble(":BTMP\nSET A, BTMP+0x2000").getBinary());
    }
    
    @Test
    public void assembleLabelSubtractionTest() {
        assertArrayEquals(new int[] {0x7c01, 0xFFFF}, Assembler.assemble(":BTMP\nSET A, BTMP-1").getBinary());
    }
    
    @Test
    public void assembleNOPTest() {
        assertArrayEquals(new int[] {0x0000}, Assembler.assemble("NOP").getBinary());
    }
    
    @Test
    public void assembleDBGTest() {
        assertArrayEquals(new int[] {0x0060}, Assembler.assemble("DBG").getBinary());
    }
    
    @Test
    public void assembleRFITest() {
        assertArrayEquals(new int[] {0x0160}, Assembler.assemble("RFI").getBinary());
        assertArrayEquals(new int[] {0x0160}, Assembler.assemble("RFI A").getBinary());
    }
    
    @Test
    public void assembleSymbolTableTest() {
        AssemblerResult ar = Assembler.assemble(":BTMP\nSET A, BTMP+0x2000");
        assertEquals("0000 BTMP", ar.getSymbolTable());
    }
    
    @Test
    public void assembleSymbolTableDataTest() {
        AssemblerResult ar = Assembler.assemble("DAT 0x5555");
        assertEquals(".DATA 0000", ar.getSymbolTable());
    }
    
    @Test
    public void assembleSymbolTableAsciiTest() {
        AssemblerResult ar = Assembler.assemble("DAT \"e\"");
        assertEquals(".ASCII 0000", ar.getSymbolTable());
    }
    
    @Test
    public void assembleSymbolTableHideTest() {
        AssemblerResult ar = Assembler.assemble(".ORG 0x0001\nSET A, 1");
        assertEquals(".HIDE 0000", ar.getSymbolTable());
    }
    
    @Test
    public void allBinaryInstructionsSupportedTest() {
        assertArrayEquals(new int[] {0x8401}, Assembler.assemble("SET A, 0").getBinary());
        assertArrayEquals(new int[] {0x8402}, Assembler.assemble("ADD A, 0").getBinary());
        assertArrayEquals(new int[] {0x8403}, Assembler.assemble("SUB A, 0").getBinary());
        assertArrayEquals(new int[] {0x8404}, Assembler.assemble("MUL A, 0").getBinary());
        assertArrayEquals(new int[] {0x8405}, Assembler.assemble("MLI A, 0").getBinary());
        assertArrayEquals(new int[] {0x8406}, Assembler.assemble("DIV A, 0").getBinary());
        assertArrayEquals(new int[] {0x8407}, Assembler.assemble("DVI A, 0").getBinary());
        assertArrayEquals(new int[] {0x8408}, Assembler.assemble("MOD A, 0").getBinary());
        assertArrayEquals(new int[] {0x8409}, Assembler.assemble("MDI A, 0").getBinary());
        assertArrayEquals(new int[] {0x840a}, Assembler.assemble("AND A, 0").getBinary());
        assertArrayEquals(new int[] {0x840b}, Assembler.assemble("BOR A, 0").getBinary());
        assertArrayEquals(new int[] {0x840c}, Assembler.assemble("XOR A, 0").getBinary());
        assertArrayEquals(new int[] {0x840d}, Assembler.assemble("SHR A, 0").getBinary());
        assertArrayEquals(new int[] {0x840e}, Assembler.assemble("ASR A, 0").getBinary());
        assertArrayEquals(new int[] {0x840f}, Assembler.assemble("SHL A, 0").getBinary());
        assertArrayEquals(new int[] {0x8410}, Assembler.assemble("IFB A, 0").getBinary());
        assertArrayEquals(new int[] {0x8411}, Assembler.assemble("IFC A, 0").getBinary());
        assertArrayEquals(new int[] {0x8412}, Assembler.assemble("IFE A, 0").getBinary());
        assertArrayEquals(new int[] {0x8413}, Assembler.assemble("IFN A, 0").getBinary());
        assertArrayEquals(new int[] {0x8414}, Assembler.assemble("IFG A, 0").getBinary());
        assertArrayEquals(new int[] {0x8415}, Assembler.assemble("IFA A, 0").getBinary());
        assertArrayEquals(new int[] {0x8416}, Assembler.assemble("IFL A, 0").getBinary());
        assertArrayEquals(new int[] {0x8417}, Assembler.assemble("IFU A, 0").getBinary());
        assertArrayEquals(new int[] {0x841a}, Assembler.assemble("ADX A, 0").getBinary());
        assertArrayEquals(new int[] {0x841b}, Assembler.assemble("SBX A, 0").getBinary());
        assertArrayEquals(new int[] {0x841c}, Assembler.assemble("ROL A, 0").getBinary());
        assertArrayEquals(new int[] {0x841d}, Assembler.assemble("ROR A, 0").getBinary());
        assertArrayEquals(new int[] {0x841e}, Assembler.assemble("STI A, 0").getBinary());
        assertArrayEquals(new int[] {0x841f}, Assembler.assemble("STD A, 0").getBinary());
    }
    
    @Test
    public void allUnaryInstructionsSupportedTest() {
        assertArrayEquals(new int[] {0x0020}, Assembler.assemble("JSR A").getBinary());
        assertArrayEquals(new int[] {0x0040}, Assembler.assemble("BSR A").getBinary());
        assertArrayEquals(new int[] {0x0100}, Assembler.assemble("INT A").getBinary());
        assertArrayEquals(new int[] {0x0120}, Assembler.assemble("IAG A").getBinary());
        assertArrayEquals(new int[] {0x0140}, Assembler.assemble("IAS A").getBinary());
        assertArrayEquals(new int[] {0x0160}, Assembler.assemble("RFI A").getBinary());
        assertArrayEquals(new int[] {0x0180}, Assembler.assemble("IAQ A").getBinary());
        assertArrayEquals(new int[] {0x0200}, Assembler.assemble("HWN A").getBinary());
        assertArrayEquals(new int[] {0x0220}, Assembler.assemble("HWQ A").getBinary());
        assertArrayEquals(new int[] {0x0240}, Assembler.assemble("HWI A").getBinary());
        assertArrayEquals(new int[] {0x0280}, Assembler.assemble("SXB A").getBinary());
        assertArrayEquals(new int[] {0x02a0}, Assembler.assemble("SWP A").getBinary());
    }
    
    @Test
    public void allRegistersSupportedDirectTest() {
        assertArrayEquals(new int[] {0x8401}, Assembler.assemble("SET A, 0").getBinary());
        assertArrayEquals(new int[] {0x8421}, Assembler.assemble("SET B, 0").getBinary());
        assertArrayEquals(new int[] {0x8441}, Assembler.assemble("SET C, 0").getBinary());
        assertArrayEquals(new int[] {0x8461}, Assembler.assemble("SET X, 0").getBinary());
        assertArrayEquals(new int[] {0x8481}, Assembler.assemble("SET Y, 0").getBinary());
        assertArrayEquals(new int[] {0x84a1}, Assembler.assemble("SET Z, 0").getBinary());
        assertArrayEquals(new int[] {0x84c1}, Assembler.assemble("SET I, 0").getBinary());
        assertArrayEquals(new int[] {0x84e1}, Assembler.assemble("SET J, 0").getBinary());
    }
    
    @Test
    public void allRegistersSupportedIndirectTest() {
        assertArrayEquals(new int[] {0x8501}, Assembler.assemble("SET [A], 0").getBinary());
        assertArrayEquals(new int[] {0x8521}, Assembler.assemble("SET [B], 0").getBinary());
        assertArrayEquals(new int[] {0x8541}, Assembler.assemble("SET [C], 0").getBinary());
        assertArrayEquals(new int[] {0x8561}, Assembler.assemble("SET [X], 0").getBinary());
        assertArrayEquals(new int[] {0x8581}, Assembler.assemble("SET [Y], 0").getBinary());
        assertArrayEquals(new int[] {0x85a1}, Assembler.assemble("SET [Z], 0").getBinary());
        assertArrayEquals(new int[] {0x85c1}, Assembler.assemble("SET [I], 0").getBinary());
        assertArrayEquals(new int[] {0x85e1}, Assembler.assemble("SET [J], 0").getBinary());
    }
    
    @Test
    public void allRegistersSupportedIndirectPlusWordTest() {
        assertArrayEquals(new int[] {0x8601, 0x1234}, Assembler.assemble("SET [A+0x1234], 0").getBinary());
        assertArrayEquals(new int[] {0x8621, 0x1234}, Assembler.assemble("SET [B+0x1234], 0").getBinary());
        assertArrayEquals(new int[] {0x8641, 0x1234}, Assembler.assemble("SET [C+0x1234], 0").getBinary());
        assertArrayEquals(new int[] {0x8661, 0x1234}, Assembler.assemble("SET [X+0x1234], 0").getBinary());
        assertArrayEquals(new int[] {0x8681, 0x1234}, Assembler.assemble("SET [Y+0x1234], 0").getBinary());
        assertArrayEquals(new int[] {0x86a1, 0x1234}, Assembler.assemble("SET [Z+0x1234], 0").getBinary());
        assertArrayEquals(new int[] {0x86c1, 0x1234}, Assembler.assemble("SET [I+0x1234], 0").getBinary());
        assertArrayEquals(new int[] {0x86e1, 0x1234}, Assembler.assemble("SET [J+0x1234], 0").getBinary());
    }
}
