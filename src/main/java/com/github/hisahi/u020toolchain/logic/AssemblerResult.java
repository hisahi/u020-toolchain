
package com.github.hisahi.u020toolchain.logic; 

/**
 * Represents a result given by the Assembler class, consisting
 * of the generated machine code and symbol table.
 * 
 * @author hisahi
 */
public class AssemblerResult {
    private int[] binary;
    private String symbolTable;
    /**
     * Initializes a new AssemblerResult instance.
     * 
     * @param binary      The machine code in binary form.
     * @param symbolTable The generated symbol table.
     */
    public AssemblerResult(int[] binary, String symbolTable) {
        this.binary = binary;
        this.symbolTable = symbolTable;
    }
    
    /**
     * Returns the assembled machine code.
     * 
     * @return            The machine code in binary form.
     */
    public int[] getBinary() {
        return this.binary;
    }
    
    /**
     * Returns the generated symbol table.
     * 
     * @return            The generated symbol table.
     */
    public String getSymbolTable() {
        return this.symbolTable;
    }
}
