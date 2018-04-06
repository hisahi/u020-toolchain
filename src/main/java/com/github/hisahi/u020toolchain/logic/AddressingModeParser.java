
package com.github.hisahi.u020toolchain.logic; 

class AddressingModeParser {
    static InstructionAddressingMode parseA(int lineno, String string) {
        return parse(lineno, string, true);
    }
    static InstructionAddressingMode parseB(int lineno, String string) {
        return parse(lineno, string, false);
    }
    static void error(int lineno, boolean isA) {
        Assembler.asmError(lineno, "error.asm.syntaxerroraddr" + (isA ? "a" : "b"));
    }
    private static InstructionAddressingMode parse(int lineno, String string, boolean isA) {
        string = string.trim();
        switch (string) {
            case "A": return new InstructionAddressingMode(0x00);
            case "B": return new InstructionAddressingMode(0x01);
            case "C": return new InstructionAddressingMode(0x02);
            case "X": return new InstructionAddressingMode(0x03);
            case "Y": return new InstructionAddressingMode(0x04);
            case "Z": return new InstructionAddressingMode(0x05);
            case "I": return new InstructionAddressingMode(0x06);
            case "J": return new InstructionAddressingMode(0x07);
            case "PUSH": return new InstructionAddressingMode(0x18);
            case "POP": return new InstructionAddressingMode(0x18);
            case "PEEK": return new InstructionAddressingMode(0x19);
            case "SP": return new InstructionAddressingMode(0x1b);
            case "PC": return new InstructionAddressingMode(0x1c);
            case "EX": return new InstructionAddressingMode(0x1d);
        }
        if (string.startsWith("PICK")) {
            String[] pickTokens = string.split("\\s+");
            if (pickTokens.length == 2 && pickTokens[0].equals("PICK")) {
                int offset = 0;
                InstructionAddressingMode im = parseValue(pickTokens[1]);
                return new InstructionAddressingMode(0x1a, im.parameter, im.label);
            }
        }
        if (string.startsWith("[") && string.endsWith("]")) {
            return parseIndirect(lineno, string.substring(1, string.length() - 1), isA);
        }
        try {
            InstructionAddressingMode im = parseValue(string);
            if (isA && im.parameter == 0xFFFF && im.label == null) {
                return new InstructionAddressingMode(0x20);
            }
            if (isA && im.parameter <= 30 && im.label == null) {
                return new InstructionAddressingMode(im.parameter + 0x21);
            }
            return new InstructionAddressingMode(0x1f, im.parameter, im.label);
        } catch (NumberFormatException nex) {
        }
        error(lineno, isA);
        return null;
    }
    static InstructionAddressingMode parseValue(String string) {
        NumberFormatException fallback = null;
        try {
            return new InstructionAddressingMode(0x1f, decodeWithBinary(string), null);
        } catch (NumberFormatException nex) {
            fallback = nex;
        }
        if (SymbolTableParser.isValidLabel(string)) {
            return new InstructionAddressingMode(0x1f, 0, string);
        }
        if (string.contains("+")) {
            String[] addtok = string.split("\\+");
            String lbl = null;
            int offset = 0;
            for (String tok: addtok) {
                InstructionAddressingMode i0 = parseValue(tok);
                if (i0.label != null) {
                    if (lbl != null) {
                        throw fallback;
                    } else {
                        lbl = i0.label;
                    }
                } else {
                    offset += i0.parameter;
                }
            }
            return new InstructionAddressingMode(0x1f, offset & 0xFFFF, lbl);
        }
        if (string.contains("-")) {
            String[] subtok = string.split("-");
            String lbl = null;
            int offset = 0;
            boolean first = true;
            for (String tok: subtok) {
                InstructionAddressingMode i0 = parseValue(tok);
                if (i0.label != null) {
                    if (lbl != null || !first) {
                        throw fallback;
                    } else {
                        lbl = i0.label;
                    }
                } else {
                    if (!first) {
                        offset += (i0.parameter ^ 0xFFFF) + 1;
                    } else {
                        offset += i0.parameter;
                    }
                }
                first = false;
            }
            return new InstructionAddressingMode(0x1f, offset & 0xFFFF, lbl);
        }
        throw fallback;
    }
    private static InstructionAddressingMode parseIndirect(int lineno, String string, boolean isA) {
        switch (string) {
            case "A": return new InstructionAddressingMode(0x08);
            case "B": return new InstructionAddressingMode(0x09);
            case "C": return new InstructionAddressingMode(0x0a);
            case "X": return new InstructionAddressingMode(0x0b);
            case "Y": return new InstructionAddressingMode(0x0c);
            case "Z": return new InstructionAddressingMode(0x0d);
            case "I": return new InstructionAddressingMode(0x0e);
            case "J": return new InstructionAddressingMode(0x0f);
            case "SP": return new InstructionAddressingMode(0x19);
        }
        try {
            InstructionAddressingMode im = parseValue(string);
            return new InstructionAddressingMode(0x1e, im.parameter, im.label);
        } catch (NumberFormatException nex) {
        }
        int freg = 0x1e, treg;
        String flabel = null;
        int foffset = 0;
        string = string.replace("-", "+-");
        for (String rtok: string.split("\\+")) {
            boolean subs = rtok.startsWith("-");
            String tok = rtok;
            if (subs) {
                tok = tok.substring(1);
            }
            treg = -1;
            switch (rtok) {
                case "A": 
                    treg = 0x10; 
                    break;
                case "B": 
                    treg = 0x11; 
                    break;
                case "C": 
                    treg = 0x12; 
                    break;
                case "X": 
                    treg = 0x13; 
                    break;
                case "Y": 
                    treg = 0x14; 
                    break;
                case "Z": 
                    treg = 0x15; 
                    break;
                case "I": 
                    treg = 0x16; 
                    break;
                case "J": 
                    treg = 0x17; 
                    break;
                case "SP": 
                    treg = 0x1a; 
                    break;
            }
            if (treg >= 0) {
                if (freg != 0x1e) {
                    error(lineno, isA); // can't add two registers
                }
                freg = treg;
                continue;
            }
            try {
                InstructionAddressingMode tmp = parseValue(tok);
                if (tmp.label != null) {
                    if (flabel != null) {
                        error(lineno, isA); // can't add two labels
                    } else {
                        flabel = tmp.label;
                    }
                }
                foffset += tmp.parameter;
            } catch (NumberFormatException ex) {
                error(lineno, isA);
            }
        }
        return new InstructionAddressingMode(freg, foffset, flabel);
    }

    private static int decodeWithBinary(String string) {
        if (string.startsWith("0b")) {
            return Integer.parseInt(string.substring(2), 2);
        }
        return Integer.decode(string);
    }
}
