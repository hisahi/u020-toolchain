
package com.github.hisahi.u020toolchain.logic; 

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A timer that ticks on average the given amount of times every second.
 * This is used as the CPU clock to run 2 million times a second by default,
 * but it supports other speeds as well.
 * 
 * The timer is guaranteed to be accurate, but not precise. That is,
 * if set to tick 2 million times a second, the timer will tick approximately
 * 2 million times a second, but not necessarily with even intervals.
 * (The timer may not necessarily tick 2000 times a millisecond.)
 * 
 * @author hisahi
 */
public class HighResolutionTimer {
    private long interval;
    private ITickable tickable;
    private AtomicBoolean stopped;
    private long lastTick;
    private long leftover;
    private Thread currentThread;
    
    /**
     * Initializes a new HighResolutionTimer instance.
     * 
     * @param hz       How many times a second should the timer tick on average.
     * @param tickable An object implementing ITickable that will be ticked by this timer.
     */
    public HighResolutionTimer(int hz, ITickable tickable) {
        this.interval = 1000000000L / hz;
        this.tickable = tickable;
        this.lastTick = this.leftover = 0L;
        this.stopped = new AtomicBoolean(true);
        this.currentThread = null;
    }
    
    /**
     * Changes the speed of the timer.
     * 
     * @param hz How many times a second should the timer tick on average.
     */
    public void setSpeed(int hz) {
        this.interval = 1000000000L / hz;
        if (!stopped.get()) {
            this.stop(); 
            this.start();
        }
    }
    
    /**
     * Starts the timer and a thread to run it.
     */
    public void start() {
        this.leftover = 0L;
        this.lastTick = System.nanoTime();
        if (stopped.getAndSet(false)) {
            startThread();
        }
    }
    
    /**
     * Stops the timer and stops the thread.
     */
    public void stop() {
        stopped.set(true);
        if (this.currentThread != null) {
            this.currentThread.interrupt();
            this.currentThread = null;
        }
    }
    
    /**
     * Stops the timer without immediately stopping the thread.
     */
    public void stopSoft() {
        stopped.set(true);
    }
    
    private void startThread() {
        this.currentThread = new Thread() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    long n = System.nanoTime();
                    leftover += n - lastTick;
                    lastTick = n;
                    for (n = 0; n < leftover; n += interval) {
                        tickable.tick();
                        if (Thread.interrupted()) {
                            return;
                        }
                    }
                    leftover -= n;
                    if (n < 1) {
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException ex) {
                            return;
                        }
                    }
                }
            }
        };
        this.currentThread.start();
    }

    public boolean isRunning() {
        return !stopped.get();
    }
}
