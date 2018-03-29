
package com.github.hisahi.u020toolchain.logic; 

public class AssemblyListing {
    public int pos;
    public int[] hex;
    public String code;
    public AssemblyListing(int pos, int[] hex, String code) {
        this.pos = pos;
        this.hex = hex;
        this.code = code;
    }
    public int getPos() {
        return this.pos;
    }
    public int[] getHex() {
        return this.hex;
    }
    public String getCode() {
        return this.code;
    }
    public String formatted() {
        StringBuilder hexb = new StringBuilder("");
        for (int i = 0; i < Math.min(5, hex.length); ++i) {
            hexb.append(String.format("%04x", (hex[i] & 0xFFFF)));
            hexb.append(" ");
        }
        String hexf;
        if (hexb.length() > 15) {
            hexf = hexb.toString().substring(0, 15) + "...";
        } else {
            hexf = hexb.toString();
        }
        return String.format("%04x    %-20s    %s", (pos & 0xFFFF), hexf, code);
    }
}
