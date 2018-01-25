package io.opensphere.core.units.length;

import io.opensphere.core.units.AutoscaleUnit;

/**
 * A length that scales automatically depending on the order of the magnitude.
 */
@AutoscaleUnit
public abstract class AutoscaleLength extends Length
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** The delegate length. */
    private final Length myDelegate;

    /**
     * Constructor.
     *
     * @param magnitude The magnitude of the length.
     * @param delegate The delegate length.
     */
    protected AutoscaleLength(double magnitude, Length delegate)
    {
        super(magnitude);
        myDelegate = delegate;
    }

    @Override
    public Class<? extends Length> getDisplayClass()
    {
        return myDelegate.getClass();
    }

    @Override
    public double getDisplayMagnitude()
    {
        return myDelegate.getDisplayMagnitude();
    }

    @Override
    public Double getDisplayMagnitudeObj()
    {
        return myDelegate.getDisplayMagnitudeObj();
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return myDelegate.getLongLabel(plural);
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return myDelegate.getShortLabel(plural);
    }

    @Override
    public boolean isAutoscale()
    {
        return true;
    }
}
