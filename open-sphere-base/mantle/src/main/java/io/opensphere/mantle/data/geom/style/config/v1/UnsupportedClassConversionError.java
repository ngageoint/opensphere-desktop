package io.opensphere.mantle.data.geom.style.config.v1;

/**
 * The Class UnsupportedClassConversionError.
 */
public class UnsupportedClassConversionError extends RuntimeException
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new unsupported class conversion error.
     *
     * @param string the string
     */
    public UnsupportedClassConversionError(String string)
    {
        super(string);
    }

    /**
     * Instantiates a new unsupported class conversion error.
     *
     * @param string the string
     * @param e the e
     */
    public UnsupportedClassConversionError(String string, ClassNotFoundException e)
    {
        super(string, e);
    }
}
