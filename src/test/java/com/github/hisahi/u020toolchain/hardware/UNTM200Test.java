
package com.github.hisahi.u020toolchain.hardware;

import com.github.hisahi.u020toolchain.cpu.Register;
import com.github.hisahi.u020toolchain.cpu.StandardMemory;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the implementation of the UNTM200 peripheral.
 * 
 * @author hisahi
 */
public class UNTM200Test {
    UCPU16 cpu;
    UNTM200 timer;
    public UNTM200Test() {
    }
    
    @Before
    public void setUp() {
        cpu = new UCPU16(new StandardMemory()); 
        timer = new UNTM200(cpu);
    }
    
    public void setInterval(int cycles) {
        cpu.writeRegister(Register.A, 0);
        cpu.writeRegister(Register.B, cycles & 0xFFFF);
        cpu.writeRegister(Register.C, (cycles >> 16) & 0xFFFF);
        timer.hwi(cpu);
    }
    
    public int getInterval() {
        cpu.writeRegister(Register.A, 3);
        timer.hwi(cpu);
        return (cpu.readRegister(Register.C) << 16) | cpu.readRegister(Register.B);
    }
    
    public int getCounter() {
        cpu.writeRegister(Register.A, 1);
        timer.hwi(cpu);
        return cpu.readRegister(Register.C);
    }

    @Test
    public void timerDoNotTickWhenOffTest() {
        setInterval(0); // disable timer
        for (int i = 0; i < 10000; ++i) {
            timer.tick();
        }
        assertEquals("timer should not tick when turned off", 0, getCounter());
    }

    @Test
    public void timerLowIntervalGetTest() {
        setInterval(1);
        assertEquals("interval is not returned correctly", 1, getInterval());
    }

    @Test
    public void timerLowIntervalTickTest() {
        setInterval(1);
        for (int i = 0; i < 1000; ++i) {
            timer.tick();
        }
        assertEquals("timer does not tick correctly", 1000, getCounter());
    }

    @Test
    public void timerCounterResetAfterReadTest() {
        setInterval(1);
        for (int i = 0; i < 1000; ++i) {
            timer.tick();
        }
        getCounter();
        assertEquals("timer counter should reset after a query", 0, getCounter());
    }

    @Test
    public void timerCounterResetAfterIntervalChangeTest() {
        setInterval(1);
        for (int i = 0; i < 1000; ++i) {
            timer.tick();
        }
        setInterval(65538);
        assertEquals("timer counter should reset after a change of interval", 0, getCounter());
    }

    @Test
    public void timerHighIntervalGetTest() {
        setInterval(65538);
        assertEquals("interval is not returned correctly", 65538, getInterval());
    }

    @Test
    public void timerHighIntervalTickTest() {
        setInterval(65538);
        for (int i = 0; i < 100000; ++i) {
            timer.tick();
        }
        assertEquals("timer does not tick correctly", 1, getCounter());
    }
}
