
package com.github.hisahi.u020_toolchain.logic; 

import java.util.concurrent.atomic.AtomicBoolean;

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
                    long now = System.nanoTime();
                    leftover += now - lastTick;
                    lastTick = now;
                    long cycles = leftover / interval;
                    leftover %= interval;
                    if (cycles > 0) {
                        for (int i = 0; i < cycles; ++i) {
                            tickable.tick();
                        }
                    } else {
                        Thread.yield();
                    }
                }
            }
        }.start();
    }
}
