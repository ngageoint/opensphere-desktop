package io.opensphere.imagery.transform;

import java.util.LinkedList;
import java.util.List;

import io.opensphere.imagery.algorithm.genetic.SequenceGenerator;
import io.opensphere.imagery.algorithm.genetic.SequenceString;

/**
 * The Class SeqGenSimple.
 */
public class SeqGenSimple implements SequenceGenerator
{
//    /** The Constant ONE_HUNDRED_TWENTY. */
//    private static final double ONE_HUNDRED_TWENTY = 120.0;
//
//    /** The Constant ONE_BILLIONTH. */
//    private static final double ONE_BILLIONTH = 0.000000001;
//
//    /** The Constant ONE_HUNDRED_MILLIONTH. */
//    private static final double ONE_HUNDRED_MILLIONTH = 0.00000001;
//
//    /** The Constant ONE_TEN_MILLIONTH. */
//    private static final double ONE_TEN_MILLIONTH = 0.0000001;
//
//    /** The Constant ONE_MILLIONTH. */
//    private static final double ONE_MILLIONTH = 0.000001;
//
//    /** The Constant ONE_HUNDRED_THOUSANDANTH. */
//    private static final double ONE_HUNDRED_THOUSANDANTH = 0.00001;
//
//    /** The Constant ONE_TEN_THOUSANDANTH. */
//    private static final double ONE_TEN_THOUSANDANTH = 0.0001;
//
//    /** The Constant ONE_THOUSANDANTH. */
//    private static final double ONE_THOUSANDANTH = 0.001;
//
//    /** The Constant ONE_HUNDREDTH. */
//    private static final double ONE_HUNDREDTH = 0.01;
//
//    /** The Constant ONE_TENTH. */
//    private static final double ONE_TENTH = 0.1;
//
//    /** The Constant NINETY_PERCENT. */
//    private static final double NINETY_PERCENT = 0.90;

    /** The SIZE. */
    public static final int SIZE = 18;

//    /** The Constant SEVENTY_PERCENT. */
//    private static final double SEVENTY_PERCENT = 0.70;
//
//    /** The Constant SIXTY_PERCENT. */
//    private static final double SIXTY_PERCENT = 0.60;

    /** The child count. */
    private static final int CHILD_COUNT = 5;

//    /** The Constant FORTY_PERCENT. */
//    private static final double FORTY_PERCENT = 0.40;
//
//    /** The Constant THIRTY_PERCENT. */
//    private static final double THIRTY_PERCENT = 0.30;
//
//    /** The Constant TWENTY_PERCENT. */
//    private static final double TWENTY_PERCENT = 0.20;
//
//    /** The Constant TEN_PERCENT. */
//    private static final double TEN_PERCENT = 0.10;
//
//    /** The Constant FIVE_PERCENT. */
//    private static final double FIVE_PERCENT = 0.05;

    /** The Constant EIGHTY_PERCENT. */
    private static final double EIGHTY_PERCENT = 0.80;

    /** The Constant FIFTY_PERCENT. */
    private static final double FIFTY_PERCENT = 0.50;

    /** The Constant NINE. */
    private static final double NINE = 9.0;

    /** The ran. */
    private java.util.Random myRan = new java.util.Random(System.currentTimeMillis());

    /** The time. */
    private long myTime = System.currentTimeMillis();

    @Override
    public List<SequenceString> crossOver(SequenceString one, SequenceString two)
    {
        List<SequenceString> returns = new LinkedList<>();
        for (int i = 0; i < CHILD_COUNT; i++)
        {
            SequenceString child = new SequenceString();
            StringBuilder b = new StringBuilder();

            for (int codonIter = 0; codonIter < one.getSequence().length(); codonIter++)
            {
                if (myRan.nextDouble() < FIFTY_PERCENT)
                {
                    b.append(one.getSequence().charAt(codonIter));
                }
                else
                {
                    b.append(two.getSequence().charAt(codonIter));
                }
            }

            child.setSequence(b.toString());

            if (myRan.nextDouble() < EIGHTY_PERCENT)
            {
                child = mutate(child);
            }

            returns.add(child);
        }
        return returns;
    }

    @Override
    public SequenceString generateSequence(int length)
    {
        prepareToGenerateSequence();

        SequenceString seq = new SequenceString();
        int q = 0;
        for (int i = 0; i < length; i++)
        {
            if (q >= SIZE)
            {
                q = 0;
            }

            if (q == 0)
            {
                handleQZero(seq);
            }

            if (q > 0 && q < 6 || q > 6 && q < 14)
            {
                seq.addCodon(Long.toString(Math.round(myRan.nextDouble() * NINE)));
            }
            if (q > 15)
            {
                handleQFifteen(seq);
            }
            if (q == 6)
            {
                seq.addCodon(".");
            }

            if (q == 14)
            {
                seq.addCodon("E");
            }

            if (q == 15)
            {
                handleQZero(seq);
            }
            q++;
        }
        return seq;
    }

