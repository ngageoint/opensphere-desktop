package io.opensphere.imagery.algorithm.genetic;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This Generation performs cross over and mutation on its next generation.
 * Parents aren't currently maintained unless they are the best parent. The best
 * parent gets special treatment, it gets clones/mutations in the next
 * generation.
 */
public class GenerationSimple implements Generation
{
    /** The Best. */
    private Candidate myBest;

    /** The candidates. */
    private List<Candidate> myCandidates = new LinkedList<>();

    @Override
    public Candidate cloneMutate(Candidate in, SequenceGenerator seqGen)
    {
        Candidate out = in.clone();
        out.setSequence(seqGen.mutate(in.getSequence()));
        return out;
    }

    @Override
    public List<Candidate> getCandidates()
    {
        return myCandidates;
    }

    @Override
    public Generation getNextGeneration(SequenceGenerator seqGen)
    {
        for (int i = 0; i < 100; i++)
        {
            myCandidates.add(cloneMutate(myBest, seqGen));
        }

        List<Candidate> halfOne = myCandidates.subList(0, myCandidates.size() / 2);
        List<Candidate> halfTwo = myCandidates.subList(myCandidates.size() / 2, myCandidates.size());

        Collections.shuffle(halfOne, new java.util.Random(System.currentTimeMillis()));
        Collections.shuffle(halfTwo, new java.util.Random(System.currentTimeMillis()));

        Iterator<Candidate> iterOne = halfOne.iterator();
        Iterator<Candidate> iterTwo = halfTwo.iterator();

        Generation returnVal = new GenerationSimple();

        while (iterOne.hasNext() && iterTwo.hasNext())
        {
            Candidate parentOne = iterOne.next();
            Candidate parentTwo = iterTwo.next();
            addChildren(seqGen, returnVal, parentOne, parentTwo);

            returnVal.getCandidates().add(parentOne);
            returnVal.getCandidates().add(parentTwo);
        }

        for (int i = 0; i < 20; i++) // straight up, mutated clones
        {
            returnVal.getCandidates().add(cloneMutate(myBest, seqGen));
        }

        for (int i = 0; i < 20; i++) // cross overed clones
        {
            Candidate parentOne = cloneMutate(myBest, seqGen);
            Candidate parentTwo = cloneMutate(myBest, seqGen);
            addChildren(seqGen, returnVal, parentOne, parentTwo);
            returnVal.getCandidates().add(parentOne);
            returnVal.getCandidates().add(parentTwo);
        }
        Collections.shuffle(returnVal.getCandidates(), new java.util.Random(System.currentTimeMillis()));

        returnVal.setCandidates(returnVal.getCandidates().subList(0, 10000));

        return returnVal;
    }

    @Override
    public void setBest(Candidate bestCand)
    {
        myBest = bestCand;
    }

    @Override
    public void setCandidates(List<Candidate> someCandidates)
    {
        myCandidates = someCandidates;
    }

    /**
     * Adds the children.
     *
     * @param seqGen the seq gen
     * @param returnVal the return val
     * @param parentOne the parent one
     * @param parentTwo the parent two
     */
    private void addChildren(SequenceGenerator seqGen, Generation returnVal, Candidate parentOne, Candidate parentTwo)
    {
        List<SequenceString> seq1 = seqGen.crossOver(parentOne.getSequence(), parentTwo.getSequence());

        for (int i = 0; i < seq1.size(); i++)
        {
            Candidate cand = new Candidate();
            cand.setSequence(seq1.get(i).clone());
            returnVal.getCandidates().add(cand);
        }
    }
}
