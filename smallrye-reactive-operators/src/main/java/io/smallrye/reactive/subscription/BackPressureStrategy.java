package io.smallrye.reactive.subscription;

/**
 * The back pressure strategies.
 */
public enum BackPressureStrategy {

    /**
     * Buffer the events.
     * While being generally the default, be aware that this strategy may cause {@link OutOfMemoryError} as it uses
     * unbounded buffer.
     */
    BUFFER,

    /**
     * Drop the incoming result events if the downstream is not ready to receive it.
     */
    DROP,

    /**
     * Fire a failure with a {@link BackPressureFailure} when the downstream can't keep up
     */
    ERROR,

    /**
     * Ignore downstream back-pressure requests. Basically it pushes results downstream as they come.
     * <p>
     * This may cause an {@link BackPressureFailure} to be fired when queues get full downstream.
     */
    IGNORE,

    /**
     * Drop the oldest result events from the buffer so the downstream will get only the latest results from upstream.
     */
    LATEST
}
