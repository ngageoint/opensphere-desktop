package io.opensphere.mantle.data.geom.style;

/**
 * The Class VisualizationStyleParameterFlags.
 */
public class VisualizationStyleParameterFlags
{
    /** The Is data type specific. */
    private final boolean myIsDataTypeSpecific;

    /** The Is nullable. */
    private final boolean myIsNullable;

    /** The Is saved. */
    private boolean myIsSaved = true;

    /**
     * Instantiates a new visualization style parameter flags.
     *
     * @param isDataTypeSpecific the is data type specific
     * @param isNullable the is nullable
     */
    public VisualizationStyleParameterFlags(boolean isDataTypeSpecific, boolean isNullable)
    {
        myIsDataTypeSpecific = isDataTypeSpecific;
        myIsNullable = isNullable;
    }

    /**
     * Instantiates a new visualization style parameter flags.
     *
     * @param isDataTypeSpecific the is data type specific
     * @param isNullable the is nullable
     * @param isSaved the is saved
     */
    public VisualizationStyleParameterFlags(boolean isDataTypeSpecific, boolean isNullable, boolean isSaved)
    {
        myIsDataTypeSpecific = isDataTypeSpecific;
        myIsNullable = isNullable;
        myIsSaved = isSaved;
    }

    /**
     * Instantiates a new visualization style parameter flags.
     *
     * @param other the other
     */
    public VisualizationStyleParameterFlags(VisualizationStyleParameterFlags other)
    {
        myIsDataTypeSpecific = other.myIsDataTypeSpecific;
        myIsNullable = other.myIsNullable;
        myIsSaved = other.myIsSaved;
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
        VisualizationStyleParameterFlags other = (VisualizationStyleParameterFlags)obj;
        return myIsDataTypeSpecific == other.myIsDataTypeSpecific && myIsNullable == other.myIsNullable
                && myIsSaved == other.myIsSaved;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myIsDataTypeSpecific ? 1231 : 1237);
        result = prime * result + (myIsNullable ? 1231 : 1237);
        result = prime * result + (myIsSaved ? 1231 : 1237);
        return result;
    }

    /**
     * Checks if is checks if is data type specific.
     *
     * @return true, if is checks if is data type specific
     */
    public boolean isDataTypeSpecific()
    {
        return myIsDataTypeSpecific;
    }

    /**
     * Checks if is checks if is nullable.
     *
     * @return true, if is checks if is nullable
     */
    public boolean isNullable()
    {
        return myIsNullable;
    }

    /**
     * Checks if is checks if is saved.
     *
     * @return true, if is checks if is saved
     */
    public boolean isSaved()
    {
        return myIsSaved;
    }
}
