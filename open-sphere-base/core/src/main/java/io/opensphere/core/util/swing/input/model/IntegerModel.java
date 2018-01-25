package io.opensphere.core.util.swing.input.model;

import io.opensphere.core.util.ObservableValueValidatorSupport;
import io.opensphere.core.util.PredicateWithMessage;
import io.opensphere.core.util.WrappedPredicateWithMessage;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.predicate.IntegerRangePredicate;
import io.opensphere.core.util.swing.input.model.PropertyChangeEvent.Property;

/**
 * Integer model.
 */
public class IntegerModel extends AbstractViewModel<Integer>
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The minimum allowed value. */
    private int myMin;

    /** The minimum allowed value. */
    private int myMax;

    /**
     * Constructor.
     */
    public IntegerModel()
    {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Constructor.
     *
     * @param min The minimum allowed value
     * @param max The maximum allowed value
     */
    public IntegerModel(int min, int max)
    {
        myMin = min;
        myMax = max;
        setValidator();
    }

    /**
     * Gets the max.
     *
     * @return the max
     */
    public int getMax()
    {
        return myMax;
    }

    /**
     * Gets the min.
     *
     * @return the min
     */
    public int getMin()
    {
        return myMin;
    }

    /**
     * Sets the max.
     *
     * @param max the max
     */
    public void setMax(int max)
    {
        myMax = max;
        setValidator();

        // Adjust the value if necessary
        Integer value = get();
        if (value != null && value.intValue() > myMax)
        {
            set(Integer.valueOf(myMax));
        }

        firePropertyChangeEvent(Property.VALIDATION_CRITERIA);
    }

    /**
     * Sets the min.
     *
     * @param min the min
     */
    public void setMin(int min)
    {
        myMin = min;
        setValidator();

        // Adjust the value if necessary
        Integer value = get();
        if (value != null && value.intValue() < myMin)
        {
            set(Integer.valueOf(myMin));
        }

        firePropertyChangeEvent(Property.VALIDATION_CRITERIA);
    }

    /**
     * Set the validator using the current validation criteria.
     */
    private void setValidator()
    {
        PredicateWithMessage<Integer> predicate = new WrappedPredicateWithMessage<Integer>(
                new IntegerRangePredicate(myMin, myMax))
        {
            @Override
            public String getMessage()
            {
                if (myMin > Integer.MIN_VALUE && myMax < Integer.MAX_VALUE)
                {
                    return StringUtilities.concat(getName(), " must be an integer between ", String.valueOf(myMin), " and ",
                            String.valueOf(myMax));
                }
                else if (myMin > Integer.MIN_VALUE)
                {
                    return StringUtilities.concat(getName(), " must be an integer >= ", String.valueOf(myMin));
                }
                else
                {
                    return StringUtilities.concat(getName(), " must be an integer <= ", String.valueOf(myMax));
                }
            }
        };
        setValidatorSupport(new ObservableValueValidatorSupport<Integer>(this, predicate));
    }
}
