
package com.github.hisahi.u020toolchain.logic; 

/**
 * Represents an addressing mode used by an instruction. This is a data
 * class used by the assembler that contains the ID of the addressing
 * mode (which will be embedded into the machine code representation
 * of the instruction), the value of a potential parameter and a label,
 * if the value in question refers to a symbol rather than being a literal.
 * 
 * @author hisahi
 */
public class InstructionAddressingMode {
    int code;
    boolean hasParameter;
    int parameter;
    String label;
    
    /**
     * Initializes a new InstructionAddressingMode instance without a parameter.
     * @param code  The ID of this addressing mode, used in the
     *              binary representation of a machine code instruction.
     */
    public InstructionAddressingMode(int code) {
        this(code, 0, "");
        this.hasParameter = false;
    }
    
    /**
     * Initializes a new InstructionAddressingMode instance with a parameter.
     * 
     * @param code  The ID of this addressing mode, used in the
     *              binary representation of a machine code instruction.
     * @param par   The parameter for this addressing mode.
     * @param label The label that the parameter refers to, or null.
     */
    public InstructionAddressingMode(int code, int par, String label) {
        this.code = code;
        this.hasParameter = true;
        this.parameter = par & 0xFFFF;
        this.label = label;
    }
}
