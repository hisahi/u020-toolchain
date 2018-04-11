
package com.github.hisahi.u020toolchain.logic; 

import com.github.hisahi.u020toolchain.ui.I18n;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses symbol tables for disassembling code with a symbol table provided.
 * The symbol table may contain labels as well as data and text sections.
 * 
 * @author hisahi
 */
public class SymbolTableParser {

    /**
     * Parses a symbol table and writes the labels and data areas into the given map and array respectively.
     * 
     * @param text          The symbol table as a String.
     * @param labels        The map of existing labels or symbols, used as an output.
     * @param dataAreas     An array describing the role of every memory position, used as an output.
     */
    public static void parse(String text, Map<Integer, List<String>> labels, int[] dataAreas) {
        int lno = 0;
        for (String lineraw: text.split("\n")) {
            ++lno;
            String line = lineraw.trim();
            if (line.startsWith(";")) {
                // comment
                continue;
            }
            if (line.isEmpty()) {
                continue;
            }
            String[] tok = line.split("\\s+");
            int colonExpr = Integer.MIN_VALUE;
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
            } else if (tok[0].equalsIgnoreCase(".hide")) {
                // .hide ADDR
                // .hide START:END
                colonExpr = -1;
            }
            if (colonExpr != Integer.MIN_VALUE) {
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
                if (!isValidLabel(label)) {
                    throw new IllegalArgumentException(String.format("%s\n%s",
                        I18n.format("error.disasm.linenosym", lno),
                        I18n.format("error.disasm.invalidlabel")));
                }
                if (!labels.containsKey(addr)) {
                    labels.put(addr, new ArrayList<String>());
                }
                labels.get(addr).add(label);
            }
        }
    }

    static boolean isValidLabel(String label) {
        if (!label.matches(LABEL_REGEX)) {
            // allow dots, as long as they're not in the beginning or the end
            // and there aren't two in a row
            if (label.matches(LABEL_REGEX_DOT) && !label.endsWith(".")) {
                for (int i = 1; i < label.length(); ++i) {
                    if (label.charAt(i) == '.' && label.charAt(i - 1) == '.') {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        for (String token: RESERVED_SYMBOLS) {
            if (token.equals(label)) {
                return false;
            }
        }
        return true;
    }
    
    static final String LABEL_REGEX = "[A-Za-z_][A-Za-z0-9_]*";
    static final String LABEL_REGEX_DOT = "[A-Za-z_][A-Za-z0-9_\\.]*";
    static final String[] RESERVED_SYMBOLS = new String[] {"A", "B", "C", "X", "Y", "Z", "I", "J", "PC", "SP", "EX", "IA", "PUSH", "POP", "PEEK", "PICK"};

}