    @Override
    public SequenceString mutate(SequenceString in)
    {
        SequenceString ss = new SequenceString();

        int pos = (int)(myRan.nextDouble() * in.getSequence().length());
        int q = pos % SIZE;
        String val = Long.toString(Math.round(myRan.nextDouble() * NINE));
        if (q > 0 && q < 6 || q > 6 && q < 14 || q > 15)
        {
            ss.setSequence(
                    in.getSequence().substring(0, pos) + val + in.getSequence().substring(pos + 1, in.getSequence().length()));
        }
        else
        {
            ss.setSequence(String.copyValueOf(in.getSequence().toCharArray()));
        }
        // double dist = ranValue();
        //
        // in.getCodons().set((int) Math.round(ran.nextDouble() *
        // (in.getCodons().size() - 1.0)), new Codon(dist));//XXX make sure
        // affects all elements
        // fully random value appears to get to 300ish error faster

        // double dist = ranValueSmall();
        // int codonToModify = (int) Math.round(ran.nextDouble() *
        // (in.getCodons().size() - 1.0));
        // Double val = (Double) in.getCodons().get(codonToModify).getCodon();
        // in.getCodons().set(codonToModify, new Codon(val + dist));
        return ss;
    }

    /**
     * Handle q fifteen.
     *
     * @param seq the seq
     */
    private void handleQFifteen(SequenceString seq)
    {
        if (myRan.nextBoolean())
        {
            seq.addCodon(Long.toString(Math.round(myRan.nextDouble() * NINE)));
        }
        else
        {
            seq.addCodon("0");
        }
    }

//    /**
//     * Ran value.
//     *
//     * @return the double
//     */
//    private double ranValue()
//    {
//        double dist = 0.0;
//        double check = myRan.nextDouble();
//        if (check < FIVE_PERCENT)
//        {
//            dist = myRan.nextDouble() * ONE_HUNDRED_TWENTY;
//        }
//        else if (check < TEN_PERCENT)
//        {
//            dist = myRan.nextDouble();
//        }
//        else if (check < TWENTY_PERCENT)
//        {
//            dist = myRan.nextDouble() * ONE_TENTH;
//        }
//        else if (check < THIRTY_PERCENT)
//        {
//            dist = myRan.nextDouble() * ONE_HUNDREDTH;
//        }
//        else if (check < FORTY_PERCENT)
//        {
//            dist = myRan.nextDouble() * ONE_THOUSANDANTH;
//        }
//        else if (check < FIFTY_PERCENT)
//        {
//            dist = myRan.nextDouble() * ONE_TEN_THOUSANDANTH;
//        }
//        else if (check < SIXTY_PERCENT)
//        {
//            dist = myRan.nextDouble() * ONE_HUNDRED_THOUSANDANTH;
//        }
//        else if (check < SEVENTY_PERCENT)
//        {
//            dist = myRan.nextDouble() * ONE_MILLIONTH;
//        }
//        else if (check < EIGHTY_PERCENT)
//        {
//            dist = myRan.nextDouble() * ONE_TEN_MILLIONTH;
//        }
//        else if (check < NINETY_PERCENT)
//        {
//            dist = myRan.nextDouble() * ONE_HUNDRED_MILLIONTH;
//        }
//        else
//        {
//            dist = myRan.nextDouble() * ONE_BILLIONTH;
//        }
//
//        if (myRan.nextBoolean())
//        {
//            dist = -dist;
//
//        }
//
//        return dist;
//    }
//
//    /**
//     * Ran value small.
//     *
//     * @return the double
//     */
//    private double ranValueSmall()
//    {
//        double distance = 0.0;
//        double checkResult = myRan.nextDouble();
//        if (checkResult < TEN_PERCENT)
//        {
//            distance = myRan.nextDouble();
//        }
//        else if (checkResult < TWENTY_PERCENT)
//        {
//            distance = myRan.nextDouble() * ONE_TENTH;
//        }
//        else if (checkResult < THIRTY_PERCENT)
//        {
//            distance = myRan.nextDouble() * ONE_HUNDREDTH;
//        }
//        else if (checkResult < FORTY_PERCENT)
//        {
//            distance = myRan.nextDouble() * ONE_THOUSANDANTH;
//        }
//        else if (checkResult < FIFTY_PERCENT)
//        {
//            distance = myRan.nextDouble() * ONE_TEN_THOUSANDANTH;
//        }
//        else if (checkResult < SIXTY_PERCENT)
//        {
//            distance = myRan.nextDouble() * ONE_HUNDRED_THOUSANDANTH;
//        }
//        else if (checkResult < SEVENTY_PERCENT)
//        {
//            distance = myRan.nextDouble() * ONE_MILLIONTH;
//        }
//        else if (checkResult < EIGHTY_PERCENT)
//        {
//            distance = myRan.nextDouble() * ONE_TEN_MILLIONTH;
//        }
//        else if (checkResult < NINETY_PERCENT)
//        {
//            distance = myRan.nextDouble() * ONE_HUNDRED_MILLIONTH;
//        }
//        else
//        {
//            distance = myRan.nextDouble() * ONE_BILLIONTH;
//        }
//
//        if (myRan.nextBoolean())
//        {
//            distance = -distance;
//        }
//
//        return distance;
//    }

    /**
     * Handle q zero.
     *
     * @param sequence the seq
     */
    private void handleQZero(SequenceString sequence)
    {
        if (myRan.nextBoolean())
        {
            sequence.addCodon("-");
        }
        else
        {
            sequence.addCodon("+");
        }
    }

    /**
     * Prepare to generate sequence.
     */
    private void prepareToGenerateSequence()
    {
        if (myTime + 2000 < System.currentTimeMillis())
        {
            myRan = new java.util.Random(System.currentTimeMillis());
            myTime = System.currentTimeMillis();
        }
    }
}
