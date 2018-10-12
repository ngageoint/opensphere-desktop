package io.opensphere.core.geometry.renderproperties;

import java.util.Collection;
import java.util.Collections;

import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * Abstract implementation that provides the change support for concrete render
 * properties.
 */
public abstract class AbstractRenderProperties implements RenderProperties
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** The change support. */
    private transient ChangeSupport<RenderPropertyChangeListener> myChangeSupport = new WeakChangeSupport<>();

    @Override
    public void addListener(RenderPropertyChangeListener listen)
    {
        myChangeSupport.addListener(listen);
    }

    @Override
    public AbstractRenderProperties clone()
    {
        try
        {
            final AbstractRenderProperties props = (AbstractRenderProperties)super.clone();
            props.myChangeSupport = new WeakChangeSupport<>();
            return props;
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj || obj != null && getClass() == obj.getClass();
    }

    @Override
    public Collection<? extends RenderProperties> getThisPlusDescendants()
    {
        return Collections.singleton(this);
    }

    @Override
    public int hashCode()
    {
        return 1;
    }

    @Override
    public void removeListener(RenderPropertyChangeListener listen)
    {
        myChangeSupport.removeListener(listen);
    }

    /** Notify listeners when one or more of my properties has changed. */
    protected void notifyChanged()
    {
        myChangeSupport.notifyListeners(listener ->
        {
            RenderPropertyChangedEvent evt = new RenderPropertyChangedEvent(AbstractRenderProperties.this);
            listener.propertyChanged(evt);
        }, null);
    }
}
