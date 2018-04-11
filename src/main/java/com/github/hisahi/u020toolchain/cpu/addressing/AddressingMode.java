
package com.github.hisahi.u020toolchain.cpu.addressing; 

import com.github.hisahi.u020toolchain.cpu.Register;

/**
 * This is a helper class for UCPU-16 instruction addressing modes.
 * It contains the decoding logic as well as the instances of the
 * addressing modes.
 * 
 * @author hisahi
 */
public class AddressingMode {
    public static final IAddressingMode REG_A = new AddressingModeRegister(Register.A);
    public static final IAddressingMode REG_B = new AddressingModeRegister(Register.B);
    public static final IAddressingMode REG_C = new AddressingModeRegister(Register.C);
    public static final IAddressingMode REG_X = new AddressingModeRegister(Register.X);
    public static final IAddressingMode REG_Y = new AddressingModeRegister(Register.Y);
    public static final IAddressingMode REG_Z = new AddressingModeRegister(Register.Z);
    public static final IAddressingMode REG_I = new AddressingModeRegister(Register.I);
    public static final IAddressingMode REG_J = new AddressingModeRegister(Register.J);
    public static final IAddressingMode REG_A_IND = new AddressingModeRegisterIndirect(Register.A);
    public static final IAddressingMode REG_B_IND = new AddressingModeRegisterIndirect(Register.B);
    public static final IAddressingMode REG_C_IND = new AddressingModeRegisterIndirect(Register.C);
    public static final IAddressingMode REG_X_IND = new AddressingModeRegisterIndirect(Register.X);
    public static final IAddressingMode REG_Y_IND = new AddressingModeRegisterIndirect(Register.Y);
    public static final IAddressingMode REG_Z_IND = new AddressingModeRegisterIndirect(Register.Z);
    public static final IAddressingMode REG_I_IND = new AddressingModeRegisterIndirect(Register.I);
    public static final IAddressingMode REG_J_IND = new AddressingModeRegisterIndirect(Register.J);
    public static final IAddressingMode REG_A_IND_NW = new AddressingModeRegisterIndirectPlusWord(Register.A);
    public static final IAddressingMode REG_B_IND_NW = new AddressingModeRegisterIndirectPlusWord(Register.B);
    public static final IAddressingMode REG_C_IND_NW = new AddressingModeRegisterIndirectPlusWord(Register.C);
    public static final IAddressingMode REG_X_IND_NW = new AddressingModeRegisterIndirectPlusWord(Register.X);
    public static final IAddressingMode REG_Y_IND_NW = new AddressingModeRegisterIndirectPlusWord(Register.Y);
    public static final IAddressingMode REG_Z_IND_NW = new AddressingModeRegisterIndirectPlusWord(Register.Z);
    public static final IAddressingMode REG_I_IND_NW = new AddressingModeRegisterIndirectPlusWord(Register.I);
    public static final IAddressingMode REG_J_IND_NW = new AddressingModeRegisterIndirectPlusWord(Register.J);
    public static final IAddressingMode STACK = new AddressingModeStackPushPop();
    public static final IAddressingMode STACK_PEEK = new AddressingModeStackPeek();
    public static final IAddressingMode STACK_PICK = new AddressingModeStackPick();
    public static final IAddressingMode REG_SP = new AddressingModeRegisterSP();
    public static final IAddressingMode REG_PC = new AddressingModeRegisterPC();
    public static final IAddressingMode REG_EX = new AddressingModeRegisterEX();
    public static final IAddressingMode IND_NW = new AddressingModeIndirectWord();
    public static final IAddressingMode NW = new AddressingModeWord();
    public static final IAddressingMode[] LITERAL = new IAddressingMode[32];
    
    static {
        for (int i = 0; i < 32; ++i) {
            LITERAL[i] = new AddressingModeLiteral(i - 1);
        }
    }
    
    /**
     * Decodes an addressing mode from its internal representation
     * in the instruction.
     * 
     * @param a       The internal number of the instruction that represents
     *                the addressing mode of a parameter.
     * @return        The instance of the matching addressing mode, 
     *                or null if no addressing mode matches the given number.
     */
    public static IAddressingMode decode(int a) {
        switch (a) {
            case 0x00: return REG_A;
            case 0x01: return REG_B;
            case 0x02: return REG_C;
            case 0x03: return REG_X;
            case 0x04: return REG_Y;
            case 0x05: return REG_Z;
            case 0x06: return REG_I;
            case 0x07: return REG_J;
            case 0x08: return REG_A_IND;
            case 0x09: return REG_B_IND;
            case 0x0a: return REG_C_IND;
            case 0x0b: return REG_X_IND;
            case 0x0c: return REG_Y_IND;
            case 0x0d: return REG_Z_IND;
            case 0x0e: return REG_I_IND;
            case 0x0f: return REG_J_IND;
            case 0x10: return REG_A_IND_NW;
            case 0x11: return REG_B_IND_NW;
            case 0x12: return REG_C_IND_NW;
            case 0x13: return REG_X_IND_NW;
            case 0x14: return REG_Y_IND_NW;
            case 0x15: return REG_Z_IND_NW;
            case 0x16: return REG_I_IND_NW;
            case 0x17: return REG_J_IND_NW;
            case 0x18: return STACK;
            case 0x19: return STACK_PEEK;
            case 0x1a: return STACK_PICK;
            case 0x1b: return REG_SP;
            case 0x1c: return REG_PC;
            case 0x1d: return REG_EX;
            case 0x1e: return IND_NW;
            case 0x1f: return NW;
            default:
                if (a >= 32 && a < 64) {
                    return LITERAL[a - 32];
                }
                return null;
        }
    }
}
