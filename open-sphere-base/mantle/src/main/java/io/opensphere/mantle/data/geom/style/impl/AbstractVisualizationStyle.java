package io.opensphere.mantle.data.geom.style.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ExpectedCloneableException;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationControlPanel;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeListener;
import io.opensphere.mantle.data.geom.style.VisualizationStyleUtilities;

/**
 * The Class AbstractVisualizationStyle.
 */
public abstract class AbstractVisualizationStyle implements MutableVisualizationStyle
{
    /** The ExecutorService. */
    private static final ExecutorService ourExecutor = Executors.newFixedThreadPool(1,
            new NamedThreadFactory("AbstractVisualizationStyle::Dispatch", 3, 4));

    /** The dti key. */
    private String myDTIKey;

    /** The Parameter change support. */
    private WeakChangeSupport<VisualizationStyleParameterChangeListener> myParameterChangeSupport;

    /** The Parameter set. */
    private Map<String, VisualizationStyleParameter> myParameterKeyToParameterMap;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new abstract visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public AbstractVisualizationStyle(Toolbox tb)
    {
        this(tb, null);
    }

    /**
     * Instantiates a new abstract visualization style.
     *
     * @param tb the {@link Toolbox}.
     * @param dtiKey the {@link DataTypeInfo} key.
     */
    public AbstractVisualizationStyle(Toolbox tb, String dtiKey)
    {
        Utilities.checkNull(tb, "toolbox");
        myToolbox = tb;
        myParameterKeyToParameterMap = New.map();
        myDTIKey = dtiKey;
        myParameterChangeSupport = new WeakChangeSupport<>();
    }

    @Override
    public void addStyleParameterChangeListener(VisualizationStyleParameterChangeListener listener)
    {
        myParameterChangeSupport.addListener(listener);
    }

