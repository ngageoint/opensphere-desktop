package io.opensphere.imagery.algorithm.genetic;

import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * A candidate for the genetic algorithms fitness function.
 */
public class Candidate implements Cloneable
{
    /**
     * Fitness as judged by fitness function.
     */
    private double myFitness;

    /** The Fitness measurement. */
    // left room for an object to be used for fitness measurement
    private Object myFitnessMeasurement;

    /**
     * The codon sequence for this Candidate.
     */
    private SequenceString mySequence;

    @Override
    public Candidate clone()
    {
        try
        {
            final Candidate cand = (Candidate)super.clone();
            cand.mySequence = mySequence.clone();
            cand.myFitness = myFitness;
            cand.myFitnessMeasurement = myFitnessMeasurement;
            return cand;
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    /**
     * Get the fitness of this Candidate. Your fitness function decides whether
     * high or low, positive or negative fitness is good or bad.
     *
     * @return the <code>double</code> fitness of this Candidate
     */
    public double getFitness()
    {
        Double.parseDouble("1");
        return myFitness;
    }

    /**
     * Return an object that is the measure of this Candidates fitness.
     *
     * @return the <code>Object</code> that is the fitness
     */
    public Object getFitnessMeasurement()
    {
        return myFitnessMeasurement;
    }

    /**
     * Return this Candidates {@link SequenceString SequenceString}.
     *
     * @return the {@link SequenceString SequenceString} for this Candidate
     * @see SequenceString
     */
    public SequenceString getSequence()
    {
        return mySequence;
    }

    /**
     * Set the fitness of this candidate.
     *
     * @param aFitness the <code>double</code> that is the new fitness of this
     *            Candidate.
     */
    public void setFitness(double aFitness)
    {
        myFitness = aFitness;
    }

    /**
     * Set the object which is the measure of this Candidates fitness.
     *
     * @param aFitnessMeasurement the fitness measurement.
     */
    public void setFitnessMeasurement(Object aFitnessMeasurement)
    {
        myFitnessMeasurement = aFitnessMeasurement;
    }

    /**
     * Set this Candidates {@link SequenceString SequenceString}.
     *
     * @param newSequence {@link SequenceString SequenceString} the Sequence to
     *            set to
     */
    public void setSequence(SequenceString newSequence)
    {
        mySequence = newSequence;
    }

    @Override
    public String toString()
    {
        return "Candidate [mySequence=" + mySequence + ", myFitness=" + myFitness + ", myFitnessMeasurement="
                + myFitnessMeasurement + "]";
    }
}
