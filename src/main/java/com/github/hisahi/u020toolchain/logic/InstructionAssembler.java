
package com.github.hisahi.u020toolchain.logic; 

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Assembles individual instructions into a machine code form.
 * 
 * @author hisahi
 */
public class InstructionAssembler {
    
    /**
     * Assembles a single instruction.
     * 
     * @param lno           The line number of the instruction in the symbolic assembly code.
     * @param code          The symbolic representation of the instruction to be assembled.
     * @param pos           The address of the instruction to be assembled.
     * @param data          The memory in which to assemble, used as an output.
     * @param dataAreas     An array describing the role of every memory position, used as an output.
     * @param labels        The map of existing labels or symbols, used as an output.
     * @param requestSymbol A map that can be used by the assembled instruction to request a specific label, used as an output.
     * @return              The length of the assembled instruction in words.
     */
    public static int assemble(int lno, String code, int pos, int[] data, int[] dataAreas, Map<String, Integer> labels, Map<Integer, String> requestSymbol) {
        code = code.trim();
        if (code.isEmpty()) {
            return 0;
        }
        String[] tok = code.split("\\s+", 2);
        String instr = tok[0];
        String[] par = new String[] {};
        if (tok.length > 1) {
            par = splitOutsideQuotes(tok[1], ",");
            for (int i = 0; i < par.length; ++i) {
                par[i] = par[i].trim();
            }
        }
        if (instr.equalsIgnoreCase("NOP")) {
            dataAreas[pos] = 0;
            data[pos++] = 0x0000;
            return 1;
        } else if (instr.equalsIgnoreCase("DBG")) {
            dataAreas[pos] = 0;
            data[pos++] = 0x0060;
            return 1;
        } else if (instr.equalsIgnoreCase("RFI") && par.length < 1) {
            dataAreas[pos] = 0;
            data[pos++] = 0x0160;
            return 1;
        } else if (instr.equalsIgnoreCase("DAT")) {
            int opos = pos;
            for (String p: par) {
                if (p.startsWith("'") || p.startsWith("\"")) {
                    if (p.length() < 2 || !p.endsWith(p.substring(0, 1))) {
                        Assembler.asmError(lno, "error.asm.syntaxerror");
                    }
                    for (char c: p.substring(1, p.length() - 1).toCharArray()) {
                        dataAreas[pos] = 2;
                        data[pos++] = c;
                    }
                    continue;
                }
                try {
                    dataAreas[pos] = 1;
                    InstructionAddressingMode tmp = AddressingModeParser.parseValue(p);
                    if (tmp.label != null) {
                        requestSymbol.put(pos, tmp.label);
                    }
                    data[pos++] = tmp.parameter;
                } catch (NumberFormatException ex) {
                    Assembler.asmError(lno, "error.asm.syntaxerror");
                }
            }
            return (pos - opos);
        }
        int opos = pos;
        int binaryInstr = findBinaryInstruction(instr);
        if (binaryInstr >= 0) {
            if (par.length != 2) {
                Assembler.asmError(lno, "error.asm.syntaxerrorbinary");
            }
            InstructionAddressingMode ia = AddressingModeParser.parseA(lno, par[1]);
            InstructionAddressingMode ib = AddressingModeParser.parseB(lno, par[0]);
            dataAreas[pos] = 0;
            data[pos++] = (ia.code << 10) | (ib.code << 5) | binaryInstr;
            if (ia.hasParameter) {
                if (ia.label != null) {
                    if (!SymbolTableParser.isValidLabel(ia.label)) {
                        Assembler.asmError(lno, "error.asm.syntaxerroraddra");
                    }
                    if (!labels.containsKey(ia.label)) {
                        Assembler.asmError(lno, "error.asm.unknownsymbol", ia.label);
                    }
                    requestSymbol.put(pos, ia.label);
                }
                dataAreas[pos] = 0;
                data[pos++] = ia.parameter;
            }
            if (ib.hasParameter) {
                if (ib.label != null) {
                    if (!SymbolTableParser.isValidLabel(ib.label)) {
                        Assembler.asmError(lno, "error.asm.syntaxerroraddrb");
                    }
                    if (!labels.containsKey(ib.label)) {
                        Assembler.asmError(lno, "error.asm.unknownsymbol", ib.label);
                    }
                    requestSymbol.put(pos, ib.label);
                }
                dataAreas[pos] = 0;
                data[pos++] = ib.parameter;
            }
            return pos - opos;
        }
        int unaryInstr = findUnaryInstruction(instr);
        if (unaryInstr >= 0) {
            if (par.length != 1) {
                Assembler.asmError(lno, "error.asm.syntaxerrorunary");
            }
            InstructionAddressingMode ia = AddressingModeParser.parseA(lno, par[0]);
            dataAreas[pos] = 0;
            data[pos++] = (ia.code << 10) | (unaryInstr << 5);
            if (ia.hasParameter) {
                if (ia.label != null) {
                    if (!SymbolTableParser.isValidLabel(ia.label)) {
                        Assembler.asmError(lno, "error.asm.syntaxerroraddra");
                    }
                    if (!labels.containsKey(ia.label)) {
                        Assembler.asmError(lno, "error.asm.unknownsymbol", ia.label);
                    }
                    requestSymbol.put(pos, ia.label);
                    dataAreas[pos] = 0;
                    data[pos++] = ia.parameter;
                } else {
                    dataAreas[pos] = 0;
                    data[pos++] = ia.parameter;
                }
            }
            return pos - opos;
        }
        Assembler.asmError(lno, "error.asm.syntaxerror");
        return 0;
    }
    
