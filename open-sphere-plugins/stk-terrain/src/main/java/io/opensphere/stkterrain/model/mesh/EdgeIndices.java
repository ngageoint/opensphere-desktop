package io.opensphere.stkterrain.model.mesh;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;

import io.opensphere.core.util.lang.ToStringHelper;

/** Edge Indices. */
@Immutable
public class EdgeIndices implements Serializable
{
    /** Serialization id. */
    private static final long serialVersionUID = 1L;

    /** The west indices. */
    private final Indices myWestIndices;

    /** The south indices. */
    private final Indices mySouthIndices;

    /** The east indices. */
    private final Indices myEastIndices;

    /** The north indices. */
    private final Indices myNorthIndices;

    /**
     * Constructor.
     *
     * @param west The west indices
     * @param south The south indices
     * @param east The east indices
     * @param north The north indices
     */
    public EdgeIndices(Indices west, Indices south, Indices east, Indices north)
    {
        myWestIndices = west;
        mySouthIndices = south;
        myEastIndices = east;
        myNorthIndices = north;
    }

    /**
     * Gets the westIndices.
     *
     * @return the westIndices
     */
    public Indices getWestIndices()
    {
        return myWestIndices;
    }

    /**
     * Gets the southIndices.
     *
     * @return the southIndices
     */
    public Indices getSouthIndices()
    {
        return mySouthIndices;
    }

    /**
     * Gets the eastIndices.
     *
     * @return the eastIndices
     */
    public Indices getEastIndices()
    {
        return myEastIndices;
    }

    /**
     * Gets the northIndices.
     *
     * @return the northIndices
     */
    public Indices getNorthIndices()
    {
        return myNorthIndices;
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
        helper.add("WestIndices", myWestIndices);
        helper.add("SouthIndices", mySouthIndices);
        helper.add("EastIndices", myEastIndices);
        helper.add("NorthIndices", myNorthIndices);
        return helper.toStringMultiLine(1);
    }
}
