package io.opensphere.imagery.algorithm.genetic;

import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * The Class SequenceString.
 */
public class SequenceString implements Cloneable
{
    /** The sequence. */
    private String mySequence = "";

    /**
     * Adds the codon.
     *
     * @param in the in
     */
    public void addCodon(String in)
    {
        mySequence = mySequence + in;
    }

    @Override
    public SequenceString clone()
    {
        try
        {
            final SequenceString ss = (SequenceString)super.clone();
            ss.mySequence = mySequence;
            return ss;
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    /**
     * Gets the sequence.
     *
     * @return the sequence
     */
    public String getSequence()
    {
        return mySequence;
    }

    /**
     * Sets the sequence.
     *
     * @param newSequence the new sequence
     */
    public void setSequence(String newSequence)
    {
        mySequence = newSequence;
    }

    @Override
    public String toString()
    {
        return "Sequence [mySequence=" + mySequence + "]";
    }
}
