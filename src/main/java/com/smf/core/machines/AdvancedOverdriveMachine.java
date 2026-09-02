package com.smf.core.machines;

/**
 * Implemented by multiblock block entities that can host the advanced overdrive
 * module (persistent efficiency across recipe switches).
 */
public interface AdvancedOverdriveMachine {
    /**
     * Whether the machine currently holds an advanced overdrive module that keeps
     * the efficiency accumulation across recipe switches.
     */
    boolean isAdvancedOverdrive();
}