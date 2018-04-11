
package com.github.hisahi.u020toolchain.logic;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the HighResolutionTimer class used as the CPU clock
 * for UCPU-16.
 * 
 * @author hisahi
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
    public void test1MHzWithin5Percent() throws InterruptedException {
        this.timer.start();
        Thread.sleep(200);
        this.timer.stopSoft();
        long ticks = this.tickable.ticks;
        assertTrue("There are too few ticks (should be within 5%, 950000-1050000 Hz, was " + (ticks * 5) + " Hz)", ticks >= 190000);
        assertTrue("There are too many ticks (should be within 5%, 950000-1050000 Hz, was " + (ticks * 5) + " Hz)", ticks <= 210000);
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
