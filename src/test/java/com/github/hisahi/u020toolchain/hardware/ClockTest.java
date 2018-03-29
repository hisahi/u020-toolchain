
package com.github.hisahi.u020toolchain.hardware;

import com.github.hisahi.u020toolchain.cpu.StandardMemory;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClockTest {
    UCPU16 cpu;
    Clock clock;
    public ClockTest() {
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
        clock = new Clock(cpu);
    }
    
    @After
    public void tearDown() {
    }
    
    // ticks every 60/d seconds (d/60 Hz), if d = 0 turn off
    public void setClockInterval(int d) {
        cpu.writeRegister(UCPU16.REG_A, 0);
        cpu.writeRegister(UCPU16.REG_B, d);
        clock.hwi(cpu);
    }
    
    public int getCounter() {
        cpu.writeRegister(UCPU16.REG_A, 1);
        clock.hwi(cpu);
        return cpu.readRegister(UCPU16.REG_C);
    }
    
    @Test
    public void clockTest() throws InterruptedException {
        getCounter(); // reset counter
        setClockInterval(0); // turn off clock
        Thread.sleep(200);
        clock.tick();
        assertEquals("Clock was running even when it should be off", 0, getCounter());
        setClockInterval(6000); // 100 Hz
        clock.tick();
        Thread.sleep(200);
        clock.tick();
        assertEquals("Clock should run at 100 Hz", 20, getCounter());
        assertEquals("Clock counter should reset on read", 0, getCounter());
        setClockInterval(0); // turn off clock
    }

    
}
