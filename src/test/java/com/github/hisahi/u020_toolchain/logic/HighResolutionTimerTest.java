/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hisahi.u020_toolchain.logic;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hopea
 */
public class HighResolutionTimerTest {
    
    public HighResolutionTimerTest() {
    }
    private HighResolutionTimer timer;
    private DummyTickable tickable;
    
    @Before
    public void setUp() {
        this.tickable = new DummyTickable();
        this.timer = new HighResolutionTimer(1000000, tickable);
    }

    @Test
    public void test1MHzWithinPromille() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        this.timer.start();
        Thread.sleep(200 - (System.currentTimeMillis() - startTime));
        this.timer.stop();
        long ticks = this.tickable.ticks;
        assertTrue("There are too few ticks (should be within 1%, 990000-1010000 Hz, was " + (ticks * 5) + " Hz)", ticks >= 198000);
        assertTrue("There are too many ticks (should be within 1%, 990000-1010000 Hz, was " + (ticks * 5) + " Hz)", ticks <= 202000);
    }
    
    class DummyTickable implements ITickable {
        long ticks;
        public DummyTickable() {
            this.ticks = 0L;
        }
        @Override
        public void tick() {
            ++this.ticks;
        }
    }
}
