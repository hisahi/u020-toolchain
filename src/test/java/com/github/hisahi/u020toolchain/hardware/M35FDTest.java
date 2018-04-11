
package com.github.hisahi.u020toolchain.hardware;

import com.github.hisahi.u020toolchain.cpu.Register;
import com.github.hisahi.u020toolchain.cpu.StandardMemory;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the implementation of the M35FD peripheral.
 * 
 * @author hisahi
 */
public class M35FDTest {
    M35FD fd;
    private static final int[] diskimg = new int[M35FD.DISK_SIZE];
    
    public M35FDTest() {
    }
    
    @Before
    public void setUp() {
        fd = new M35FD(new UCPU16(new StandardMemory()), 0);
    }
    
    public void initReadStateAndError(M35FD fd) {
        fd.cpu.writeRegister(Register.A, 0);
        fd.hwi(fd.cpu);
    }
    
    public int getState(M35FD fd) {
        initReadStateAndError(fd);
        return fd.cpu.readRegister(Register.B);
    }
    
    public int getError(M35FD fd) {
        initReadStateAndError(fd);
        return fd.cpu.readRegister(Register.C);
    }

    @Test
    public void stateNoMediaTest() {
        assertEquals(M35FD.STATE_NO_MEDIA, getState(fd));
    }

    @Test
    public void errorNoMediaTest() {
        fd.cpu.writeRegister(Register.A, 2);
        fd.hwi(fd.cpu);
        assertEquals(M35FD.ERROR_NO_MEDIA, getError(fd));
    }

    @Test
    public void stateHasMediaTest() {
        fd.insert(diskimg);
        assertEquals(M35FD.STATE_READY, getState(fd));
    }

    @Test
    public void stateBusyTest() {
        fd.insert(diskimg);
        fd.cpu.writeRegister(Register.A, 2);
        fd.hwi(fd.cpu);
        assertEquals(M35FD.STATE_BUSY, getState(fd));
    }
    
    @Test
    public void errorBusyTest() {
        fd.insert(diskimg);
        fd.cpu.writeRegister(Register.A, 2);
        fd.hwi(fd.cpu);
        fd.hwi(fd.cpu);
        assertEquals(M35FD.ERROR_BUSY, getError(fd));
    }

    @Test
    public void readMediaTest() throws InterruptedException {
        int[] cpumem = fd.cpu.getMemory().array();
        for (int i = 0x2000; i < 0x3000; ++i) {
            cpumem[i] = 0xDEAD;
        }
        fd.insert(diskimg);
        fd.cpu.writeRegister(Register.A, 2);
        fd.cpu.writeRegister(Register.X, 1);
        fd.cpu.writeRegister(Register.Y, 0x2000);
        fd.hwi(fd.cpu);
        while (getState(fd) == M35FD.STATE_BUSY) {
            Thread.sleep(50);
            fd.tick();
        }
        assertArrayEquals(Arrays.copyOfRange(diskimg, 512, 1024), Arrays.copyOfRange(cpumem, 0x2000, 0x2000 + 512));
    }

    @Test
    public void writeMediaTest() throws InterruptedException {
        int[] cpumem = fd.cpu.getMemory().array();
        for (int i = 0x4001; i < 0x4001 + 512; ++i) {
            cpumem[i] = 0x55AA ^ ((i << 7) & 0xFFFF);
        }
        fd.insert(diskimg);
        fd.cpu.writeRegister(Register.A, 3);
        fd.cpu.writeRegister(Register.X, 2);
        fd.cpu.writeRegister(Register.Y, 0x4001);
        fd.hwi(fd.cpu);
        while (getState(fd) == M35FD.STATE_BUSY) {
            Thread.sleep(50);
            fd.tick();
        }
        int[] img = fd.getRawMedia();
        for (int j = 0; j < 512; ++j) {
            int membase = j + 0x4001;
            int imgbase = j + 1024;
            assertEquals("at position [" + j + "]", 0x55AA ^ ((membase << 7) & 0xFFFF), img[imgbase]);
        }
    }

    @Test
    public void ejectTest() {
        fd.insert(diskimg);
        getState(fd);
        fd.eject();
        assertEquals(M35FD.STATE_NO_MEDIA, getState(fd));
    }

    @Test
    public void ejectMidOperationTest() {
        fd.insert(diskimg);
        getState(fd);
        fd.cpu.writeRegister(Register.A, 2);
        fd.hwi(fd.cpu);
        fd.eject();
        fd.tick();
        assertEquals(M35FD.ERROR_EJECT, getError(fd));
    }

    @Test
    public void stateWriteProtectedTest() {
        fd.insert(diskimg);
        fd.setWriteProtected(true);
        assertEquals(M35FD.STATE_READY_WP, getState(fd));
    }

    @Test
    public void writeProtectedReadMediaTest() throws InterruptedException {
        int[] cpumem = fd.cpu.getMemory().array();
        for (int i = 0x2000; i < 0x3000; ++i) {
            cpumem[i] = 0xDEAD;
        }
        fd.insert(diskimg);
        fd.setWriteProtected(true);
        fd.cpu.writeRegister(Register.A, 2);
        fd.cpu.writeRegister(Register.X, 1);
        fd.cpu.writeRegister(Register.Y, 0x2000);
        fd.hwi(fd.cpu);
        while (getState(fd) == M35FD.STATE_BUSY) {
            Thread.sleep(50);
            fd.tick();
        }
        assertArrayEquals(Arrays.copyOfRange(diskimg, 512, 1024), Arrays.copyOfRange(cpumem, 0x2000, 0x2000 + 512));
    }

    @Test
    public void errorWriteProtectedTest() {
        fd.insert(diskimg);
        fd.setWriteProtected(true);
        fd.cpu.writeRegister(Register.A, 3);
        fd.hwi(fd.cpu);
        assertEquals(M35FD.ERROR_PROTECTED, getError(fd));
    }
    
    @Test
    public void saveRestoreStateTest() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream daos = new DataOutputStream(baos);
        fd.saveState(daos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dais = new DataInputStream(bais);
        fd.restoreState(dais);
    }
    
    static {
        for (int i = 0; i < diskimg.length; ++i) {
            diskimg[i] = (i * 37) & 0xFFFF;
        }
    }
}
