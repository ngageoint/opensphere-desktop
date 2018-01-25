package io.opensphere.imagery.transform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.opensphere.core.common.georeference.GroundControlPoint;
import io.opensphere.imagery.algorithm.genetic.Candidate;
import io.opensphere.imagery.algorithm.genetic.FitnessFunction;
import io.opensphere.imagery.algorithm.genetic.Generation;
import io.opensphere.imagery.algorithm.genetic.GenerationSimple;
import io.opensphere.imagery.algorithm.genetic.LifeCycle;
import io.opensphere.imagery.algorithm.genetic.SequenceGenerator;

/**
 * The Class LifeCycleWarp.
 */
public class LifeCycleWarp extends LifeCycle
{
    /** The Constant NINETY_EIGHT_PERCENT. */
    private static final double NINETY_EIGHT_PERCENT = 0.98;

    /** The best cand. */
    private Candidate myBestCand;

    /** The gcps. */
    private List<GroundControlPoint> myGcps;

    /** The ran. */
    private final java.util.Random myRan = new java.util.Random(System.currentTimeMillis());

    /**
     * Instantiates a new life cycle warp.
     *
     * @param maxGeneration the max generation
     * @param maxPop the max pop
     * @param minPop the min pop
     * @param fitnessFunction the fitness function
     */
    public LifeCycleWarp(int maxGeneration, int maxPop, int minPop, FitnessFunction fitnessFunction)
    {
        super(maxGeneration, maxPop, minPop, fitnessFunction);
    }

    @Override
    public List<Candidate> getBestCandidates()
    {
        List<Candidate> list = new ArrayList<>();
        list.add(myBestCand);
        return list;
    }

    /**
     * Gets the gC ps.
     *
     * @return the gC ps
     */
    public List<GroundControlPoint> getGCPs()
    {
        return myGcps;
    }

    @Override
    public void run()
    {
        SequenceGenerator seq = new SeqGenSimple();
        int seqSize = ImageryTransformNthOrder.getNumCoefficientsForOrder(1);
        seqSize = SeqGenSimple.SIZE * seqSize;

        Generation gen = new GenerationSimple();

        double best = Double.MAX_VALUE;

        for (int i = 0; i < getMaxPop(); i++) // first generation
        {
            makeFullyRandomMultiCandidate(seq, seqSize, gen);
        }

        Generation next = gen;

        for (int i = 0; i < getMaxGeneration(); i++)
        {
            Iterator<Candidate> iter = next.getCandidates().iterator();

            while (iter.hasNext())
            {
                Candidate cand = iter.next();
                double fitness = getFitFun().measureFitnessSpecific(cand);
                best = processCandidate(best, iter, cand, fitness);
            }
            int extras = getMinPop() - next.getCandidates().size();

            for (int q = 0; q < extras; q++) // re-add new random guys
            {
                makeFullyRandomMultiCandidate(seq, seqSize, next);
            }

            if (best < 1.0)
            {
                break;
            }

            next.setBest(myBestCand);
            next = next.getNextGeneration(seq);
        }
    }

    /**
     * Sets the gC ps.
     *
     * @param gcps the new gC ps
     */
    public void setGCPs(List<GroundControlPoint> gcps)
    {
        myGcps = gcps;
    }

    /**
     * Make fully random multi candidate.
     *
     * @param seq the seq
     * @param seqSize the seq size
     * @param gen the gen
     */
    private void makeFullyRandomMultiCandidate(SequenceGenerator seq, int seqSize, Generation gen)
    {
        Candidate c = new Candidate();
        c.setSequence(seq.generateSequence(seqSize));
        gen.getCandidates().add(c);
    }

    /**
     * Process candidate.
     *
     * @param pBest the best
     * @param iter the iter
     * @param cand the cand
     * @param fitness the fitness
     * @return the double
     */
    private double processCandidate(double pBest, Iterator<Candidate> iter, Candidate cand, double fitness)
    {
        double best = pBest;
        if (Double.isNaN(fitness))
        {
            if (myRan.nextDouble() < NINETY_EIGHT_PERCENT)
            {
                iter.remove();
            }
        }
        // TODO best * 2.0 could be in the fitness function
        else if (fitness > best * 2.0 && myRan.nextDouble() < 0.50)
        {
            iter.remove();
        }

        if (myBestCand == null)
        {
            myBestCand = cand;
        }

        if (fitness < best)
        {
            best = fitness;
            myBestCand = cand;
        }
        return best;
    }
}
