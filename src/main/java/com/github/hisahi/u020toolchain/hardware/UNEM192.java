
package com.github.hisahi.u020toolchain.hardware; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 192 KB extra memory module
 */
public class UNEM192 extends Hardware {
    private final static int MEMORY_SIZE = 65536;
    int[] mem0;
    int[] mem1;
    int[] mem2;
    int[] mem3;
    int[] banks;

    public UNEM192(UCPU16 cpu) {
        super(cpu);
        this.reset();
    }

    @Override
    public long hardwareId() {
        return 0xCA1C4B47L;
    }

    @Override
    public int hardwareVersion() {
        return 0x01c0;
    }

    @Override
    public long hardwareManufacturer() {
        return 0x2590a31c;
    }

    @Override
    public void hwi(UCPU16 cpu) {
        switch (cpu.readRegister(UCPU16.REG_A)) {
            case 0: 
            {
                int res = 0;
                for (int i = 0; i < 16; ++i) {
                    res |= (banks[i] & 1) << i;
                }
                cpu.writeRegister(UCPU16.REG_B, res);
                break;
            }
            case 1: 
            {   
                writeBack();
                int b = cpu.readRegister(UCPU16.REG_B);
                for (int i = 0; i < 16; ++i) {
                    if (((b >> i) & 1) != 0) {
                        banks[i] |= 1; 
                    } else {
                        banks[i] &= ~1;
                    }
                }
                bankUpdate();
                break;
            }
            case 2: 
            {
                int res = 0;
                for (int i = 0; i < 16; ++i) {
                    res |= ((banks[i] >> 1) & 1) << i;
                }
                cpu.writeRegister(UCPU16.REG_B, res);
                break;
            }
            case 3: 
            {
                writeBack();
                int b = cpu.readRegister(UCPU16.REG_B);
                for (int i = 0; i < 16; ++i) {
                    if (((b >> i) & 1) != 0) {
                        banks[i] |= 2;
                    } else {
                        banks[i] &= ~2;
                    }
                }
                bankUpdate();
                break;
            }
            case 4: 
            {
                int b = cpu.readRegister(UCPU16.REG_B) & 3;
                int i = cpu.readRegister(UCPU16.REG_I);
                if (b == banks[(i >> 12)]) {
                    cpu.writeRegister(UCPU16.REG_X, cpu.getMemory().read(i));
                } else {
                    switch (b) {
                        case 0:
                            cpu.writeRegister(UCPU16.REG_X, mem0[i]);
                            break;
                        case 1:
                            cpu.writeRegister(UCPU16.REG_X, mem1[i]);
                            break;
                        case 2:
                            cpu.writeRegister(UCPU16.REG_X, mem2[i]);
                            break;
                        case 3:
                            cpu.writeRegister(UCPU16.REG_X, mem3[i]);
                            break;
                    }
                }
                break;
            }
            case 5: 
            {
                int b = cpu.readRegister(UCPU16.REG_B) & 3;
                int i = cpu.readRegister(UCPU16.REG_I);
                if (b == banks[(i >> 12)]) {
                    cpu.getMemory().write(i, cpu.readRegister(UCPU16.REG_X));
                } else {
                    switch (b) {
                        case 0:
                            mem0[i] = cpu.readRegister(UCPU16.REG_X);
                            break;
                        case 1:
                            mem1[i] = cpu.readRegister(UCPU16.REG_X);
                            break;
                        case 2:
                            mem2[i] = cpu.readRegister(UCPU16.REG_X);
                            break;
                        case 3:
                            mem3[i] = cpu.readRegister(UCPU16.REG_X);
                            break;
                    }
                }
                break;
            }
        }
    }

    @Override
    public void reset() {
        mem0 = new int[MEMORY_SIZE];
        mem1 = new int[MEMORY_SIZE];
        mem2 = new int[MEMORY_SIZE];
        mem3 = new int[MEMORY_SIZE];
        banks = new int[16];
        for (int i = 0; i < 16; ++i) {
            banks[i] = 0;
        }    
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    private void writeBack() {
        for (int i = 0; i < 16; ++i) {
            switch (banks[i]) {
                case 0:
                    System.arraycopy(cpu.getMemory().array(), i << 12, mem0, i << 12, 0x1000);
                    break;
                case 1:
                    System.arraycopy(cpu.getMemory().array(), i << 12, mem1, i << 12, 0x1000);
                    break;
                case 2:
                    System.arraycopy(cpu.getMemory().array(), i << 12, mem2, i << 12, 0x1000);
                    break;
                case 3:
                    System.arraycopy(cpu.getMemory().array(), i << 12, mem3, i << 12, 0x1000);
                    break;
            }
        }
    }

    private void bankUpdate() {
        for (int i = 0; i < 16; ++i) {
            switch (banks[i]) {
                case 0:
                    System.arraycopy(mem0, i << 12, cpu.getMemory().array(), i << 12, 0x1000);
                    break;
                case 1:
                    System.arraycopy(mem1, i << 12, cpu.getMemory().array(), i << 12, 0x1000);
                    break;
                case 2:
                    System.arraycopy(mem2, i << 12, cpu.getMemory().array(), i << 12, 0x1000);
                    break;
                case 3:
                    System.arraycopy(mem3, i << 12, cpu.getMemory().array(), i << 12, 0x1000);
                    break;
            }
        }
    }

    @Override
    public void saveState(DataOutputStream stream) throws IOException {
        for (int i = 0; i < 16; ++i) {
            stream.writeInt(banks[i]);
        }
        for (int i = 0; i < mem0.length; ++i) {
            stream.writeShort((short) mem0[i]);
        }
        for (int i = 0; i < mem1.length; ++i) {
            stream.writeShort((short) mem1[i]);
        }
        for (int i = 0; i < mem2.length; ++i) {
            stream.writeShort((short) mem2[i]);
        }
        for (int i = 0; i < mem3.length; ++i) {
            stream.writeShort((short) mem3[i]);
        }
    }

    @Override
    public void restoreState(DataInputStream stream) throws IOException {
        mem0 = new int[MEMORY_SIZE];
        mem1 = new int[MEMORY_SIZE];
        mem2 = new int[MEMORY_SIZE];
        mem3 = new int[MEMORY_SIZE];
        banks = new int[16];
        for (int i = 0; i < 16; ++i) {
            banks[i] = stream.readInt();
        }
        for (int i = 0; i < mem0.length; ++i) {
            mem0[i] = (int) stream.readShort() & 0xFFFF;
        }
        for (int i = 0; i < mem1.length; ++i) {
            mem1[i] = (int) stream.readShort() & 0xFFFF;
        }
        for (int i = 0; i < mem2.length; ++i) {
            mem2[i] = (int) stream.readShort() & 0xFFFF;
        }
        for (int i = 0; i < mem3.length; ++i) {
            mem3[i] = (int) stream.readShort() & 0xFFFF;
        }
    }
    
}
