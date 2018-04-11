
package com.github.hisahi.u020toolchain.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the UCPU-16 disassembler.
 * 
 * @author hisahi
 */
public class DisassemblerTest {
    
    public DisassemblerTest() {
    }

    private String disassembleAndGetInstruction(int[] i) {
        List<AssemblyListing> listing = Disassembler.disassemble(i, 0, i.length, null, null);
        if (listing.isEmpty()) {
            return "";
        }
        return listing.get(0).getCode().replaceAll("\\s+", " ").trim();
    }

    private String disassembleAndGetInstruction(int[] i, String symtable) {
        Map<Integer, List<String>> labels = new HashMap<>();
        int[] dataAreas = new int[65536];
        SymbolTableParser.parse(symtable, labels, dataAreas);
        List<AssemblyListing> listing = Disassembler.disassemble(i, 0, i.length, labels, dataAreas);
        if (listing.isEmpty()) {
            return "";
        }
        return listing.get(0).getCode().replaceAll("\\s+", " ").trim();
    }

    @Test
    public void disassembleEmptyTest() {
        assertEquals("", disassembleAndGetInstruction(new int[] {}));
    }
    
    @Test
    public void disassembleRegisterAddressingModeTest() {
        assertEquals("JSR A", disassembleAndGetInstruction(new int[] {0x0020}));
    }
    
    @Test
    public void disassembleRegisterIndirectAddressingModeTest() {
        assertEquals("SXB [C]", disassembleAndGetInstruction(new int[] {0x2a80}));
    }
    
    @Test
    public void disassembleRegisterIndirectPlusWordAddressingModeTest() {
        assertEquals("SET [A+0x0002], C", disassembleAndGetInstruction(new int[] {0x0a01, 0x0002}));
    }
    
    @Test
    public void disassembleStackPushAddressingModeTest() {
        assertEquals("SET PUSH, A", disassembleAndGetInstruction(new int[] {0x0301}));
    }
    
    @Test
    public void disassembleStackPopAddressingModeTest() {
        assertEquals("SET PC, POP", disassembleAndGetInstruction(new int[] {0x6381}));
    }
    
    @Test
    public void disassembleStackPeekAddressingModeTest() {
        assertEquals("ADD [SP], 5", disassembleAndGetInstruction(new int[] {0x9b22}));
    }
    
    @Test
    public void disassembleStackPickAddressingModeTest() {
        assertEquals("MUL [SP+0x0001], 3", disassembleAndGetInstruction(new int[] {0x9344, 0x0001}));
    }
    
    @Test
    public void disassembleSPAddressingModeTest() {
        assertEquals("SUB SP, 8", disassembleAndGetInstruction(new int[] {0xa763}));
    }
    
    @Test
    public void disassemblePCAddressingModeTest() {
        assertEquals("SET PC, 0x42a0", disassembleAndGetInstruction(new int[] {0x7f81, 0x42a0}));
    }
    
    @Test
    public void disassembleEXAddressingModeTest() {
        assertEquals("SET EX, 0", disassembleAndGetInstruction(new int[] {0x87a1}));
    }
    
    @Test
    public void disassembleWordIndirectAddressingModeTest() {
        assertEquals("JSR [0xa000]", disassembleAndGetInstruction(new int[] {0x7820, 0xa000}));
    }
    
    @Test
    public void disassembleWordAddressingModeTest() {
        assertEquals("XOR 0x0000, 0", disassembleAndGetInstruction(new int[] {0x87ec, 0x0000}));
    }
    
    @Test
    public void binaryBAfterATest() {
        assertEquals("ADD [0x5678], 0x1234", disassembleAndGetInstruction(new int[] {0x7fc2, 0x1234, 0x5678}));
    }
    
    @Test
    public void disassembleRegisterIndirectPlusWordAddressingModeWithLabelTest() {
        assertEquals("SET [A+BTMP], C", disassembleAndGetInstruction(new int[] {0x0a01, 0x0006}, "0006 BTMP"));
    }
    
    @Test
    public void disassembleWordIndirectAddressingModeWithLabelTest() {
        assertEquals("JSR [BTMP]", disassembleAndGetInstruction(new int[] {0x7820, 0x0006}, "0006 BTMP"));
    }
    
    @Test
    public void disassembleWordAddressingModeWithLabelTest() {
        assertEquals("SET A, BTMP", disassembleAndGetInstruction(new int[] {0x7c01, 0x0006}, "0006 BTMP"));
    }
    
    @Test
    public void disassembleSymbolTableDataTest() {
        assertEquals("DAT 0x1234", disassembleAndGetInstruction(new int[] {0x1234}, ".DATA 0000"));
    }
    
    @Test
    public void disassembleSymbolTableAsciiTest() {
        assertEquals("DAT 'X'", disassembleAndGetInstruction(new int[] {0x0058}, ".ASCII 0000"));
    }
    
    @Test
    public void disassembleSymbolTableHideTest() {
        assertEquals("", disassembleAndGetInstruction(new int[] {0x0000}, ".HIDE 0000"));
    }
    
    @Test
    public void disassembleSpecialAsciiTest() {
        assertEquals("DAT \"'\"", disassembleAndGetInstruction(new int[] {0x0027}, ".ASCII 0000"));
        assertEquals("DAT 0x0000 ; \\0", disassembleAndGetInstruction(new int[] {0x0000}, ".ASCII 0000"));
        assertEquals("DAT 0x0009 ; \\t", disassembleAndGetInstruction(new int[] {0x0009}, ".ASCII 0000"));
        assertEquals("DAT 0x000a ; \\n", disassembleAndGetInstruction(new int[] {0x000a}, ".ASCII 0000"));
        assertEquals("DAT 0x000d ; \\r", disassembleAndGetInstruction(new int[] {0x000d}, ".ASCII 0000"));
        assertEquals("DAT 0x8000", disassembleAndGetInstruction(new int[] {0x8000}, ".ASCII 0000"));
    }
    
    @Test
    public void disassembleMidInstructionLabelBeforeATest() {
        Map<Integer, List<String>> labels = new HashMap<>();
        int[] dataAreas = new int[65536];
        SymbolTableParser.parse("0001 MIDLABEL", labels, dataAreas);
        int[] i = new int[] {0x7fc1, 0x5678, 0x4000};
        List<AssemblyListing> listing = Disassembler.disassemble(i, 0, i.length, labels, dataAreas);
        assertEquals("SET [0x4000], 0x5678", listing.get(0).getCode().replaceAll("\\s+", " ").trim());
        assertEquals(0, listing.get(0).getPos());
        assertEquals(":MIDLABEL", listing.get(1).getCode().replaceAll("\\s+", " ").trim());
        assertEquals(1, listing.get(1).getPos());
    }
    
    @Test
    public void disassembleMidInstructionLabelBeforeBTest() {
        Map<Integer, List<String>> labels = new HashMap<>();
        int[] dataAreas = new int[65536];
        SymbolTableParser.parse("0002 MIDLABEL", labels, dataAreas);
        int[] i = new int[] {0x7fc1, 0x5678, 0x4000};
        List<AssemblyListing> listing = Disassembler.disassemble(i, 0, i.length, labels, dataAreas);
        assertEquals("SET [0x4000], 0x5678", listing.get(0).getCode().replaceAll("\\s+", " ").trim());
        assertEquals(0, listing.get(0).getPos());
        assertEquals(":MIDLABEL", listing.get(1).getCode().replaceAll("\\s+", " ").trim());
        assertEquals(2, listing.get(1).getPos());
    }
}
