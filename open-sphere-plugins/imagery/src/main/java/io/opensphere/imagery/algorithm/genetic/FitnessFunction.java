package io.opensphere.imagery.algorithm.genetic;

/**
 * Interface to allow for a Genetic Algorithm implementor to implement their own
 * fitness function.
 *
 * @see Candidate
 * @see SequenceString
 * @see SequenceGenerator
 */
public interface FitnessFunction
{
    /**
     * Measure fitness.
     *
     * @param cand the candidate
     * @return the object
     */
    Object measureFitness(Candidate cand);

    /**
     * Measure the fitness of a {@link Candidate}.
     *
     * @param cand The {@link Candidate} to analyze.
     * @return <code>double</code> representing the fitness. Low or high,
     *         negative or positive being better is determined by this fitness
     *         function.
     */
    double measureFitnessSpecific(Candidate cand);
}
