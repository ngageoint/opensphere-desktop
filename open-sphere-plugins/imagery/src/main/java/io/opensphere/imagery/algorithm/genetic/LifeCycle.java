package io.opensphere.imagery.algorithm.genetic;

import java.util.List;

/**
 * The LifeCycle is the controller for the {@link Generation}, the
 * {@link FitnessFunction}, and the {@link SequenceGenerator}.
 */
public abstract class LifeCycle
{
    /** The Fit fun. */
    private final FitnessFunction myFitFun;

    /**
     * The maximum number of generations to run for.
     */
    private final int myMaxGeneration;

    /**
     * The maximum population to grow a generation to, will cull back to this
     * amount after crossover.
     */
    private final int myMaxPop;

    /**
     * The minimum population to allow before adding fully random, new
     * candidates.
     */
    private final int myMinPop;

    /**
     * Instantiates a new life cycle.
     *
     * @param maxGeneration the max generation
     * @param maxPop the max pop
     * @param minPop the min pop
     * @param fitnessFunction the fitness function
     */
    public LifeCycle(int maxGeneration, int maxPop, int minPop, FitnessFunction fitnessFunction)
    {
        super();
        myMaxGeneration = maxGeneration;
        myMaxPop = maxPop;
        myMinPop = minPop;
        myFitFun = fitnessFunction;
    }

    /**
     * Gets the best candidates.
     *
     * @return the best candidates
     */
    public abstract List<Candidate> getBestCandidates();

    /**
     * Run.
     */
    public abstract void run();

    /**
     * Gets the fit fun.
     *
     * @return the fit fun
     */
    protected FitnessFunction getFitFun()
    {
        return myFitFun;
    }

    /**
     * Gets the max generation.
     *
     * @return the max generation
     */
    protected int getMaxGeneration()
    {
        return myMaxGeneration;
    }

    /**
     * Gets the max pop.
     *
     * @return the max pop
     */
    protected int getMaxPop()
    {
        return myMaxPop;
    }

    /**
     * Gets the min pop.
     *
     * @return the min pop
     */
    protected int getMinPop()
    {
        return myMinPop;
    }
}
