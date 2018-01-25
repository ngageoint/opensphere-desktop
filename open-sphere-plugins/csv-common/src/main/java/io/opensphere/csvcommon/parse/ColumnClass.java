package io.opensphere.csvcommon.parse;

/** The Enum ColumnClass. */
public enum ColumnClass
{
    /** The DOUBLE. */
    DOUBLE(Double.class),

    /** The FLOAT. */
    FLOAT(Float.class),

    /** The LONG. */
    LONG(Long.class),

    /** The INTEGER. */
    INTEGER(Integer.class),

    /** The BOOLEAN. */
    BOOLEAN(Boolean.class),

    /** The STRING. */
    STRING(String.class);

    /** The Class. */
    private final Class<?> myClass;

    /**
     * Instantiates a new column class.
     *
     * @param colClass the col class
     */
    ColumnClass(Class<?> colClass)
    {
        myClass = colClass;
    }

    /**
     * Gets the representative class.
     *
     * @return the representative class
     */
    public Class<?> getRepresentativeClass()
    {
        return myClass;
    }
}
