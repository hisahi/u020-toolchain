
package com.github.hisahi.u020toolchain.cpu.instructions;

public class Instruction {
    public static final IInstruction SET = new InstructionSET();
    public static final IInstruction ADD = new InstructionADD();
    public static final IInstruction SUB = new InstructionSUB();
    public static final IInstruction MUL = new InstructionMUL();
    public static final IInstruction MLI = new InstructionMLI();
    public static final IInstruction DIV = new InstructionDIV();
    public static final IInstruction DVI = new InstructionDVI();
    public static final IInstruction MOD = new InstructionMOD();
    public static final IInstruction MDI = new InstructionMDI();
    public static final IInstruction AND = new InstructionAND();
    public static final IInstruction BOR = new InstructionBOR();
    public static final IInstruction XOR = new InstructionXOR();
    public static final IInstruction SHR = new InstructionSHR();
    public static final IInstruction ASR = new InstructionASR();
    public static final IInstruction SHL = new InstructionSHL();
    public static final IInstruction IFB = new InstructionIFB();
    public static final IInstruction IFC = new InstructionIFC();
    public static final IInstruction IFE = new InstructionIFE();
    public static final IInstruction IFN = new InstructionIFN();
    public static final IInstruction IFG = new InstructionIFG();
    public static final IInstruction IFA = new InstructionIFA();
    public static final IInstruction IFL = new InstructionIFL();
    public static final IInstruction IFU = new InstructionIFU();
    public static final IInstruction ADX = new InstructionADX();
    public static final IInstruction SBX = new InstructionSBX();
    public static final IInstruction ROL = new InstructionROL();
    public static final IInstruction ROR = new InstructionROR();
    public static final IInstruction STI = new InstructionSTI();
    public static final IInstruction STD = new InstructionSTD();
    public static final IInstruction NOP = new InstructionNOP();
    public static final IInstruction JSR = new InstructionJSR();
    public static final IInstruction BSR = new InstructionBSR();
    public static final IInstruction DBG = new InstructionDBG();
    public static final IInstruction INT = new InstructionINT();
    public static final IInstruction IAG = new InstructionIAG();
    public static final IInstruction IAS = new InstructionIAS();
    public static final IInstruction RFI = new InstructionRFI();
    public static final IInstruction IAQ = new InstructionIAQ();
    public static final IInstruction HWN = new InstructionHWN();
    public static final IInstruction HWQ = new InstructionHWQ();
    public static final IInstruction HWI = new InstructionHWI();
    public static final IInstruction SXB = new InstructionSXB();
    public static final IInstruction SWP = new InstructionSWP();
    
    public static IInstruction decode(int a, int b, int o) {
        switch (o) {
            case 0x01: return SET;
            case 0x02: return ADD;
            case 0x03: return SUB;
            case 0x04: return MUL;
            case 0x05: return MLI;
            case 0x06: return DIV;
            case 0x07: return DVI;
            case 0x08: return MOD;
            case 0x09: return MDI;
            case 0x0a: return AND;
            case 0x0b: return BOR;
            case 0x0c: return XOR;
            case 0x0d: return SHR;
            case 0x0e: return ASR;
            case 0x0f: return SHL;
            case 0x10: return IFB;
            case 0x11: return IFC;
            case 0x12: return IFE;
            case 0x13: return IFN;
            case 0x14: return IFG;
            case 0x15: return IFA;
            case 0x16: return IFL;
            case 0x17: return IFU;
            case 0x1a: return ADX;
            case 0x1b: return SBX;
            case 0x1c: return ROL;
            case 0x1d: return ROR;
            case 0x1e: return STI;
            case 0x1f: return STD;
            case 0:
                switch (b) {
                    case 0x00: return NOP;
                    case 0x01: return JSR;
                    case 0x02: return BSR;
                    case 0x03: return DBG;
                    case 0x08: return INT;
                    case 0x09: return IAG;
                    case 0x0a: return IAS;
                    case 0x0b: return RFI;
                    case 0x0c: return IAQ;
                    case 0x10: return HWN;
                    case 0x11: return HWQ;
                    case 0x12: return HWI;
                    case 0x14: return SXB;
                    case 0x15: return SWP;
                    default:
                        return null;
                }
            default:
                return null;
        }
    }
}
