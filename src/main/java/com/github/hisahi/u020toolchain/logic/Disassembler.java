
package com.github.hisahi.u020toolchain.logic; 

import com.github.hisahi.u020toolchain.cpu.addressing.AddressingMode;
import com.github.hisahi.u020toolchain.cpu.addressing.IAddressingMode;
import com.github.hisahi.u020toolchain.cpu.instructions.IInstruction;
import com.github.hisahi.u020toolchain.cpu.instructions.Instruction;
import java.util.ArrayList;
import java.util.List;

public class Disassembler {
    public static List<AssemblyListing> disassembleOneInstruction(int[] memory, int pos) {
        List<AssemblyListing> l = new ArrayList<>();
        l.add(disassembleInstruction(memory, pos));
        return l;
    }
    public static List<AssemblyListing> disassembleInRange(int[] memory, int start, int end) {
        List<AssemblyListing> l = new ArrayList<>();
        while (start < end) {
            l.add(disassembleInstruction(memory, start));
            start += l.get(l.size() - 1).getHex().length;
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
    private static AssemblyListing disassembleInstruction(int[] memory, int pos) {
        int opos = pos;
        int ibin = memory[(pos++) & 0xFFFF];
        int a = (ibin >> 10) & 0b111111;
        int b = (ibin >> 5) & 0b11111;
        int o = (ibin) & 0b11111;
        int am = 0;
        int bm = 0;
        IAddressingMode ia = AddressingMode.decode(a);
        IAddressingMode ib = null;
        if (o != 0) {
            ib = AddressingMode.decode(b);
        }
        IInstruction instr = Instruction.decode(a, b, o);
        if (ia.takesNextWord()) {
            am = memory[(pos++) & 0xFFFF];
        }
        if (ib != null) {
            if (ib.takesNextWord()) {
                bm = memory[(pos++) & 0xFFFF];
            }
        }
        if (instr == null) {
            return new AssemblyListing(opos, copyOfRangeWithWraparound(memory, opos, pos), "??? illegal instruction!");
        }
        String finalstr;
        if (o == 0) {
            finalstr = String.format("%-6s  %s", instr.getName(), ia.format(false, am));
        } else {
            finalstr = String.format("%-6s  %s, %s", instr.getName(), ib.format(true, bm), ia.format(false, am));
        }
        return new AssemblyListing(opos, copyOfRangeWithWraparound(memory, opos, pos), finalstr);
    }
    private static int[] copyOfRangeWithWraparound(int[] memory, int a, int b) {
        int l = (b - a);
        int[] res = new int[l];
        for (int i = 0; i < l; ++i) {
            res[i] = memory[(a + i) & 0xFFFF];
        }
        return res;
    }
    private Disassembler() {}
}
