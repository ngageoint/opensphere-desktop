package io.opensphere.core.util.swing.input.model;

import java.awt.EventQueue;
import java.io.Serializable;
import java.util.Collection;

import org.apache.log4j.Logger;

import io.opensphere.core.util.AbstractChangeSupport;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.StrongChangeSupport;
import io.opensphere.core.util.StrongObservableValue;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.ref.Reference;

/**
 * Abstract view model.
 *
 * @param <T> The type of the model
 */
public abstract class AbstractViewModel<T> extends StrongObservableValue<T> implements ViewModel<T>, Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractViewModel.class);

    /** Whether the model is valid. */
    private boolean myIsValid = true;

    /** Whether the model is required. */
    private boolean myIsRequired = true;

    /** Whether the model is enabled. */
    private boolean myIsEnabled = true;

    /** Whether the model is visible. */
    private boolean myIsVisible = true;

    /** Whether the model has changed since the last mark. */
    private boolean myIsChanged;

    /** The name of the model. */
    private String myName;

    /** The description of the model. */
    private String myDescription;

    /** The validator support. */
    private transient ValidatorSupport myValidatorSupport;

    /** Whether the model is validating. */
    private boolean myIsValidating = true;

    /** The change support. */
    private final transient ChangeSupport<PropertyChangeListener> myChangeSupport = new StrongChangeSupport<>();

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        myChangeSupport.addListener(listener);
        if (LOGGER.isDebugEnabled() && myChangeSupport.getListenerCount() > 5)
        {
            LOGGER.warn(myChangeSupport.getListenerCount() + " listeners for " + myName + "=" + get());
        }
    }

    @Override
    public String getDescription()
    {
        return myDescription;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the property change listeners.
     *
     * @return the property change listeners
     */
    public Collection<PropertyChangeListener> getPropertyChangeListeners()
    {
        Collection<PropertyChangeListener> listeners = New.list(myChangeSupport.getListenerCount());
        AbstractChangeSupport<PropertyChangeListener> support = (AbstractChangeSupport<PropertyChangeListener>)myChangeSupport;
        for (Reference<PropertyChangeListener> reference : support.getListeners())
        {
            listeners.add(reference.get());
        }
        return listeners;
    }

    /**
     * Gets the validator support.
     *
     * @return the validator support
     */
    public ValidatorSupport getValidatorSupport()
    {
        return myValidatorSupport;
    }

    @Override
    public boolean isChanged()
    {
        return myIsChanged;
    }

    @Override
    public boolean isEnabled()
    {
        return myIsEnabled;
    }

    @Override
    public boolean isRequired()
    {
        return myIsRequired;
    }

    @Override
    public ValidationStatus getValidationStatus()
    {
        if (!myIsValidating)
        {
            return ValidationStatus.VALID;
        }
        if (!myIsValid)
        {
            setError(null, null);
            return ValidationStatus.ERROR;
        }
        if (get() == null)
        {
            if (myIsRequired)
            {
                setError(StringUtilities.concat(myName, " is a required field."), null);
                return ValidationStatus.ERROR;
            }
        }
        else if (myValidatorSupport != null && myValidatorSupport.getValidationStatus() != ValidationStatus.VALID)
        {
            setError(myValidatorSupport.getValidationMessage(), null);
            return ValidationStatus.ERROR;
        }
        return ValidationStatus.VALID;
    }

    @Override
    public boolean isValidating()
    {
        return myIsValidating;
    }

    @Override
    public boolean isVisible()
    {
        return myIsVisible;
    }

    /**
     * Removes all listeners.
     */
    @Override
    public void removeAllListeners()
    {
        AbstractChangeSupport<PropertyChangeListener> support = (AbstractChangeSupport<PropertyChangeListener>)myChangeSupport;
        for (Reference<PropertyChangeListener> reference : support.getListeners())
        {
            myChangeSupport.removeListener(reference.get());
        }
        super.removeAllListeners();
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public void setChanged(boolean isChanged)
    {
        myIsChanged = isChanged;
    }

    /**
     * Sets the description.
     *
     * @param description The description
     */
    public void setDescription(String description)
    {
        myDescription = description;
    }

    @Override
    public void setEnabled(boolean isEnabled)
    {
        if (myIsEnabled != isEnabled)
        {
            myIsEnabled = isEnabled;
            firePropertyChangeEvent(PropertyChangeEvent.Property.ENABLED);
        }
    }

    /**
     * Sets the name.
     *
     * @param name The name
     */
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Sets the name and description.
     *
     * @param name The name
     * @param description The description
     */
    public void setNameAndDescription(String name, String description)
    {
        if (!EqualsHelper.equals(myName, name, myDescription, description))
        {
            myName = name;
            myDescription = description;
            firePropertyChangeEvent(PropertyChangeEvent.Property.NAME_AND_DESCRIPTION);
        }
    }

    /**
     * Sets whether the model is required.
     *
     * @param isRequired Whether the model is required
     */
    public void setRequired(boolean isRequired)
    {
        if (myIsRequired != isRequired)
        {
            myIsRequired = isRequired;
            firePropertyChangeEvent(PropertyChangeEvent.Property.REQUIRED);
        }
    }

    /**
     * Sets whether the model is valid.
     *
     * @param isValid Whether the model is valid
     * @param source The source
     */
    public void setValid(boolean isValid, Object source)
    {
        if (myIsValid != isValid)
        {
            myIsValid = isValid;
            firePropertyChangeEvent(PropertyChangeEvent.Property.VALIDATION_CRITERIA);
        }
    }

    @Override
    public void setValidating(boolean isValidating)
    {
        if (myIsValidating != isValidating)
        {
            myIsValidating = isValidating;
            firePropertyChangeEvent(PropertyChangeEvent.Property.VALIDATION_CRITERIA);
        }
    }

    /**
     * Sets the validator support.
     *
     * @param validator the validator support
     */
    public void setValidatorSupport(ValidatorSupport validator)
    {
        myValidatorSupport = validator;
    }

    @Override
    public boolean set(T value, boolean forceFire)
    {
        assert getChangeSupport().isEmpty() || EventQueue.isDispatchThread();
        boolean changed = super.set(value, forceFire);
        if (changed)
        {
            setValid(true, null);
            myIsChanged = true;
        }
        return changed;
    }

    @Override
    public void setVisible(boolean isVisible)
    {
        if (myIsVisible != isVisible)
        {
            myIsVisible = isVisible;
            firePropertyChangeEvent(PropertyChangeEvent.Property.VISIBLE);
        }
    }

    /**
     * Fires a PropertyChangeEvent.
     *
     * @param source The source
     * @param property The event type
     */
    protected void firePropertyChangeEvent(Object source, PropertyChangeEvent.Property property)
    {
        final PropertyChangeEvent event = new PropertyChangeEvent(source == null ? this : source, property);
        myChangeSupport.notifyListeners(new Callback<PropertyChangeListener>()
        {
            @Override
            public void notify(PropertyChangeListener listener)
            {
                listener.stateChanged(event);
            }
        });
    }

    /**
     * Fires a PropertyChangeEvent.
     *
     * @param property The property
     */
    protected void firePropertyChangeEvent(PropertyChangeEvent.Property property)
    {
        firePropertyChangeEvent(null, property);
    }
}
