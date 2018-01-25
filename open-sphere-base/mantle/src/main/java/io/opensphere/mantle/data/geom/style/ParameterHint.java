package io.opensphere.mantle.data.geom.style;

import io.opensphere.core.util.SharedObjectPool;

/**
 * The Interface ParameterHint.
 */
public class ParameterHint
{
    /** The our intern pool. */
    private static SharedObjectPool<ParameterHint> ourInternPool = new SharedObjectPool<>();

    /** The is render property change only. */
    private final boolean myIsRenderPropertyChangeOnly;

    /** The Requires meta data. */
    private final boolean myRequiresMetaData;

    /**
     * Static factory method for getting a pooled hint with the specified
     * settings.
     *
     * @param renderPropertyChangeOnly the render property change only
     * @param requiresMetaData the requires meta data
     * @return the parameter hint
     */
    public static ParameterHint hint(boolean renderPropertyChangeOnly, boolean requiresMetaData)
    {
        ParameterHint hint = new ParameterHint(renderPropertyChangeOnly, requiresMetaData);
        synchronized (ourInternPool)
        {
            hint = ourInternPool.get(hint);
        }
        return hint;
    }

    /**
     * Instantiates a new default parameter hint.
     *
     * @param other the other
     */
    public ParameterHint(ParameterHint other)
    {
        myIsRenderPropertyChangeOnly = other.isRenderPropertyChangeOnly();
        myRequiresMetaData = other.isRequiresMetaData();
    }

    /**
     * Instantiates a new default parameter hint.
     *
     * @param renderPropertyChangeOnly the render property change only
     * @param requiresMetaData the render property requires meta data.
     */
    private ParameterHint(boolean renderPropertyChangeOnly, boolean requiresMetaData)
    {
        myIsRenderPropertyChangeOnly = renderPropertyChangeOnly;
        myRequiresMetaData = requiresMetaData;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        ParameterHint other = (ParameterHint)obj;
        return myIsRenderPropertyChangeOnly == other.myIsRenderPropertyChangeOnly
                && myRequiresMetaData == other.myRequiresMetaData;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myIsRenderPropertyChangeOnly ? 1231 : 1237);
        result = prime * result + (myRequiresMetaData ? 1231 : 1237);
        return result;
    }

    /**
     * Checks if is render property change only.
     *
     * @return true, if is render property change only
     */
    public boolean isRenderPropertyChangeOnly()
    {
        return myIsRenderPropertyChangeOnly;
    }

    /**
     * Checks if is requires meta data.
     *
     * @return true, if is requires meta data
     */
    public boolean isRequiresMetaData()
    {
        return myRequiresMetaData;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append(this.getClass().getSimpleName()).append(" RenderPropertyChangeOnly[").append(myIsRenderPropertyChangeOnly)
                .append("]" + " RequiresMetaData[").append(myRequiresMetaData).append(']');
        return sb.toString();
    }
}
