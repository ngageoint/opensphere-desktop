package io.opensphere.imagery.algorithm.genetic;

import java.util.List;

/**
 * Generation holds the current generation and can create the next Generation.
 */
public interface Generation
{
    /**
     * Clone mutate.
     *
     * @param in the in
     * @param seqGen the seq gen
     * @return the candidate
     */
    Candidate cloneMutate(Candidate in, SequenceGenerator seqGen);

    /**
     * Gets the candidates.
     *
     * @return the candidates
     */
    List<Candidate> getCandidates();

    /**
     * Gets the next generation.
     *
     * @param seqGen the seq gen
     * @return the next generation
     */
    Generation getNextGeneration(SequenceGenerator seqGen);

    /**
     * Sets the best.
     *
     * @param bestCand the new best
     */
    void setBest(Candidate bestCand);

    /**
     * Sets the candidates.
     *
     * @param someCandidates the new candidates
     */
    void setCandidates(List<Candidate> someCandidates);
}
