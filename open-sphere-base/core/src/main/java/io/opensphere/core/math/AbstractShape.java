package io.opensphere.core.math;

/**
 * Abstract base class for shapes.
 */
public abstract class AbstractShape implements Shape
{
    /**
     * The name of this shape.
     */
    private String myName = "AbstractShape";

    @Override
    public String getName()
    {
        return myName;
    }

    /**
     * Set the name.
     *
     * @param name the name to set
     */
    public void setName(String name)
    {
        myName = name;
    }
}
