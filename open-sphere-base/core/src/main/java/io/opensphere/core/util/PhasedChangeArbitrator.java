package io.opensphere.core.util;

/** An arbitrator which will dictate whether phased commits are required. */
@FunctionalInterface
public interface PhasedChangeArbitrator
{
    /**
     * Tell whether phased commits are required.
     *
     * @return <code>true</code> when phased commits are required.
     */
    boolean isPhasedCommitRequired();
}
