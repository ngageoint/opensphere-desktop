package io.opensphere.core.util.swing.input.model;

import java.util.List;

import io.opensphere.core.util.ObservableValueValidatorSupport;
import io.opensphere.core.util.PredicateWithMessage;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.predicate.InPredicate;
import io.opensphere.core.util.swing.input.model.PropertyChangeEvent.Property;

/**
 * Choice model.
 *
 * @param <T> The type of the options
 */
public class ChoiceModel<T> extends AbstractViewModel<T>
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The options. */
    private final List<T> myOptions;

    /**
     * Constructor.
     */
    public ChoiceModel()
    {
        myOptions = New.list();
    }

    /**
     * Constructor.
     *
     * @param options The options
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public ChoiceModel(T... options)
    {
        myOptions = New.list(options);
        setValidator();
    }

    /**
     * Gets the options.
     *
     * @return the options
     */
    public List<T> getOptions()
    {
        return New.list(myOptions);
    }

    /**
     * Adds the options.
     *
     * @param options the options
     */
    public void addOptions(T[] options)
    {
        for (T option : options)
        {
            myOptions.add(option);
        }
        setValidator();

        firePropertyChangeEvent(Property.OPTIONS);
    }

    /**
     * Sets the options and updates the value to the previously set value or the
     * first option if it's not available.
     *
     * @param options the options
     */
    public void setOptions(T[] options)
    {
        T value = get();

        setOptionsOnly(options);

        T newValue = containsOption(value) ? value : myOptions.get(0);
        set(newValue, true);
    }

    /**
     * Sets the options without updating the value.
     *
     * @param options the options
     */
    public void setOptionsOnly(T[] options)
    {
        myOptions.clear();
        for (T option : options)
        {
            myOptions.add(option);
        }
        setValidator();

        firePropertyChangeEvent(Property.OPTIONS);
    }

    /**
     * Determines if the options contain the given value.
     *
     * @param value the value
     * @return whether the options contain the given value
     */
    public boolean containsOption(T value)
    {
        return value != null && myOptions.contains(value);
    }

    /**
     * Set the validator using the current options.
     */
    private void setValidator()
    {
        final InPredicate inPredicate = new InPredicate(myOptions);
        PredicateWithMessage<T> predicate = new PredicateWithMessage<>()
        {
            @Override
            public String getMessage()
            {
                return StringUtilities.concat("Invalid value: ", get());
            }

            @Override
            public boolean test(T input)
            {
                return inPredicate.test(input);
            }
        };
        setValidatorSupport(new ObservableValueValidatorSupport<>(this, predicate));
    }
}
