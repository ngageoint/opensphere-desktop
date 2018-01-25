package io.opensphere.core.util.javafx.input.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;

/**
 * Abstract view model.
 *
 * @param <T> The type of the model
 */
public class AbstractViewModel<T> implements ViewModel<T>
{
    /**
     * An observable field in which the object property is maintained.
     */
    private final ObjectProperty<T> myObjectProperty = new SimpleObjectProperty<>();

    /**
     * An observable field in which the valid status is maintained.
     */
    private final BooleanProperty myValidProperty = new SimpleBooleanProperty(false);

    /**
     * An observable field in which the required state of the model is maintained.
     */
    private final BooleanProperty myRequiredProperty = new SimpleBooleanProperty(false);

    /**
     * An observable field in which the enabled state of the model is maintained.
     */
    private final BooleanProperty myEnabledProperty = new SimpleBooleanProperty(true);

    /**
     * An observable field in which the visible state of the model is maintained.
     */
    private final BooleanProperty myVisibleProperty = new SimpleBooleanProperty(true);

    /**
     * An observable field in which the validating state of the model is maintained.
     */
    private final BooleanProperty myValidatingProperty = new SimpleBooleanProperty(true);

    /**
     * An observable field in which the name of the model is maintained.
     */
    private final StringProperty myNameProperty = new SimpleStringProperty();

    /**
     * An observable field in which the name of the model is maintained.
     */
    private final StringProperty myDescriptionProperty = new SimpleStringProperty();

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description)
    {
        myDescriptionProperty.set(description);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#getDescription()
     */
    @Override
    public String getDescription()
    {
        return myDescriptionProperty.get();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#getDescriptionProperty()
     */
    @Override
    public ObservableStringValue getDescriptionProperty()
    {
        return myDescriptionProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean isEnabled)
    {
        myEnabledProperty.set(isEnabled);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#isEnabled()
     */
    @Override
    public boolean isEnabled()
    {
        return myEnabledProperty.get();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#getEnabledProperty()
     */
    @Override
    public ObservableBooleanValue getEnabledProperty()
    {
        return myEnabledProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#setName(java.lang.String)
     */
    @Override
    public void setName(String name)
    {
        myNameProperty.set(name);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#getName()
     */
    @Override
    public String getName()
    {
        return myNameProperty.get();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#getNameProperty()
     */
    @Override
    public ObservableStringValue getNameProperty()
    {
        return myNameProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#setValid(boolean)
     */
    @Override
    public void setValid(boolean isValid)
    {
        myValidProperty.set(isValid);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#isValid()
     */
    @Override
    public boolean isValid()
    {
        return myValidProperty.get();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#getValidProperty()
     */
    @Override
    public BooleanProperty getValidProperty()
    {
        return myValidProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#setValidating(boolean)
     */
    @Override
    public void setValidating(boolean isValidating)
    {
        myValidatingProperty.set(isValidating);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#isValidating()
     */
    @Override
    public boolean isValidating()
    {
        return myValidatingProperty.get();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#getValidatingProperty()
     */
    @Override
    public ObservableBooleanValue getValidatingProperty()
    {
        return myValidatingProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean pVisible)
    {
        myVisibleProperty.set(pVisible);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#isVisible()
     */
    @Override
    public boolean isVisible()
    {
        return myVisibleProperty.get();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#getVisibleProperty()
     */
    @Override
    public ObservableBooleanValue getVisibleProperty()
    {
        return myVisibleProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#setRequired(boolean)
     */
    @Override
    public void setRequired(boolean pRequired)
    {
        myRequiredProperty.set(pRequired);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#isRequired()
     */
    @Override
    public boolean isRequired()
    {
        return myRequiredProperty.get();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#getRequiredProperty()
     */
    @Override
    public ObservableBooleanValue getRequiredProperty()
    {
        return myRequiredProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#set(java.lang.Object)
     */
    @Override
    public void set(T pValue)
    {
        myObjectProperty.set(pValue);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#get()
     */
    @Override
    public T get()
    {
        return myObjectProperty.get();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.model.ViewModel#getProperty()
     */
    @Override
    public ObservableValue<T> getProperty()
    {
        return myObjectProperty;
    }
}
