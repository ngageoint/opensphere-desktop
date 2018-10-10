package io.opensphere.core.util.swing.input.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.StrongChangeSupport;
import io.opensphere.core.util.StrongObservableValue;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;

/**
 * A model that wraps a domain model.
 *
 * @param <T> The type of the domain model
 */
public abstract class WrappedModel<T> extends StrongObservableValue<T> implements ViewModel<T>
{
    /** The value. */
    private T myValue;

    /** Whether the model has changed since the last mark. */
    private boolean myIsChanged;

    /** The list of all models. */
    private final Collection<ViewModel<?>> myModels = new ArrayList<>();

    /** The change support. */
    private final transient ChangeSupport<PropertyChangeListener> myPropertyChangeSupport = new StrongChangeSupport<>();

    /**
     * A temporary (hopefully) way of letting a listener know which part of the
     * model changed.
     */
    private Object myChangedSource;

    /** The property change listener. */
    private final PropertyChangeListener myPropertyChangeListener = e -> myPropertyChangeSupport
            .notifyListeners(listener -> listener.stateChanged(e));

    /** The value change listener. */
    private final ChangeListener<Object> myChangeListener = (observable, oldValue, newValue) ->
    {
        // Pass the change up to my listeners
        myChangedSource = observable;
        fireChangeEvent();
        myChangedSource = null;

        // Fire an additional event for listeners that need the event source
        myPropertyChangeListener
                .stateChanged(new PropertyChangeEvent(observable, PropertyChangeEvent.Property.WRAPPED_VALUE_CHANGED));
    };

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        myPropertyChangeSupport.addListener(listener);
    }

    /**
     * Applies changes from the view model to the domain model.
     */
    public void applyChanges()
    {
        updateDomainModel(myValue);
    }

    /**
     * Gets the changed source.
     *
     * @return the changed source
     */
    public Object getChangedSource()
    {
        return myChangedSource;
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public synchronized String getErrorMessage()
    {
        String firstError = null;

        Collection<ViewModel<?>> modelsToCheck = StreamUtilities.filter(myModels,
                (Predicate<ViewModel<?>>)model -> model.isEnabled() && model.isVisible()
                        && model.getValidationStatus() != ValidationStatus.VALID);

        if (!modelsToCheck.isEmpty())
        {
            ViewModel<?> worstModel = Collections.max(modelsToCheck,
                    (o1, o2) -> o1.getValidationStatus().compareTo(o2.getValidationStatus()));
            firstError = worstModel.getErrorMessage();
        }

        return firstError;
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public synchronized T get()
    {
        return myValue;
    }

    @Override
    public boolean isChanged()
    {
        boolean isChanged = myIsChanged;
        if (!isChanged)
        {
            for (ViewModel<?> model : myModels)
            {
                if (model.isEnabled() && model.isVisible())
                {
                    isChanged |= model.isChanged();
                }
            }
        }
        return isChanged;
    }

    @Override
    public boolean isEnabled()
    {
        // This is kind of meaningless for WrappedModel
        return true;
    }

    @Override
    public boolean isRequired()
    {
        return true;
    }

    @Override
    public ValidationStatus getValidationStatus()
    {
        ValidationStatus status = ValidationStatus.VALID;
        Set<ValidationStatus> statuses = New.set();
        for (ViewModel<?> model : myModels)
        {
            if (model.isEnabled() && model.isVisible())
            {
                statuses.add(model.getValidationStatus());
            }
        }

        if (!statuses.isEmpty())
        {
            status = Collections.max(statuses);
        }

        return status;
    }

    @Override
    public boolean isValidating()
    {
        return myModels.stream().allMatch(model -> model.isValidating());
    }

    @Override
    public boolean isVisible()
    {
        // Not sure what to do here
        return true;
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        myPropertyChangeSupport.removeListener(listener);
    }

    @Override
    public void setChanged(boolean isChanged)
    {
        myIsChanged = isChanged;
        for (ViewModel<?> model : myModels)
        {
            model.setChanged(isChanged);
        }
    }

    @Override
    public void setEnabled(boolean isEnabled)
    {
        for (ViewModel<?> model : myModels)
        {
            model.setEnabled(isEnabled);
        }
    }

    @Override
    public void setValidating(boolean isValidating)
    {
        for (ViewModel<?> model : myModels)
        {
            model.setValidating(isValidating);
        }
    }

    @Override
    public boolean set(T value)
    {
        myValue = value;
        myIsChanged = true;
        updateViewModel(value);
        return true;
    }

    @Override
    public void setVisible(boolean isVisible)
    {
        for (ViewModel<?> model : myModels)
        {
            model.setVisible(isVisible);
        }
    }

    /**
     * Adds a model.
     *
     * @param model the model
     */
    protected void addModel(ViewModel<?> model)
    {
        myModels.add(model);
        model.addListener(myChangeListener);
        model.addPropertyChangeListener(myPropertyChangeListener);
    }

    /**
     * Removes a model.
     *
     * @param model the model
     */
    protected void removeModel(ViewModel<?> model)
    {
        myModels.remove(model);
        model.removeListener(myChangeListener);
        model.removePropertyChangeListener(myPropertyChangeListener);
    }

    /**
     * Gets the changeListener.
     *
     * @return the changeListener
     */
    protected ChangeListener<Object> getChangeListener()
    {
        return myChangeListener;
    }

    /**
     * Updates the domain model from the view model.
     *
     * @param domainModel The domain model
     */
    protected abstract void updateDomainModel(T domainModel);

    /**
     * Updates the view model from the domain model.
     *
     * @param domainModel The domain model
     */
    protected abstract void updateViewModel(T domainModel);
}