    @Override
    public AbstractVisualizationStyle clone()
    {
        try
        {
            final AbstractVisualizationStyle props = (AbstractVisualizationStyle)super.clone();

            // Don't clone the listener list.
            props.myParameterKeyToParameterMap = New.map(myParameterKeyToParameterMap);
            props.myParameterChangeSupport = new WeakChangeSupport<>();
            return props;
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    @Override
    public Set<VisualizationStyleParameter> getAlwaysSaveParameters()
    {
        return New.set();
    }

    @Override
    public Set<VisualizationStyleParameter> getChangedParameters(VisualizationStyle other)
    {
        return VisualizationStyleUtilities.getChangedParameters(other, this);
    }

    @Override
    public String getDTIKey()
    {
        return myDTIKey;
    }

    @Override
    public VisualizationStyleParameter getStyleParameter(String paramKey)
    {
        VisualizationStyleParameter result = null;
        synchronized (myParameterKeyToParameterMap)
        {
            result = myParameterKeyToParameterMap.get(paramKey);
        }
        return result;
    }

    @Override
    public Set<String> getStyleParameterKeys()
    {
        Set<String> keySet = null;
        synchronized (myParameterKeyToParameterMap)
        {
            keySet = New.set(myParameterKeyToParameterMap.keySet());
        }
        return Collections.unmodifiableSet(keySet);
    }

    @Override
    public Set<VisualizationStyleParameter> getStyleParameterSet()
    {
        Set<VisualizationStyleParameter> pSet = null;
        synchronized (myParameterKeyToParameterMap)
        {
            pSet = New.set(myParameterKeyToParameterMap.values());
        }
        return Collections.unmodifiableSet(pSet);
    }

    @Override
    public Object getStyleParameterValue(String paramKey)
    {
        VisualizationStyleParameter parameter = getStyleParameter(paramKey);
        return parameter == null ? null : parameter.getValue();
    }

    @Override
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    @Override
    public abstract FeatureVisualizationControlPanel getUIPanel();

    @Override
    public void removeStyleParameterChangeListener(VisualizationStyleParameterChangeListener listener)
    {
        myParameterChangeSupport.removeListener(listener);
    }

    @Override
    public boolean requiresShaders()
    {
        return false;
    }

    @Override
    public void revertToDefaultParameters(Object source)
    {
        // Generate a unaltered copy of the style
        VisualizationStyle style = newInstance(myToolbox);
        if (style != null)
        {
            if (getDTIKey() != null)
            {
                style.setDTIKey(getDTIKey());
                style.initializeFromDataType();
            }
            setParameters(style.getStyleParameterSet(), source);
        }
    }

    @Override
    public boolean setParameter(String paramKey, Object newValue, Object source) throws IllegalArgumentException
    {
        boolean changed = false;
        VisualizationStyleParameter oldParameter = null;
        VisualizationStyleParameter newParam = null;
        synchronized (myParameterKeyToParameterMap)
        {
            oldParameter = myParameterKeyToParameterMap.get(paramKey);
            if (oldParameter != null && !Objects.equals(oldParameter.getValue(), newValue))
            {
                changed = true;
                newParam = oldParameter.deriveWithNewValue(newValue);
                myParameterKeyToParameterMap.put(paramKey, newParam);
            }
        }
        if (changed)
        {
            fireParameterChangeEvent(Collections.singleton(newParam), source);
        }
        return changed;
    }

    @Override
    public Set<VisualizationStyleParameter> setParameters(Set<VisualizationStyleParameter> parameters, Object source)
    {
        Set<VisualizationStyleParameter> changedParameters = New.set();
        synchronized (myParameterKeyToParameterMap)
        {
            for (VisualizationStyleParameter param : parameters)
            {
                VisualizationStyleParameter oldParameter = myParameterKeyToParameterMap.get(param.getKey());
                if (oldParameter != null && !Objects.equals(oldParameter.getValue(), param.getValue()))
                {
                    VisualizationStyleParameter newParam = oldParameter.deriveWithNewValue(param.getValue());
                    myParameterKeyToParameterMap.put(newParam.getKey(), newParam);
                    changedParameters.add(newParam);
                }
            }
        }
        if (!changedParameters.isEmpty())
        {
            fireParameterChangeEvent(changedParameters, source);
        }
        return Collections.unmodifiableSet(changedParameters);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append(getClass().getName()).append("\n" + " DataType[").append(getDTIKey()).append("]\n" + " Parameters: ")
                .append(myParameterKeyToParameterMap.size()).append('\n');
        synchronized (myParameterKeyToParameterMap)
        {
            for (Map.Entry<String, VisualizationStyleParameter> entry : myParameterKeyToParameterMap.entrySet())
            {
                sb.append("   ").append(entry).append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * Fire parameter change event.
     *
     * @param changedSet the changed set
     * @param source the source
     */
    protected void fireParameterChangeEvent(final Set<VisualizationStyleParameter> changedSet, final Object source)
    {
        final VisualizationStyleParameterChangeEvent event = new VisualizationStyleParameterChangeEvent(myDTIKey, this,
                changedSet, source);
        if (!Utilities.sameInstance(NO_EVENT_SOURCE, source))
        {
            myParameterChangeSupport.notifyListeners(listener -> listener.styleParametersChanged(event), ourExecutor);
        }
    }

    @Override
    public void setDTIKey(String dtiKey)
    {
        myDTIKey = dtiKey;
    }

    /**
     * Sets the parameter.
     *
     * @param param the parameter to set.
     * @return the existing visualization parameter with the key name that is
     *         replaced or null if no parameter is replaced.
     */
    protected VisualizationStyleParameter setParameter(VisualizationStyleParameter param)
    {
        VisualizationStyleParameter oldParameter = null;
        synchronized (myParameterKeyToParameterMap)
        {
            oldParameter = myParameterKeyToParameterMap.put(param.getKey(), param);
        }
        return oldParameter;
    }
}
