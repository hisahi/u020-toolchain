
package com.github.hisahi.u020toolchain.logic; 

import com.github.hisahi.u020toolchain.ui.I18n;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assembler {
    public static AssemblerResult assemble(String code) {
        code = Preprocessor.preprocess(code);
        Set<String> labels = findLabels(code);
        Map<String, Integer> labelpos = new HashMap<>();
        Map<Integer, String> labelfill = new HashMap<>();
        for (String label: labels) {
            labelpos.put(label, 0);
        }
        int lno = 0;
        String finalLabel = "";
        int[] hex = new int[65536];
        int[] dataAreas = new int[65536];
        for (int i = 0; i < dataAreas.length; ++i) {
            dataAreas[i] = -1;
        }
        int pos = 0;
        for (String line: code.split("\n")) {
            if (pos >= 65536) {
                asmError(lno, "error.asm.toolong");
            }
            ++lno;
            if (line.isEmpty()) {
                continue;
            }
            line = removeComments(line).trim(); // ; comments
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith(":")) { // label
                Matcher m = labelMatching.matcher(line);
                if (m.find()) {
                    String lbl = m.group(1);
                    if (lbl.startsWith(".")) {
                        lbl = finalLabel + lbl;
                    } else {
                        finalLabel = lbl;
                    }
                    if (SymbolTableParser.isValidLabel(lbl)) {
                        labelpos.put(lbl, pos);
                    } else {
                        asmError(lno, "error.asm.invalidlabel");
                    }
                    // line may still continue
                    line = line.substring(1 + lbl.length()).trim();
                } else {
                    asmError(lno, "error.asm.invalidlabel");
                }
            } else if (line.startsWith(".")) { // command
                String[] tok = line.split("\\s+");
                String cmd = tok[0];
                if (cmd.equalsIgnoreCase(".org")) { // .org ADDR
                    if (tok.length != 2) {
                        asmError(lno, "error.asm.syntaxerror");
                    }
                    int addr = 0;
                    try {
                        addr = Integer.decode(tok[1]);
                        if (addr >= 0x10000 || addr < 0) {
                            throw new NumberFormatException("out of range");
                        }
                    } catch (NumberFormatException ex) {
                        asmError(lno, "error.asm.syntaxerror");
                    }
                    pos = addr & 0xFFFF;
                } else {
                    asmError(lno, "error.asm.unknowncmd", cmd);
                }
                continue;
            }
            // consider it an instruction
            pos += InstructionAssembler.assemble(lno, line, pos, hex, dataAreas, labelpos, labelfill);
        }
        for (Integer i: labelfill.keySet()) {
            hex[i] = (hex[i] + labelpos.get(labelfill.get(i))) & 0xFFFF;
        }
        return new AssemblerResult(Arrays.copyOfRange(hex, 0, pos), 
                constructSymbolTable(pos, labelpos, dataAreas));
    }
    
    static String constructSymbolTable(int length, Map<String, Integer> labels,
                                        int[] dataAreas) {
        List<String> lines = new ArrayList<>();
        int lastArea = 0;
        int lastAreaStart = -1;
        for (int i = 0; i < length; ++i) {
            if (dataAreas[i] != lastArea) {
                if (lastArea != 0) {
                    lines.add(getHeaderForDataAreaType(lastArea) + " " 
                            + rangeToString(lastAreaStart, i - 1));
                }
                lastArea = dataAreas[i];
                lastAreaStart = i;
            }
        }
        if (lastArea != 0 && length > 0) {
            lines.add(getHeaderForDataAreaType(lastArea) + " " 
                    + rangeToString(lastAreaStart, length - 1));
        }
        List<String> labellines = new ArrayList<>();
        for (String label: labels.keySet()) {
            labellines.add(String.format("%04x %s", labels.get(label), label));
        }
        Collections.sort(labellines);
        lines.addAll(labellines);
        return String.join("\n", lines);
    }
    
    private static String rangeToString(int a, int b) {
        if (a == b) {
            return String.format("%04x", a);
        } else {
            return String.format("%04x:%04x", a, b);
        }
    }
    private static String getHeaderForDataAreaType(int type) {
        if (type == 1) {
            return ".DATA";
        } else if (type == 2) {
            return ".ASCII";
        } else if (type == -1) {
            return ".HIDE";
        } else {
            return null;
        }
    }
    
    static Set<String> findLabels(String code) {
        Set<String> res = new HashSet<>();
        int lno = 0;
        String finalLabel = "";
        for (String line: code.split("\n")) {
            ++lno;
            if (line.isEmpty()) {
                continue;
            }
            line = removeComments(line).trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith(":")) {
                Matcher m = labelMatching.matcher(line);
                if (m.find()) {
                    String lbl = m.group(1);
                    if (lbl.startsWith(".")) {
                        lbl = finalLabel + lbl;
                    } else {
                        finalLabel = lbl;
                    }
                    if (SymbolTableParser.isValidLabel(lbl)) {
                        if (res.contains(lbl)) {
                            asmError(lno, "error.asm.duplicatesymbol", lbl);
                        } else {
                            res.add(lbl);
                        }
                    } else {
                        asmError(lno, "error.asm.invalidlabel");
                    }
                } else {
                    asmError(lno, "error.asm.invalidlabel");
                }
            }
        }
        return res;
    }

    private static String removeComments(String line) {
        if (line.contains(";")) {
            String[] tok = line.split(";");
            if (tok.length < 1) {
                return "";
            }
            return tok[0];
        } else {
            return line;
        }
    }
    
    static void asmError(int lineno, String fmt, String... arr) {
        throw new IllegalArgumentException(String.format("%s\n%s",
                I18n.format("error.asm.lineno", lineno),
                I18n.format(fmt, arr)));
    }
    static Pattern labelMatching = Pattern.compile(":([A-Za-z_][A-Za-z0-9_\\.]*)");
}
