
package com.github.hisahi.u020toolchain.hardware;

import com.github.hisahi.u020toolchain.cpu.StandardMemory;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class UNTM200Test {
    UCPU16 cpu;
    UNTM200 timer;
    public UNTM200Test() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        cpu = new UCPU16(new StandardMemory()); 
        timer = new UNTM200(cpu);
    }
    
    @After
    public void tearDown() {
    }
    
    public void setInterval(int cycles) {
        cpu.writeRegister(UCPU16.REG_A, 0);
        cpu.writeRegister(UCPU16.REG_B, cycles & 0xFFFF);
        cpu.writeRegister(UCPU16.REG_C, (cycles >> 16) & 0xFFFF);
        timer.hwi(cpu);
    }
    
    public int getInterval() {
        cpu.writeRegister(UCPU16.REG_A, 3);
        timer.hwi(cpu);
        return (cpu.readRegister(UCPU16.REG_C) << 16) | cpu.readRegister(UCPU16.REG_B);
    }
    
    public int getCounter() {
        cpu.writeRegister(UCPU16.REG_A, 1);
        timer.hwi(cpu);
        return cpu.readRegister(UCPU16.REG_C);
    }

    @Test
    public void timerTest() {
        getCounter(); // reset counter
        setInterval(0); // disable timer
        for (int i = 0; i < 10000; ++i) {
            timer.tick();
        }
        assertEquals("timer should not tick when turned off", 0, getCounter());
        setInterval(1);
        assertEquals("interval is not returned correctly", 1, getInterval());
        for (int i = 0; i < 1000; ++i) {
            timer.tick();
        }
        assertEquals("timer does not tick correctly", 1000, getCounter());
        assertEquals("timer counter should reset after a query", 0, getCounter());
        for (int i = 0; i < 100; ++i) {
            timer.tick();
        }
        setInterval(65538);
        assertEquals("timer counter should reset after a change of interval", 0, getCounter());
        assertEquals("interval is not returned correctly", 65538, getInterval());
        for (int i = 0; i < 100000; ++i) {
            timer.tick();
        }
        assertEquals("timer does not tick correctly", 1, getCounter());
    }
}
