
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Disassembles machine code back into a symbolic assembly form.
 * 
 * @author hisahi
 */
public class Disassembler {
    
    /**
     * Disassembles a single instruction.
     * 
     * @param memory The memory from which to fetch instruction data.
     * @param pos    The address of the instruction to disassemble.
     * @return       A list of instruction listings.
     */
    public static List<AssemblyListing> disassembleOneInstruction(int[] memory, int pos) {
        List<AssemblyListing> l = new ArrayList<>();
        l.addAll(disassembleInstruction(memory, pos, null, null));
        return l;
    }
    
    /**
     * Disassembles all instructions within a certain range.
     * 
     * @param memory The memory from which to fetch instruction data.
     * @param start  The start of the range of addresses of the instructions to disassemble.
     * @param end    The end of the range of addresses of the instructions to disassemble. Note that end is the first address to not be included in the range.
     * @return       A list of instruction listings.
     */
    public static List<AssemblyListing> disassembleInRange(int[] memory, int start, int end) {
        List<AssemblyListing> l = new ArrayList<>();
        while (start < end) {
            l.addAll(disassembleInstruction(memory, start, null, null));
            start += l.get(l.size() - 1).getHex().length;
        }
        return l;
    }
    
    
    /**
     * Disassembles all instructions within a certain range with the given labels and data areas.
     * 
     * @param memory      The memory from which to fetch instruction data.
     * @param start       The start of the range of addresses of the instructions to disassemble.
     * @param end         The end of the range of addresses of the instructions to disassemble. Note that end is the first address to not be included in the range.
     * @param labels      A map of labels as given by {@link SymbolTableParser#parse}.
     * @param dataAreas   The data areas as given by {@link SymbolTableParser#parse}.
     * @return            A list of instruction listings.
     */
    public static List<AssemblyListing> disassemble(int[] memory, int start, int end, Map<Integer, List<String>> labels, int[] dataAreas) {
        List<AssemblyListing> l = new ArrayList<>();
        int pos = start;
        if (labels != null) {
            for (int i = (end & ~0xFFFF) != 0 ? (end & 0xFFFF) : 0; i < start; ++i) {
                if (labels.containsKey(i)) {
                    for (String label: labels.get(i)) {
                        l.add(new AssemblyListing(i, new int[] {}, ":" + label));
                    }
                }
            }
        }
        while (pos < end) {
            List<AssemblyListing> il = disassembleInstruction(memory, pos, labels, dataAreas);
            Iterator<AssemblyListing> iter = il.iterator();
            while (iter.hasNext()) {
                AssemblyListing instr = iter.next();
                pos += instr.getHex().length;
                if (instr.getCode() == null) {
                    iter.remove();
                }
            }
            l.addAll(il);
        }
        if (labels != null) {
            for (int i = end; i < 0x10000; ++i) {
                if (labels.containsKey(i)) {
                    for (String label: labels.get(i)) {
                        l.add(new AssemblyListing(i, new int[] {}, ":" + label));
                    }
                }
            }
        }
        return l;
    }
    
    /**
     * Formats an instruction listing.
     * 
     * @param listing A single instruction as an AssemblyListing.
     * @return        The formatted listing.
     */
    public static String listingToString(AssemblyListing listing) {
        return listing.formatted();
    }
    
    /**
     * Formats instruction listings.
     * 
     * @param listing A list of instructions as AssemblyListing instances.
     * @return        The formatted listing.
     */
    public static String listingToString(List<AssemblyListing> listing) {
        StringBuilder sb = new StringBuilder(".");
        for (AssemblyListing l: listing) {
            sb.append(l.formatted());
            sb.append("\n");
        }
        return sb.toString().trim().substring(1);
    }
    
    private static List<AssemblyListing> disassembleInstruction(int[] memory, int pos, Map<Integer, List<String>> labels, int[] dataAreas) {
        ArrayList<AssemblyListing> ls = new ArrayList<>();
        int opos = pos;
        if (labels != null && labels.containsKey(opos)) {
            for (String label: labels.get(opos)) {
                ls.add(new AssemblyListing(opos, new int[] {}, ":" + label));
            }
        }
        if (dataAreas != null && dataAreas[opos] == -1) {
            ls.add(new AssemblyListing(opos, new int[] { memory[opos] }, null));
            return ls;
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
        List<String> finalLabel = null;
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
                List<String> lbls = labels.get(pos);
                finalLabel = lbls;
                opos = pos;
            }
            am = memory[(pos++) & 0xFFFF];
        }
        if (ib != null && ib.takesNextWord()) {
            if (labels != null && labels.containsKey(pos)) {
                List<String> lbls = labels.get(pos);
                if (ip < 0) {
                    ip = ls.size();
                    ls.add(new AssemblyListing(opos, copyOfRangeWithWraparound(memory, opos, pos), null));
                } else {
                    ls.add(new AssemblyListing(opos, copyOfRangeWithWraparound(memory, opos, pos), ":" + finalLabel.get(0)));
                    for (int i = 1; i < finalLabel.size(); ++i) {
                        ls.add(new AssemblyListing(pos, new int[0], ":" + finalLabel.get(i)));
                    }
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
                        ia.format(false, am, albl && labels.containsKey(am) ? last(labels.get(am)) : null));
            } else {
                boolean blbl = couldBeLabel(ib);
                finalstr = String.format("%s%-6s  %s, %s", spacing, instr.getName(), 
                        ib.format(true, bm, blbl && labels.containsKey(bm) ? last(labels.get(bm)) : null), 
                        ia.format(false, am, albl && labels.containsKey(am) ? last(labels.get(am)) : null));
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
            ls.add(new AssemblyListing(opos, copyOfRangeWithWraparound(memory, opos, pos), ":" + finalLabel.get(0)));
            for (int i = 1; i < finalLabel.size(); ++i) {
                ls.add(new AssemblyListing(pos, new int[0], ":" + finalLabel.get(i)));
            }
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

    private static String last(List<String> x) {
        return x.get(x.size() - 1);
    }
    
    private Disassembler() {}
}
