
package com.github.hisahi.u020toolchain.logic;

/**
 * Represents a tickable device. HighResolutionTimer instances require
 * an ITickable to tick. Hardware may implement this interface to
 * receive a tick signal for every CPU clock cycle.
 * 
 * @author hisahi
 */
public interface ITickable {
    /**
     * Called when a tick occurs. This can be a HighResolutionTimer or
     * UCPU16 instance ticking.
     */
    public void tick();
}
