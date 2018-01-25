package io.opensphere.imagery.algorithm.genetic;

import java.util.List;

/**
 * An interface so that different SequenceGenerators can be switched out by the
 * user. The SequenceGenerator creates {@link SequenceString}s via any method it
 * chooses.
 */
public interface SequenceGenerator
{
    /**
     * Cross over.
     *
     * @param one the one
     * @param two the two
     * @return the list
     */
    List<SequenceString> crossOver(SequenceString one, SequenceString two);

    /**
     * Generate sequence.
     *
     * @param length the length
     * @return {@link SequenceString} new SequenceString
     */
    SequenceString generateSequence(int length);

    /**
     * Mutate a {@link SequenceString}.
     *
     * @param in {@link SequenceString} to be mutated
     * @return {@link SequenceString} a new copy of <code>in</code> with a
     *         mutation.
     */
    SequenceString mutate(SequenceString in);
}
