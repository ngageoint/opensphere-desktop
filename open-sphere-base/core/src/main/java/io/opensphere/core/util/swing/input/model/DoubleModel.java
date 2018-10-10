package io.opensphere.core.util.swing.input.model;

import io.opensphere.core.util.ObservableValueValidatorSupport;
import io.opensphere.core.util.PredicateWithMessage;
import io.opensphere.core.util.WrappedPredicateWithMessage;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.predicate.DoubleRangePredicate;

/**
 * Double model.
 */
public class DoubleModel extends AbstractViewModel<Double>
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The minimum allowed value. */
    private final double myMin;

    /** The minimum allowed value. */
    private final double myMax;

    /**
     * Constructor.
     */
    public DoubleModel()
    {
        this(-Double.MAX_VALUE, Double.MAX_VALUE);
    }

    /**
     * Constructor.
     *
     * @param min The minimum allowed value
     * @param max The maximum allowed value
     */
    public DoubleModel(double min, double max)
    {
        myMin = min;
        myMax = max;

        PredicateWithMessage<Double> predicate = new WrappedPredicateWithMessage<>(new DoubleRangePredicate(min, max))
        {
            @Override
            public String getMessage()
            {
                if (myMin > -Double.MAX_VALUE && myMax < Double.MAX_VALUE)
                {
                    return StringUtilities.concat(getName(), " must be a number between ", String.valueOf(myMin), " and ",
                            String.valueOf(myMax));
                }
                else if (myMin > -Double.MAX_VALUE)
                {
                    return StringUtilities.concat(getName(), " must be a number >= ", String.valueOf(myMin));
                }
                else if (myMax < Double.MAX_VALUE)
                {
                    return StringUtilities.concat(getName(), " must be a number <= ", String.valueOf(myMax));
                }
                else
                {
                    return StringUtilities.concat(getName(), " must be a valid number");
                }
            }
        };
        setValidatorSupport(new ObservableValueValidatorSupport<>(this, predicate));
    }

    /**
     * Gets the max.
     *
     * @return the max
     */
    public double getMax()
    {
        return myMax;
    }

    /**
     * Gets the min.
     *
     * @return the min
     */
    public double getMin()
    {
        return myMin;
    }
}