    private static String[] splitOutsideQuotes(String string, String sep) {
        List<String> s = new ArrayList<>();
        int begin = 0;
        char quote = 0;
        int end = string.length();
        for (int i = 0; i <= string.length() - sep.length(); ++i) {
            char c = string.charAt(i);
            if (c == '\'' || c == '"') {
                if (quote == c) {
                    quote = 0;
                } else if (quote == 0) {
                    quote = c;
                }
            }
            if (quote != 0) {
                continue;
            }
            if (string.substring(i, i + sep.length()).equals(sep)) {
                s.add(string.substring(begin, i));
                begin = i + sep.length();
                i += sep.length() - 1;
            }
        }
        if (begin < end) {
            s.add(string.substring(begin, end));
        }
        return s.toArray(new String[] {});
    }

    private static int findBinaryInstruction(String instr) {
        switch (instr.toUpperCase()) {
            case "SET": return 0x01;
            case "ADD": return 0x02;
            case "SUB": return 0x03;
            case "MUL": return 0x04;
            case "MLI": return 0x05;
            case "DIV": return 0x06;
            case "DVI": return 0x07;
            case "MOD": return 0x08;
            case "MDI": return 0x09;
            case "AND": return 0x0a;
            case "BOR": return 0x0b;
            case "XOR": return 0x0c;
            case "SHR": return 0x0d;
            case "ASR": return 0x0e;
            case "SHL": return 0x0f;
            case "IFB": return 0x10;
            case "IFC": return 0x11;
            case "IFE": return 0x12;
            case "IFN": return 0x13;
            case "IFG": return 0x14;
            case "IFA": return 0x15;
            case "IFL": return 0x16;
            case "IFU": return 0x17;
            case "ADX": return 0x1a;
            case "SBX": return 0x1b;
            case "ROL": return 0x1c;
            case "ROR": return 0x1d;
            case "STI": return 0x1e;
            case "STD": return 0x1f;
        }
        return -1;
    }

    private static int findUnaryInstruction(String instr) {
        switch (instr.toUpperCase()) {
            case "JSR": return 0x01;
            case "BSR": return 0x02;
            case "INT": return 0x08;
            case "IAG": return 0x09;
            case "IAS": return 0x0a;
            case "RFI": return 0x0b;
            case "IAQ": return 0x0c;
            case "HWN": return 0x10;
            case "HWQ": return 0x11;
            case "HWI": return 0x12;
            case "SXB": return 0x14;
            case "SWP": return 0x15;
        }
        return -1;
    }
}
