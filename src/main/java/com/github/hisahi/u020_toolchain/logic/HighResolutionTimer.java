
package com.github.hisahi.u020_toolchain.logic; 

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HighResolutionTimer {
    private long interval;
    private ITickable tickable;
    private AtomicBoolean stopped;
    private long lastTick;
    private long leftover;
    public HighResolutionTimer(int hz, ITickable tickable) {
        this.interval = 1000000000L / hz;
        this.tickable = tickable;
        this.lastTick = this.leftover = 0L;
        this.stopped = new AtomicBoolean(true);
    }
    public void setSpeed(int hz) {
        this.interval = 1000000000L / hz;
        if (!stopped.get()) {
            this.stop(); 
            this.start();
        }
    }
    public void start() {
        this.leftover = 0L;
        this.lastTick = System.nanoTime();
        if (stopped.getAndSet(false)) {
            startThread();
        }
    }
    public void stop() {
        stopped.set(true);
    }
    private void startThread() {
        new Thread() {
            @Override
            public void run() {
                while (!stopped.get()) {
                    long n = System.nanoTime();
                    leftover += n - lastTick;
                    lastTick = n;
                    for (n = 0; n < leftover; n += interval) {
                        tickable.tick();
                    }
                    leftover -= n;
                    if (n < 1) {
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException ex) {
                            Thread.yield();
                        }
                    }
                }
            }
        }.start();
    }

    public boolean isRunning() {
        return !stopped.get();
    }
}
