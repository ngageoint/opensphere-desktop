package io.opensphere.stkterrain.model.mesh;

import java.util.Arrays;

import javax.annotation.concurrent.Immutable;

import io.opensphere.core.util.lang.ToStringHelper;

/** 16 bit indices. */
@SuppressWarnings("PMD.AvoidUsingShortType")
@Immutable
public class Indices16 implements Indices
{
    /** Serialization id. */
    private static final long serialVersionUID = 1L;

    /** The indices array. */
    private final short[] myIndices;

    /**
     * Constructor.
     *
     * @param indices the indices
     */
    public Indices16(short[] indices)
    {
        myIndices = indices.clone();
    }

    @Override
    public int getIndexCount()
    {
        return myIndices.length;
    }

    @Override
    public int getIndex(int i)
    {
        return myIndices[i];
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
        helper.add("Indices", Arrays.toString(myIndices));
        return helper.toString();
    }
}
