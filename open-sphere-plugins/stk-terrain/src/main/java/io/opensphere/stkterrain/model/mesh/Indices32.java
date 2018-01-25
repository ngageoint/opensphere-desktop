package io.opensphere.stkterrain.model.mesh;

import java.util.Arrays;

import javax.annotation.concurrent.Immutable;

import io.opensphere.core.util.lang.ToStringHelper;

/** 32 bit indices. */
@Immutable
public class Indices32 implements Indices
{
    /** Serialization id. */
    private static final long serialVersionUID = 1L;

    /** The indices array. */
    private final int[] myIndices;

    /**
     * Constructor.
     *
     * @param indices the indices
     */
    public Indices32(int[] indices)
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
