package io.opensphere.merge.model;

import java.util.LinkedList;
import java.util.List;

import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Data model used directly by the JoinGui. It contains references to the active
 * DataTypeInfo instances.
 */
public class JoinModel
{
    /** The chosen name for the new layer. */
    private String joinName;

    /** The matching method; true indicates exact matching. */
    private boolean useExact = true;

    /** Layer-specific parameters for each layer. */
    private final List<Rec> params = new LinkedList<>();

    /**
     * Get the name of the new layer.
     *
     * @return name
     */
    public String getJoinName()
    {
        return joinName;
    }

    /**
     * Setter.
     *
     * @param n name
     */
    public void setJoinName(String n)
    {
        joinName = n;
    }

    /**
     * Get the matching method flag.
     *
     * @return true if and only if exact matching is desired.
     */
    public boolean isUseExact()
    {
        return useExact;
    }

    /**
     * Setter.
     *
     * @param b useExact
     */
    public void setUseExact(boolean b)
    {
        useExact = b;
    }

    /**
     * Get the parameters list. Note: the primary is always first.
     *
     * @return List of Rec
     */
    public List<Rec> getParams()
    {
        return params;
    }

    /**
     * Add the parameters for a layer to this model.
     *
     * @param primary true if and only if this layer is the primary
     * @param t the layer in question
     * @param col the name of the join column for <i>t</i>
     */
    public void addRow(boolean primary, DataTypeInfo t, String col)
    {
        Rec r = new Rec();
        r.primary = primary;
        r.type = t;
        r.column = col;
        if (r.primary)
        {
            params.add(0, r);
        }
        else
        {
            params.add(r);
        }
    }

    /** Struct representation of layer parameters. */
    public static class Rec
    {
        /** Primary flag. */
        public boolean primary;

        /** The layer. */
        public DataTypeInfo type;

        /** The join column. */
        public String column;
    }
}