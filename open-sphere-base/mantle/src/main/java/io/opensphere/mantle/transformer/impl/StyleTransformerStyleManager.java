package io.opensphere.mantle.transformer.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.concurrent.GuardedBy;

import org.apache.log4j.Logger;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle.AppliesTo;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleDatatypeChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeListener;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry;
import io.opensphere.mantle.data.geom.style.VisualizationStyleUtilities;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class StyleTransformerStyleManager.
 */
public class StyleTransformerStyleManager
        implements VisualizationStyleParameterChangeListener, VisualizationStyleRegistry.VisualizationStyleRegistryChangeListener
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(StyleTransformerStyleManager.class);

    /** The MGS class to first interface map. */
    private static final Map<Class<? extends VisualizationSupport>, Class<? extends VisualizationSupport>> ourMGSClassToFirstInterfaceMap = New
            .map();

    /** The data type info. */
    private final DataTypeInfo myDataTypeInfo;

    /** The Id to mgs class map. */
    private final TLongObjectMap<Class<? extends VisualizationSupport>> myIdToMGSClassMap;

    /** The MGS class to id map. */
    private final TObjectLongMap<Class<? extends VisualizationSupport>> myMGSClassToIdMap;

    /** The MGS id counter. */
    private final AtomicLong myMGSIdCounter = new AtomicLong();

    /** The MGS to sub transformer map. */
    private final Map<Class<? extends VisualizationSupport>, FeatureVisualizationStyle> myMGSToStyleMap;

    /** Map of data element IDs to their overridden style, if any. */
    @GuardedBy("myOverrideLock")
    private final TLongObjectMap<FeatureVisualizationStyle> myOverrideStyleMap = new TLongObjectHashMap<>();

    /** Read/Write lock for myOverrideStyleMap. */
    private final ReentrantReadWriteLock myOverrideLock;

    /** The MGS to sub transformer map lock. */
    private final ReentrantLock myStyleLock;

    /** The Style set. */
    private final Set<FeatureVisualizationStyle> myStyleSet;

    /** The Style transformer geometry processor. */
    private final StyleTransformerGeometryProcessor myStyleTransformerGeometryProcessor;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new style transformer style manager.
     *
     * @param tb the {@link Toolbox}
     * @param dti the {@link DataTypeInfo}
     * @param processor the processor
     */
    public StyleTransformerStyleManager(Toolbox tb, DataTypeInfo dti, StyleTransformerGeometryProcessor processor)
    {
        Utilities.checkNull(tb, "tb");
        Utilities.checkNull(dti, "dti");

        myStyleTransformerGeometryProcessor = processor;
        myMGSClassToIdMap = new TObjectLongHashMap<>();
        myIdToMGSClassMap = new TLongObjectHashMap<>();
        myDataTypeInfo = dti;
        myToolbox = tb;
        myStyleLock = new ReentrantLock();
        myOverrideLock = new ReentrantReadWriteLock();
        myStyleSet = New.set();
        myMGSToStyleMap = New.map();
        MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry()
                .addVisualizationStyleRegistryChangeListener(this);
    }

    /**
     * Any style always requires full geometry rebuild.
     *
     * @return true, if successful
     */
    public boolean anyStyleAlwaysRequiresFullGeometryRebuild()
    {
        boolean requiresRebuild = false;
        myStyleLock.lock();
        try
        {
            for (FeatureVisualizationStyle style : myStyleSet)
            {
                if (style.allChangesRequireRebuild())
                {
                    requiresRebuild = true;
                    break;
                }
            }
        }
        finally
        {
            myStyleLock.unlock();
        }
        return requiresRebuild;
    }

    /**
     * Any style applies to all elements.
     *
     * @return true, if successful
     */
    public boolean anyStyleAppliesToAllElements()
    {
        boolean appliesToAllElements = false;
        myStyleLock.lock();
        try
        {
            for (FeatureVisualizationStyle style : myStyleSet)
            {
                if (style.getAppliesTo() == AppliesTo.ALL_ELEMENTS)
                {
                    appliesToAllElements = true;
                    break;
                }
            }
        }
        finally
        {
            myStyleLock.unlock();
        }
        return appliesToAllElements;
    }

    @Override
    public void defaultStyleChanged(Class<? extends VisualizationSupport> mgsClass,
            Class<? extends VisualizationStyle> styleClass, Object source)
    {
        // Ignore
    }

    /**
     * Gets the style id for style.
     *
     * @param geometry the geometry
     * @return the style id for style
     */
    public long getMGSTypeIdForMGS(VisualizationSupport geometry)
    {
        long typeId = -1;
        Class<? extends VisualizationSupport> mgsIfClass = ourMGSClassToFirstInterfaceMap.get(geometry.getClass());
        if (mgsIfClass != null)
        {
            typeId = myMGSClassToIdMap.get(mgsIfClass);
        }
        return typeId;
    }

    /**
     * Sets the style for the individual data element IDs, overriding the layer
     * style.
     *
     * @param elementIds the data element IDs
     * @param style the style
     */
    public void setOverrideStyle(Collection<Long> elementIds, FeatureVisualizationStyle style)
    {
        if (!myOverrideLock.writeLock().tryLock())
        {
            myOverrideLock.writeLock().lock();
        }

        try
        {
            for (Long id : elementIds)
            {
                myOverrideStyleMap.put(id.longValue(), style);
            }
        }
        finally
        {
            myOverrideLock.writeLock().unlock();
        }
    }

    /**
     * Removes the overridden style for the data element IDs.
     *
     * @param elementIds the data element IDs
     */
    public void removeOverrideStyle(Collection<Long> elementIds)
    {
        if (!myOverrideLock.writeLock().tryLock())
        {
            myOverrideLock.writeLock().lock();
        }

        try
        {
            for (Long id : elementIds)
            {
                myOverrideStyleMap.remove(id.longValue());
            }
        }
        finally
        {
            myOverrideLock.writeLock().unlock();
        }
    }

    /**
     * Gets the list of overridden data element IDs.
     *
     * @return the data element IDs.
     */
    public List<Long> getOverriddenIds()
    {
        myOverrideLock.readLock().lock();
        try
        {
            long[] keys = myOverrideStyleMap.keys();
            return CollectionUtilities.listView(Arrays.copyOf(keys, keys.length));
        }
        finally
        {
            myOverrideLock.readLock().unlock();
        }
    }

    /**
     * Gets the {@link FeatureVisualizationStyle} for a given
     * VisualizationSupport.
     *
     * First checks internal maps, then retrieves from the style registry and
     * caches the style in the manager.
     *
     * @param geometry the {@link VisualizationSupport} for which to find the
     *            style.
     * @param elementId the data element ID
     * @return the {@link FeatureVisualizationStyle} for the
     *         {@link VisualizationSupport}.
     */
    public FeatureVisualizationStyle getStyle(VisualizationSupport geometry, long elementId)
    {
        FeatureVisualizationStyle style = null;
        Class<? extends VisualizationSupport> mgsIfClass = ourMGSClassToFirstInterfaceMap.get(geometry.getClass());
        if (mgsIfClass == null)
        {
            mgsIfClass = VisualizationStyleUtilities.getFirstMGSInterface(geometry.getClass());
            if (mgsIfClass != null)
            {
                ourMGSClassToFirstInterfaceMap.put(geometry.getClass(), mgsIfClass);
            }
        }
        if (mgsIfClass != null)
        {
            style = getStyle(mgsIfClass, elementId);
        }
        return style;
    }

    /**
     * Gets the style by mgs type id.
     *
     * @param mgsTypeId the mgs type id
     * @param elementId the data element ID
     * @return the style
     */
    public FeatureVisualizationStyle getStyleByMGSTypeId(long mgsTypeId, long elementId)
    {
        Class<? extends VisualizationSupport> mgsIfClass = myIdToMGSClassMap.get(mgsTypeId);
        return mgsIfClass == null ? null : getStyle(mgsIfClass, elementId);
    }

    /**
     * Get if I have styles that depend on the selection state of the elements.
     *
     * @return {@code true} if any of my styles depend on selection state.
     */
    public boolean hasSelectionSensitiveStyle()
    {
        boolean selectionSensitive = false;
        myStyleLock.lock();
        try
        {
            for (FeatureVisualizationStyle style : myStyleSet)
            {
                if (style.isSelectionSensitiveStyle())
                {
                    selectionSensitive = true;
                    break;
                }
            }
        }
        finally
        {
            myStyleLock.unlock();
        }
        return selectionSensitive;
    }

    /**
     * Cleans up the style manager.
     */
    public void shutdown()
    {
        MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry()
                .removeVisualizationStyleRegistryChangeListener(this);
        myStyleLock.lock();
        try
        {
            for (FeatureVisualizationStyle style : myStyleSet)
            {
                style.removeStyleParameterChangeListener(this);
            }
            myMGSToStyleMap.clear();
            myStyleSet.clear();
        }
        finally
        {
            myStyleLock.unlock();
        }
    }

    @Override
    public void styleParametersChanged(VisualizationStyleParameterChangeEvent evt)
    {
        if (evt != null && evt.getStyle() instanceof FeatureVisualizationStyle)
        {
            notifyRebuild();
        }
    }

    /**
     * Styles require meta data.
     *
     * @return true, if successful
     */
    public boolean stylesRequireMetaData()
    {
        boolean requiresMetaData = false;
        myStyleLock.lock();
        try
        {
            for (FeatureVisualizationStyle style : myStyleSet)
            {
                if (style.requiresMetaData())
                {
                    requiresMetaData = true;
                    break;
                }
            }
        }
        finally
        {
            myStyleLock.unlock();
        }
        return requiresMetaData;
    }

    @Override
    public void visualizationStyleDatatypeChanged(VisualizationStyleDatatypeChangeEvent evt)
    {
        if ((evt.getDTIKey() == null || EqualsHelper.equals(myDataTypeInfo.getTypeKey(), evt.getDTIKey()))
                && evt.getNewStyle() instanceof FeatureVisualizationStyle)
        {
            myStyleLock.lock();
            try
            {
                if (!myMGSToStyleMap.isEmpty())
                {
                    Set<Class<? extends VisualizationSupport>> ifSet = New.set(myMGSToStyleMap.keySet());
                    for (Map.Entry<Class<? extends VisualizationSupport>, FeatureVisualizationStyle> entry : myMGSToStyleMap
                            .entrySet())
                    {
                        entry.getValue().removeStyleParameterChangeListener(this);
                    }
                    myMGSToStyleMap.clear();
                    myStyleSet.clear();

                    // Re-setup styles so that we are prepared for processing.
                    for (Class<? extends VisualizationSupport> mgsIf : ifSet)
                    {
                        final int useDefaultStyleFlag = -1;
                        getStyle(mgsIf, useDefaultStyleFlag);
                    }
                }
            }
            finally
            {
                myStyleLock.unlock();
            }
            notifyRebuild();
        }
    }

    @Override
    public void visualizationStyleInstalled(Class<? extends VisualizationStyle> styleClass, Object source)
    {
        // Don't care we will get visualizationStyleDatatypeChanged
    }

    /**
     * Gets the style given the base interface class of a map geometry support.
     *
     * @param mgsIfClass the mgs first interface class.
     * @param elementId the data element ID, or -1 to use the default style
     * @return the style
     */
    private FeatureVisualizationStyle getStyle(Class<? extends VisualizationSupport> mgsIfClass, long elementId)
    {
        FeatureVisualizationStyle style = null;

        if (elementId != -1)
        {
            myOverrideLock.readLock().lock();
            try
            {
                style = myOverrideStyleMap.get(elementId);
            }
            finally
            {
                myOverrideLock.readLock().unlock();
            }
        }

        if (style == null && mgsIfClass != null)
        {
            myStyleLock.lock();
            try
            {
                long mgsId = myMGSClassToIdMap.get(mgsIfClass);
                if (mgsId == 0)
                {
                    mgsId = myMGSIdCounter.incrementAndGet();
                    myMGSClassToIdMap.put(mgsIfClass, mgsId);
                    myIdToMGSClassMap.put(mgsId, mgsIfClass);
                }
                style = myMGSToStyleMap.get(mgsIfClass);
                if (style == null)
                {
                    VisualizationStyle vs = MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry()
                            .getStyle(mgsIfClass, myDataTypeInfo.getTypeKey(), true);
                    if (vs instanceof FeatureVisualizationStyle)
                    {
                        style = (FeatureVisualizationStyle)vs;
                        myMGSToStyleMap.put(mgsIfClass, style);
                        myStyleSet.clear();
                        myStyleSet.addAll(myMGSToStyleMap.values());
                        style.addStyleParameterChangeListener(this);
                    }
                    else
                    {
                        LOGGER.error("Failed to find style for MGS base interface " + mgsIfClass.getName());
                    }
                }
            }
            finally
            {
                myStyleLock.unlock();
            }
        }

        return style;
    }

    /**
     * Notify rebuild.
     */
    private void notifyRebuild()
    {
        myStyleTransformerGeometryProcessor.rebuildGeometriesFromStyleChange();
    }
}
