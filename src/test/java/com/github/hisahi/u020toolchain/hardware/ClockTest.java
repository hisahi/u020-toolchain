
package com.github.hisahi.u020toolchain.hardware;

import com.github.hisahi.u020toolchain.cpu.Register;
import com.github.hisahi.u020toolchain.cpu.StandardMemory;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the implementation of the Generic Clock peripheral.
 * 
 * @author hisahi
 */
public class ClockTest {
    UCPU16 cpu;
    Clock clock;
    public ClockTest() {
    }
    
    @Before
    public void setUp() {
        cpu = new UCPU16(new StandardMemory());
        clock = new Clock(cpu);
    }
    
    // ticks every 60/d seconds (d/60 Hz), if d = 0 turn off
    public void setClockInterval(int d) {
        cpu.writeRegister(Register.A, 0);
        cpu.writeRegister(Register.B, d);
        clock.hwi(cpu);
    }
    
    public int getCounter() {
        cpu.writeRegister(Register.A, 1);
        clock.hwi(cpu);
        return cpu.readRegister(Register.C);
    }
    
    @Test
    public void clockShouldNotRunIfOff() throws InterruptedException {
        setClockInterval(0); // turn off clock
        Thread.sleep(200);
        clock.tick();
        assertEquals("Clock was running even when it should be off", 0, getCounter());
    }
    
    @Test
    public void clockRunsTest() throws InterruptedException {
        setClockInterval(6000); // 100 Hz
        clock.tick();
        Thread.sleep(200);
        clock.tick();
        assertEquals("Clock should run at 100 Hz", 20, getCounter());
    }
    
    @Test
    public void clockCounterTest() throws InterruptedException {
        setClockInterval(6000); // 100 Hz
        clock.tick();
        Thread.sleep(200);
        clock.tick();
        getCounter();
        assertEquals("Clock counter should reset on read", 0, getCounter());
    }

    
}
