
package com.github.hisahi.u020toolchain.logic; 

public class InstructionAddressingMode {
    int code;
    boolean hasParameter;
    int parameter;
    String label;
    public InstructionAddressingMode(int code) {
        this(code, 0, "");
        this.hasParameter = false;
    }
    public InstructionAddressingMode(int code, int par, String label) {
        this.code = code;
        this.hasParameter = true;
        this.parameter = par & 0xFFFF;
        this.label = label;
    }
}
