package io.opensphere.kml.settings;

import java.awt.EventQueue;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Observable;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

import io.opensphere.core.util.NonSuckingObservable;
import io.opensphere.kml.common.model.KMLSettings;
import io.opensphere.kml.common.model.ScalingMethod;

/** KMLSettings view model. */
class KMLSettingsViewModel
{
    /** The scaling method. */
    private final ObjectProperty<ScalingMethod> myScalingMethod = new SimpleObjectProperty<>(this, "scalingMethod");

    /** The changed observable. */
    private final transient Observable myChanged = new NonSuckingObservable();

    /**
     * Constructor.
     */
    public KMLSettingsViewModel()
    {
        ChangeListener<Object> listener = (observable, oldValue, newValue) -> myChanged.notifyObservers();
        myScalingMethod.addListener(listener);
    }

    /**
     * Sets the scaling method.
     *
     * @param scalingMethod the scaling method
     */
    public final void setScalingMethod(ScalingMethod scalingMethod)
    {
        myScalingMethod.set(scalingMethod);
    }

    /**
     * Gets the scaling method.
     *
     * @return the scaling method
     */
    public final ScalingMethod getScalingMethod()
    {
        return myScalingMethod.get();
    }

    /**
     * Gets the scaling method property.
     *
     * @return the scaling method property
     */
    public ObjectProperty<ScalingMethod> scalingMethodProperty()
    {
        return myScalingMethod;
    }

    /**
     * Gets the scaling method options.
     *
     * @return the scaling method options
     */
    public Collection<ScalingMethod> getScalingMethodOptions()
    {
        return EnumSet.of(ScalingMethod.GOOGLE_EARTH, ScalingMethod.FIXED_SIZE);
    }

    /**
     * Gets the changed observable.
     *
     * @return the changed observable
     */
    public Observable getChanged()
    {
        return myChanged;
    }

    /**
     * Sets the values in this object from the domain model.
     *
     * @param model the domain model
     */
    public void populateFromDomainModel(KMLSettings model)
    {
        assert EventQueue.isDispatchThread();

        setScalingMethod(model.getScalingMethod());
    }

    /**
     * Sets the values from this object into the domain model.
     *
     * @param model the domain model
     */
    public void populateDomainModel(KMLSettings model)
    {
        assert EventQueue.isDispatchThread();

        model.setScalingMethod(getScalingMethod());
    }
}
