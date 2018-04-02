
package com.github.hisahi.u020toolchain.logic; 

import com.github.hisahi.u020toolchain.ui.I18n;
import java.util.Map;

public class SymbolTableParser {

    public static void parse(String text, Map<Integer, String> labels, int[] dataAreas) {
        int lno = 0;
        for (String lineraw: text.split("\n")) {
            ++lno;
            String line = lineraw.trim();
            if (line.startsWith(";")) {
                // comment
                continue;
            }
            String[] tok = line.split("\\s+");
            int colonExpr = -1;
            if (tok[0].equalsIgnoreCase(".code")) {
                // .code ADDR
                // .code START:END
                colonExpr = 0;
            } else if (tok[0].equalsIgnoreCase(".data")) {
                // .data ADDR
                // .data START:END
                colonExpr = 1;
            } else if (tok[0].equalsIgnoreCase(".ascii")) {
                // .ascii ADDR
                // .ascii START:END
                colonExpr = 2;
            }
            if (colonExpr >= 0) {
                if (tok.length != 2) {
                    throw new IllegalArgumentException(String.format("%s\n%s",
                            I18n.format("error.disasm.linenosym", lno),
                            I18n.format("error.disasm.syntaxerror")));
                }
                String[] ctok = tok[1].split(":");
                if (ctok.length > 2) {
                    throw new IllegalArgumentException(String.format("%s\n%s",
                            I18n.format("error.disasm.linenosym", lno),
                            I18n.format("error.disasm.syntaxerror")));
                }
                if (ctok.length == 1) {
                    int addr;
                    try {
                        addr = Integer.parseInt(ctok[0], 16) & 0xFFFF;
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException(String.format("%s\n%s",
                            I18n.format("error.disasm.linenosym", lno),
                            I18n.format("error.disasm.syntaxerror")));
                    }
                    dataAreas[addr] = colonExpr;
                } else if (ctok.length == 2) {
                    int start, end;
                    try {
                        start = Integer.parseInt(ctok[0], 16) & 0xFFFF;
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException(String.format("%s\n%s",
                            I18n.format("error.disasm.linenosym", lno),
                            I18n.format("error.disasm.syntaxerror")));
                    }
                    try {
                        end = Integer.parseInt(ctok[1], 16) & 0xFFFF;
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException(String.format("%s\n%s",
                            I18n.format("error.disasm.linenosym", lno),
                            I18n.format("error.disasm.syntaxerror")));
                    }
                    for (int i = start; i <= end; ++i) {
                        dataAreas[i] = colonExpr;
                    }
                }
            } else {
                // ADDR LABEL
                int addr;
                try {
                    addr = Integer.parseInt(tok[0], 16) & 0xFFFF;
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(String.format("%s\n%s",
                        I18n.format("error.disasm.linenosym", lno),
                        I18n.format("error.disasm.syntaxerror")));
                }
                String label = tok[1];
                if (labels.containsKey(addr)) {
                    throw new IllegalArgumentException(String.format("%s\n%s",
                        I18n.format("error.disasm.linenosym", lno),
                        I18n.format("error.disasm.duplicatelabel")));
                }
                if (!isValidLabel(label)) {
                    throw new IllegalArgumentException(String.format("%s\n%s",
                        I18n.format("error.disasm.linenosym", lno),
                        I18n.format("error.disasm.invalidlabel")));
                }
                labels.put(addr, label);
            }
        }
    }

    private static boolean isValidLabel(String label) {
        if (!label.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            return false;
        }
        for (String token: RESERVED_SYMBOLS) {
            if (token.equals(label)) {
                return false;
            }
        }
        return true;
    }
    
    static final String[] RESERVED_SYMBOLS = new String[] {"A", "B", "C", "X", "Y", "Z", "I", "J", "PC", "SP", "EX", "IA", "PUSH", "POP", "PEEK", "PICK"};

}
