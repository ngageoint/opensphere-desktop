package io.opensphere.core.util.swing.input.model;

import java.util.Collection;
import java.util.Collections;

import io.opensphere.core.util.CompoundPredicateWithMessage;
import io.opensphere.core.util.ObservableValueValidatorSupport;
import io.opensphere.core.util.PredicateWithMessage;
import io.opensphere.core.util.WrappedPredicateWithMessage;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.predicate.NonBlankPredicate;
import io.opensphere.core.util.predicate.NotInPredicate;

/**
 * Model for a name.
 */
public class NameModel extends TextModel
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The disallowed names. */
    private Collection<? extends String> myDisallowedNames;

    /** An optional custom predicate. */
    private final transient PredicateWithMessage<String> myCustomPredicate;

    /**
     * Constructor.
     */
    public NameModel()
    {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param customPredicate a custom predicate for validation
     */
    public NameModel(PredicateWithMessage<String> customPredicate)
    {
        myCustomPredicate = customPredicate;
        setDisallowedNames(Collections.<String>emptyList());
    }

    /**
     * Sets the disallowed names.
     *
     * @param disallowedNames The disallowed names
     */
    public final void setDisallowedNames(Collection<? extends String> disallowedNames)
    {
        myDisallowedNames = New.unmodifiableCollection(disallowedNames);
        setValidator();
        firePropertyChangeEvent(PropertyChangeEvent.Property.VALIDATION_CRITERIA);
    }

    @Override
    public void setRequired(boolean isRequired)
    {
        super.setRequired(isRequired);
        setValidator();
    }

    @Override
    public boolean set(String value)
    {
        return super.set(StringUtilities.trim(value));
    }

    /**
     * Set the validator based on the current disallowed names and required
     * properties.
     */
    private void setValidator()
    {
        Collection<PredicateWithMessage<? super String>> predicates = New.collection();
        if (isRequired())
        {
            predicates.add(new WrappedPredicateWithMessage<String>(new NonBlankPredicate())
            {
                @Override
                public String getMessage()
                {
                    return StringUtilities.concat(getName(), " cannot be blank.");
                }
            });
        }
        predicates.add(new WrappedPredicateWithMessage<String>(new NotInPredicate(myDisallowedNames))
        {
            @Override
            public String getMessage()
            {
                return StringUtilities.concat(getName(), " is already in use.");
            }
        });
        if (myCustomPredicate != null)
        {
            predicates.add(myCustomPredicate);
        }
        PredicateWithMessage<String> predicate = new CompoundPredicateWithMessage<>(predicates);
        setValidatorSupport(new ObservableValueValidatorSupport<String>(this, predicate));
    }
}
