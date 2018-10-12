package io.opensphere.mantle.transformer.impl;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleDatatypeChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeListener;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class TransfomerStyleAssistant.
 */
public class TransfomerStyleAssistant
        implements VisualizationStyleParameterChangeListener, VisualizationStyleRegistry.VisualizationStyleRegistryChangeListener
{
    /** The Collection lock. */
    private final ReentrantLock myCollectionLock;

    /** The Data type. */
    private final DataTypeInfo myDataType;

    /** The Listener. */
    private final StyleAssistantChangeListener myListener;

    /** The MGS class to style map. */
    private final Map<Class<? extends MapGeometrySupport>, FeatureVisualizationStyle> myMGSClassToStyleMap;

    /** The Style registry. */
    private final VisualizationStyleRegistry myStyleRegistry;

    /** The Style set. */
    private final Set<FeatureVisualizationStyle> myStyleSet;

    /**
     * Instantiates a new transformer style assistant.
     *
     * @param tb the tb
     * @param dti the {@link DataTypeInfo}
     * @param listener the listener
     */
    public TransfomerStyleAssistant(Toolbox tb, DataTypeInfo dti, StyleAssistantChangeListener listener)
    {
        myDataType = dti;
        myStyleRegistry = MantleToolboxUtils.getMantleToolbox(tb).getVisualizationStyleRegistry();
        myCollectionLock = new ReentrantLock();
        myStyleRegistry.addVisualizationStyleRegistryChangeListener(this);
        myMGSClassToStyleMap = New.concurrentMap();
        myStyleSet = New.set();
        myListener = listener;
    }

    /**
     * Close.
     */
    public void close()
    {
        myCollectionLock.lock();
        try
        {
            myMGSClassToStyleMap.clear();
            myStyleRegistry.removeVisualizationStyleRegistryChangeListener(this);
            for (FeatureVisualizationStyle style : myStyleSet)
            {
                style.removeStyleParameterChangeListener(this);
            }
            myStyleSet.clear();
        }
        finally
        {
            myCollectionLock.unlock();
        }
    }

    @Override
    public void defaultStyleChanged(Class<? extends VisualizationSupport> mgsClass,
            Class<? extends VisualizationStyle> styleClass, Object source)
    {
        // Don't care we will get visualizationStyleDatatypeChanged
    }

    /**
     * Gets the style.
     *
     * @param mgs the mgs
     * @return the style
     */
    public FeatureVisualizationStyle getStyle(MapGeometrySupport mgs)
    {
        FeatureVisualizationStyle fvs = null;
        myCollectionLock.lock();
        try
        {
            fvs = myMGSClassToStyleMap.get(mgs.getClass());
            if (fvs == null)
            {
                VisualizationStyle style = myStyleRegistry.getStyle(mgs.getClass(), myDataType.getTypeKey(), false);
                if (style == null)
                {
                    style = myStyleRegistry.getDefaultStyle(mgs.getClass());
                    if (style instanceof FeatureVisualizationStyle)
                    {
                        fvs = (FeatureVisualizationStyle)style;
                        myMGSClassToStyleMap.put(mgs.getClass(), fvs);
                        myStyleSet.add(fvs);
                        fvs.addStyleParameterChangeListener(this);
                    }
                }
            }
        }
        finally
        {
            myCollectionLock.unlock();
        }
        return fvs;
    }

    @Override
    public void styleParametersChanged(VisualizationStyleParameterChangeEvent evt)
    {
        VisualizationStyle changedStyle = null;
        Set<VisualizationStyleParameter> changedParameters = null;
        myCollectionLock.lock();
        try
        {
            if (myStyleSet.contains(evt.getStyle()))
            {
                changedStyle = evt.getStyle();
                changedParameters = evt.getChangedParameterSet();
            }
        }
        finally
        {
            myCollectionLock.unlock();
        }
        if (changedStyle != null && changedParameters != null && !changedParameters.isEmpty())
        {
            notifyListenerStyleParametersChanged(changedStyle, evt.getChangedParameterKeyToParameterMap(),
                    evt.requiresGeometryRebuild());
        }
    }

    /**
     * Styles require meta data.
     *
     * @return true, if successful
     */
    public boolean stylesRequireMetaData()
    {
        boolean requires = false;
        myCollectionLock.lock();
        try
        {
            for (FeatureVisualizationStyle style : myStyleSet)
            {
                if (style.requiresMetaData())
                {
                    requires = true;
                    break;
                }
            }
        }
        finally
        {
            myCollectionLock.unlock();
        }
        return requires;
    }

    @Override
    public void visualizationStyleDatatypeChanged(VisualizationStyleDatatypeChangeEvent evt)
    {
        if (evt.getDTIKey() == null || Objects.equals(myDataType.getTypeKey(), evt.getDTIKey()))
        {
            replaceStyle((FeatureVisualizationStyle)evt.getOldStyle(), (FeatureVisualizationStyle)evt.getNewStyle());
        }
    }

    @Override
    public void visualizationStyleInstalled(Class<? extends VisualizationStyle> styleClass, Object source)
    {
        // Don't care.
    }

    /**
     * Notify listener style parameters changed.
     *
     * @param style the style
     * @param changedParameters the changed parameters
     * @param requiresGeometryRebuild the requires geometry rebuild
     */
    private void notifyListenerStyleParametersChanged(VisualizationStyle style,
            Map<String, VisualizationStyleParameter> changedParameters, boolean requiresGeometryRebuild)
    {
        if (myListener != null)
        {
            myListener.styleChanged(style, changedParameters, requiresGeometryRebuild);
        }
    }

    /**
     * Notify listener style replaced.
     */
    private void notifyListenerStyleReplaced()
    {
        if (myListener != null)
        {
            myListener.styleReplaced();
        }
    }

    /**
     * Replace style.
     *
     * @param oldStyle the old style
     * @param newStyle the new style
     */
    private void replaceStyle(FeatureVisualizationStyle oldStyle, FeatureVisualizationStyle newStyle)
    {
        boolean replaced = false;
        myCollectionLock.lock();
        try
        {
            if (myStyleSet.contains(oldStyle))
            {
                oldStyle.removeStyleParameterChangeListener(this);
                myStyleSet.remove(oldStyle);
                myStyleSet.add(newStyle);
                newStyle.addStyleParameterChangeListener(this);
                Set<Class<? extends MapGeometrySupport>> keysToChange = New.set();
                for (Map.Entry<Class<? extends MapGeometrySupport>, FeatureVisualizationStyle> entry : myMGSClassToStyleMap
                        .entrySet())
                {
                    if (Utilities.sameInstance(oldStyle, entry.getValue()))
                    {
                        keysToChange.add(entry.getKey());
                    }
                }
                if (!keysToChange.isEmpty())
                {
                    replaced = true;
                    for (Class<? extends MapGeometrySupport> cl : keysToChange)
                    {
                        myMGSClassToStyleMap.put(cl, newStyle);
                    }
                }
            }
        }
        finally
        {
            myCollectionLock.unlock();
        }
        if (replaced)
        {
            notifyListenerStyleReplaced();
        }
    }

    /**
     * The StyleAssistantChangeListener.
     */
    public interface StyleAssistantChangeListener
    {
        /**
         * Style changed.
         *
         * @param style the style
         * @param changedParameters the changed parameters
         * @param requiresGeometryRebuild the requires geometry rebuild
         */
        void styleChanged(VisualizationStyle style, Map<String, VisualizationStyleParameter> changedParameters,
                boolean requiresGeometryRebuild);

        /**
         * Style replaced.
         */
        void styleReplaced();
    }
}
