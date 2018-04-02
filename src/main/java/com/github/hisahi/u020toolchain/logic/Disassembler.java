
package com.github.hisahi.u020toolchain.logic; 

import com.github.hisahi.u020toolchain.cpu.addressing.AddressingMode;
import com.github.hisahi.u020toolchain.cpu.addressing.AddressingModeIndirectWord;
import com.github.hisahi.u020toolchain.cpu.addressing.AddressingModeLiteral;
import com.github.hisahi.u020toolchain.cpu.addressing.AddressingModeRegisterIndirectPlusWord;
import com.github.hisahi.u020toolchain.cpu.addressing.AddressingModeWord;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;
import com.github.hisahi.u020toolchain.cpu.instructions.IInstruction;
import com.github.hisahi.u020toolchain.cpu.instructions.Instruction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Disassembler {
    public static List<AssemblyListing> disassembleOneInstruction(int[] memory, int pos) {
        List<AssemblyListing> l = new ArrayList<>();
        l.addAll(disassembleInstruction(memory, pos, null, null));
        return l;
    }
    public static List<AssemblyListing> disassembleInRange(int[] memory, int start, int end) {
        List<AssemblyListing> l = new ArrayList<>();
        while (start < end) {
            l.addAll(disassembleInstruction(memory, start, null, null));
            start += l.get(l.size() - 1).getHex().length;
        }
        return l;
    }
    public static List<AssemblyListing> disassemble(int[] memory, int start, int end, Map<Integer, String> labels, int[] dataAreas) {
        List<AssemblyListing> l = new ArrayList<>();
        int pos = start;
        for (int i = (end & ~0xFFFF) != 0 ? (end & 0xFFFF) : 0; i < start; ++i) {
            if (labels.containsKey(i)) {
                l.add(new AssemblyListing(i, new int[] {}, ":" + labels.get(i)));
            }
        }
        while (pos < end) {
            List<AssemblyListing> il = disassembleInstruction(memory, pos, labels, dataAreas);
            l.addAll(il);
            for (AssemblyListing instr: il) {
                pos += instr.getHex().length;
            }
        }
        for (int i = end; i < 0x10000; ++i) {
            if (labels.containsKey(i)) {
                l.add(new AssemblyListing(i, new int[] {}, ":" + labels.get(i)));
            }
        }
        return l;
    }
    public static String listingToString(AssemblyListing listing) {
        return listing.formatted();
    }
    public static String listingToString(List<AssemblyListing> listing) {
        StringBuilder sb = new StringBuilder(".");
        for (AssemblyListing l: listing) {
            sb.append(l.formatted());
            sb.append("\n");
        }
        return sb.toString().trim().substring(1);
    }
    private static List<AssemblyListing> disassembleInstruction(int[] memory, int pos, Map<Integer, String> labels, int[] dataAreas) {
        ArrayList<AssemblyListing> ls = new ArrayList<>();
        int opos = pos;
        if (labels != null && labels.containsKey(opos)) {
            ls.add(new AssemblyListing(opos, new int[] {}, ":" + labels.get(opos)));
        }
        if (dataAreas != null && dataAreas[opos] == 1) { // 1 = DAT
            ls.add(new AssemblyListing(opos, new int[] { memory[opos] }, "    DAT     0x" + String.format("%04x", memory[opos])));
            return ls;
        } else if (dataAreas != null && dataAreas[opos] == 2) { // 2 = ASCII
            if (memory[opos] >= 0x20 && memory[opos] <= 0x7e && memory[opos] != 0x27) {
                ls.add(new AssemblyListing(opos, new int[] { memory[opos] }, "    DAT     '" + (char) (memory[opos]) + "'"));
            } else if (memory[opos] == 0x27) {
                ls.add(new AssemblyListing(opos, new int[] { memory[opos] }, "    DAT     \"" + (char) (memory[opos]) + "\""));
            } else if (memory[opos] == 0x0d) {
                ls.add(new AssemblyListing(opos, new int[] { memory[opos] }, "    DAT     0x000d ; \\r"));
            } else if (memory[opos] == 0x0a) {
                ls.add(new AssemblyListing(opos, new int[] { memory[opos] }, "    DAT     0x000a ; \\n"));
            } else if (memory[opos] == 0x09) {
                ls.add(new AssemblyListing(opos, new int[] { memory[opos] }, "    DAT     0x0009 ; \\t"));
            } else if (memory[opos] == 0x00) {
                ls.add(new AssemblyListing(opos, new int[] { memory[opos] }, "    DAT     0x0000 ; \\0"));
            } else {
                ls.add(new AssemblyListing(opos, new int[] { memory[opos] }, "    DAT     0x" + String.format("%04x", memory[opos])));
            }
            return ls;
        }
        int ibin = memory[(pos++) & 0xFFFF];
        int a = (ibin >> 10) & 0b111111;
        int b = (ibin >> 5) & 0b11111;
        int o = (ibin) & 0b11111;
        int am = 0;
        int bm = 0;
        int ip = -1;
        String finalLabel = null;
        IAddressingMode ia = AddressingMode.decode(a);
        IAddressingMode ib = null;
        if (o != 0) {
            ib = AddressingMode.decode(b);
        }
        IInstruction instr = Instruction.decode(a, b, o);
        if (ia.takesNextWord()) {
            if (labels != null && labels.containsKey(pos)) {
                ip = ls.size();
                ls.add(new AssemblyListing(opos, copyOfRangeWithWraparound(memory, opos, pos), null));
                finalLabel = labels.get(pos);
                opos = pos;
            }
            am = memory[(pos++) & 0xFFFF];
        }
        if (ib != null && ib.takesNextWord()) {
            if (labels != null && labels.containsKey(pos)) {
                if (ip < 0) {
                    ip = ls.size();
                    ls.add(new AssemblyListing(opos, copyOfRangeWithWraparound(memory, opos, pos), null));
                } else {
                    ls.add(new AssemblyListing(opos, copyOfRangeWithWraparound(memory, opos, pos), ":" + finalLabel));
                }
                finalLabel = labels.get(pos);
                opos = pos;
            }
            bm = memory[(pos++) & 0xFFFF];
        }
        if (instr == null) {
            ls.add(new AssemblyListing(opos, copyOfRangeWithWraparound(memory, opos, pos), "??? illegal instruction!"));
            return ls;
        }
        String finalstr = null;
        String spacing = labels != null ? "    " : "";
        if (labels != null) {
            boolean albl = couldBeLabel(ia);
            if (o == 0) {
                finalstr = String.format("%s%-6s  %s", spacing, instr.getName(), 
                        ia.format(false, am, albl && labels.containsKey(am) ? labels.get(am) : null));
            } else {
                boolean blbl = couldBeLabel(ib);
                finalstr = String.format("%s%-6s  %s, %s", spacing, instr.getName(), 
                        ib.format(true, bm, blbl && labels.containsKey(bm) ? labels.get(bm) : null), 
                        ia.format(false, am, albl && labels.containsKey(am) ? labels.get(am) : null));
            }
        } 
        if (finalstr == null) {
            if (o == 0) {
                finalstr = String.format("%s%-6s  %s", spacing, instr.getName(), ia.format(false, am, null));
            } else {
                finalstr = String.format("%s%-6s  %s, %s", spacing, instr.getName(), ib.format(true, bm, null), ia.format(false, am, null));
            }
        }
        if (ip >= 0 && finalLabel != null) {
            AssemblyListing templ = ls.get(ip);
            ls.remove(ip);
            ls.add(ip, new AssemblyListing(templ.getPos(), templ.getHex(), finalstr));
            ls.add(new AssemblyListing(opos, copyOfRangeWithWraparound(memory, opos, pos), ":" + finalLabel));
        } else {
            ls.add(new AssemblyListing(opos, copyOfRangeWithWraparound(memory, opos, pos), finalstr));
        }
        return ls;
    }
    private static int[] copyOfRangeWithWraparound(int[] memory, int a, int b) {
        int l = (b - a);
        int[] res = new int[l];
        for (int i = 0; i < l; ++i) {
            res[i] = memory[(a + i) & 0xFFFF];
        }
        return res;
    }

    private static boolean couldBeLabel(IAddressingMode ia) {
        return ia instanceof AddressingModeWord
                    || ia instanceof AddressingModeIndirectWord
                    || ia instanceof AddressingModeRegisterIndirectPlusWord
                    || ia instanceof AddressingModeLiteral;
    }
    
    private Disassembler() {}
}
