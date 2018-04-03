
package com.github.hisahi.u020toolchain.logic; 

public class AssemblerResult {
    private int[] binary;
    private String symbolTable;
    public AssemblerResult(int[] binary, String symbolTable) {
        this.binary = binary;
        this.symbolTable = symbolTable;
    }
    public int[] getBinary() {
        return this.binary;
    }
    public String getSymbolTable() {
        return this.symbolTable;
    }
}
