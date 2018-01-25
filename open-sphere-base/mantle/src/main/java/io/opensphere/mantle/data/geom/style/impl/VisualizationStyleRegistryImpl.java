package io.opensphere.mantle.data.geom.style.impl;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.RenderingCapabilities;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.style.DataTypeStyleConfiguration;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleDatatypeChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry;
import io.opensphere.mantle.data.geom.style.VisualizationStyleUtilities;

/**
 * The Class VisualizationStyeRegistryImpl.
 */
@SuppressWarnings("PMD.GodClass")
public class VisualizationStyleRegistryImpl implements VisualizationStyleRegistry
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(VisualizationStyleRegistryImpl.class);

    /** The ExecutorService. */
    private static final ExecutorService ourExecutor = Executors.newFixedThreadPool(1,
            new NamedThreadFactory("VisualizationStyleRegistry::Dispatch", 3, 4));

    /** The Change support. */
    private final WeakChangeSupport<VisualizationStyleRegistryChangeListener> myChangeSupport;

    /** The Default mgs class to vis class map. */
    private final Map<Class<? extends VisualizationSupport>, Class<? extends VisualizationStyle>> myDefaultMGSClassToVisClassMap;

    /** The Default style set. */
    private final Map<Class<? extends VisualizationStyle>, VisualizationStyle> myDefaultStyleMap;

    /** The Type to style map map. */
    private final Map<String, DataTypeStyleConfiguration> myDTIKeyToTypeConfigMap;

    /** The Style classes set. */
    private final Set<Class<? extends VisualizationStyle>> myStyleClassesSet;

    /** The Tile shaders supported. */
    private final boolean myTileShadersSupported;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new visualization style registry impl.
     *
     * @param tb the {@link Toolbox}
     */
    public VisualizationStyleRegistryImpl(Toolbox tb)
    {
        myToolbox = tb;
        myTileShadersSupported = myToolbox.getGeometryRegistry().getRenderingCapabilities()
                .isCapabilitySupported(RenderingCapabilities.TILE_SHADER);
        myStyleClassesSet = New.set();
        myDefaultStyleMap = New.map();
        myDefaultMGSClassToVisClassMap = New.map();
        myDTIKeyToTypeConfigMap = New.map();
        myChangeSupport = new WeakChangeSupport<>();
    }

    @Override
    public void addVisualizationStyleRegistryChangeListener(VisualizationStyleRegistryChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public VisualizationStyle getDefaultStyle(Class<? extends VisualizationSupport> mgsClass)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Searching for Default Style for MGS Class: " + mgsClass.getName());
        }
        Class<? extends VisualizationStyle> styleClass = null;
        VisualizationStyle style = null;
        synchronized (myDefaultMGSClassToVisClassMap)
        {
            styleClass = VisualizationStyleUtilities.searchForItemByMGSClass(myDefaultMGSClassToVisClassMap, mgsClass);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Found Class For MGS Class is: " + (styleClass == null ? "NULL" : styleClass.getName()));
            }
        }
        if (styleClass != null)
        {
            synchronized (myDefaultStyleMap)
            {
                style = myDefaultStyleMap.get(styleClass);
            }
        }

        return style;
    }

    @Override
    public VisualizationStyle getDefaultStyleInstanceForStyleClass(Class<? extends VisualizationStyle> styleClass)
    {
        VisualizationStyle styleInstance = null;
        synchronized (myDefaultStyleMap)
        {
            styleInstance = myDefaultStyleMap.get(styleClass);
        }
        return styleInstance;
    }

    @Override
    public Set<VisualizationStyle> getDefaultStyles()
    {
        Set<VisualizationStyle> resultSet = null;
        synchronized (myDefaultStyleMap)
        {
            resultSet = New.set(myDefaultStyleMap.values());
        }
        return resultSet.isEmpty() ? Collections.<VisualizationStyle>emptySet() : Collections.unmodifiableSet(resultSet);
    }

    @Override
    public Color getFeatureColorForActiveStyle(String dtiKey, Class<? extends VisualizationSupport> vsSupportType)
    {
        Color result = null;
        Set<Class<? extends VisualizationSupport>> ftSet = getFeatureTypes(dtiKey);

        if (CollectionUtilities.hasContent(ftSet))
        {
            for (Class<? extends VisualizationSupport> ft : ftSet)
            {
                if (vsSupportType == null)
                {
                    VisualizationStyle style = getStyle(ft, dtiKey, false);
                    if (style instanceof FeatureVisualizationStyle)
                    {
                        FeatureVisualizationStyle fvs = (FeatureVisualizationStyle)style;
                        result = fvs.getColor();
                        break;
                    }
                }
                else if (Utilities.sameInstance(vsSupportType, ft))
                {
                    VisualizationStyle style = getStyle(ft, dtiKey, false);
                    if (style instanceof AbstractFeatureVisualizationStyle)
                    {
                        FeatureVisualizationStyle fvs = (FeatureVisualizationStyle)style;
                        result = fvs.getColor();
                        break;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Set<Class<? extends VisualizationSupport>> getFeatureTypes(String dtiKey)
    {
        Set<Class<? extends VisualizationSupport>> styleSet = null;
        synchronized (myDTIKeyToTypeConfigMap)
        {
            DataTypeStyleConfiguration styleCfg = myDTIKeyToTypeConfigMap.get(dtiKey);
            if (styleCfg != null)
            {
                styleSet = styleCfg.getFeatureTypes();
            }
        }
        return styleSet;
    }

    @Override
    public VisualizationStyle getStyle(Class<? extends VisualizationSupport> mgsClass, String dtiKey,
            boolean returnDefaultIfNoSpecificStyle)
    {
        VisualizationStyle style = null;
        synchronized (myDTIKeyToTypeConfigMap)
        {
            DataTypeStyleConfiguration styleCfg = myDTIKeyToTypeConfigMap.get(dtiKey);
            if (styleCfg != null)
            {
                style = styleCfg.getStyle(mgsClass);
            }
        }
        if (style == null && returnDefaultIfNoSpecificStyle)
        {
            style = getDefaultStyle(mgsClass);
        }
        return style;
    }

    @Override
    public Set<VisualizationStyle> getStyles(String dtiKey)
    {
        Set<VisualizationStyle> styleSet = null;
        synchronized (myDTIKeyToTypeConfigMap)
        {
            DataTypeStyleConfiguration styleCfg = myDTIKeyToTypeConfigMap.get(dtiKey);
            if (styleCfg != null)
            {
                styleSet = styleCfg.getStyles();
            }
        }
        return styleSet;
    }

    @Override
    public Set<Class<? extends VisualizationStyle>> getStylesForStyleType(Class<? extends VisualizationStyle> styleClass)
    {
        Utilities.checkNull(styleClass, "styleClass");
        Set<Class<? extends VisualizationStyle>> result = New.set();
        synchronized (myStyleClassesSet)
        {
            for (Class<? extends VisualizationStyle> cl : myStyleClassesSet)
            {
                if (styleClass.isAssignableFrom(cl))
                {
                    result.add(cl);
                }
            }
        }
        return result.isEmpty() ? Collections.<Class<? extends VisualizationStyle>>emptySet()
                : Collections.unmodifiableSet(result);
    }

    @Override
    public boolean installStyle(Class<? extends VisualizationStyle> styleClass, Object source)
    {
        boolean added = false;
        Utilities.checkNull(styleClass, "styleClass");

        synchronized (myStyleClassesSet)
        {
            if (!myStyleClassesSet.contains(styleClass))
            {
                myStyleClassesSet.add(styleClass);
                try
                {
                    try
                    {
                        Constructor<? extends VisualizationStyle> ctor = styleClass.getConstructor(Toolbox.class);
                        if (ctor != null)
                        {
                            VisualizationStyle vs = ctor.newInstance(myToolbox);
                            if (!vs.requiresShaders() || vs.requiresShaders() && myTileShadersSupported)
                            {
                                myDefaultStyleMap.put(styleClass, vs);
                                added = true;
                            }
                            else
                            {
                                LOGGER.info("Visualization Style " + vs.getClass().getName()
                                        + " was not installed because it requires shaders and shaders are not supported.");
                            }
                        }
                    }
                    catch (NoSuchMethodException e)
                    {
                        LOGGER.error("VisualizationStyle class " + styleClass.getName()
                                + " Does not have a constructor that takes the Toolbox, trying no-arg constructor.");
                        VisualizationStyle vs = styleClass.newInstance();
                        if (!vs.requiresShaders() || vs.requiresShaders() && myTileShadersSupported)
                        {
                            myDefaultStyleMap.put(styleClass, vs);
                            added = true;
                        }
                        else
                        {
                            LOGGER.info("Visualization Style " + vs.getClass().getName()
                                    + " was not installed because it requires shaders and shaders are not supported.");
                        }
                    }
                }
                catch (IllegalArgumentException | SecurityException | InstantiationException | IllegalAccessException
                        | InvocationTargetException e)
                {
                    LOGGER.error(e);
                }
            }
        }
        if (added)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Added VisualizationStyle Class " + styleClass.getName());
            }
            VisualizationStyle style = myDefaultStyleMap.get(styleClass);
            if (style != null)
            {
                style.initialize();
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(style.toString());
                }
            }
            fireVisualizationStyleInstalled(styleClass, source);
        }
        return added;
    }

    @Override
    public void removeVisualizationStyleRegistryChangeListener(VisualizationStyleRegistryChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public void resetStyle(Class<? extends VisualizationSupport> mgsClass, String dtiKey, Object source)
    {
        VisualizationStyle resetStyle = null;
        synchronized (myDTIKeyToTypeConfigMap)
        {
            DataTypeStyleConfiguration styleCfg = myDTIKeyToTypeConfigMap.get(dtiKey);
            if (styleCfg != null)
            {
                resetStyle = styleCfg.removeStyle(mgsClass);
            }
        }
        if (resetStyle != null)
        {
            fireStyleDataTypeChangeEventListener(new VisualizationStyleDatatypeChangeEvent(dtiKey, mgsClass, resetStyle,
                    getDefaultStyle(mgsClass), true, source));
        }
    }

    @Override
    public void setDefaultStyle(Class<? extends VisualizationSupport> mgsClass, Class<? extends VisualizationStyle> styleClass,
            Object source)
    {
        VisualizationStyle oldStyle = getDefaultStyle(mgsClass);
        synchronized (myStyleClassesSet)
        {
            if (!myStyleClassesSet.contains(styleClass))
            {
                installStyle(styleClass, source);
            }
        }
        VisualizationStyle newStyle = null;
        synchronized (myDefaultStyleMap)
        {
            newStyle = myDefaultStyleMap.get(styleClass);
        }

        synchronized (myDefaultMGSClassToVisClassMap)
        {
            myDefaultMGSClassToVisClassMap.put(mgsClass, styleClass);
        }
        fireDefaultStyleChanged(mgsClass, styleClass, source);
        fireStyleDataTypeChangeEventListener(
                new VisualizationStyleDatatypeChangeEvent(null, mgsClass, oldStyle, newStyle, true, source));
    }

    @Override
    public VisualizationStyle setStyle(Class<? extends VisualizationSupport> mgsClass, String dtiKey, VisualizationStyle style,
            Object source)
    {
        VisualizationStyle oldStyle = null;
        synchronized (myDTIKeyToTypeConfigMap)
        {
            DataTypeStyleConfiguration styleCfg = myDTIKeyToTypeConfigMap.get(dtiKey);
            if (styleCfg == null)
            {
                styleCfg = new DataTypeStyleConfigurationImpl(dtiKey);
                myDTIKeyToTypeConfigMap.put(dtiKey, styleCfg);
            }
            oldStyle = styleCfg.getStyle(mgsClass);
            styleCfg.setStyle(mgsClass, style);
        }
        if (oldStyle == null)
        {
            oldStyle = getDefaultStyle(mgsClass);
        }
        fireStyleDataTypeChangeEventListener(
                new VisualizationStyleDatatypeChangeEvent(dtiKey, mgsClass, oldStyle, style, false, source));
        return oldStyle;
    }

    /**
     * Fire default style changed.
     *
     * @param mgsClass the mgs class
     * @param styleClass the style class
     * @param source the source
     */
    private void fireDefaultStyleChanged(final Class<? extends VisualizationSupport> mgsClass,
            final Class<? extends VisualizationStyle> styleClass, final Object source)
    {
        myChangeSupport.notifyListeners(new Callback<VisualizationStyleRegistry.VisualizationStyleRegistryChangeListener>()
        {
            @Override
            public void notify(VisualizationStyleRegistryChangeListener listener)
            {
                listener.defaultStyleChanged(mgsClass, styleClass, source);
            }
        }, ourExecutor);
    }

    /**
     * Fire style data type change event listener.
     *
     * @param event the event
     */
    private void fireStyleDataTypeChangeEventListener(final VisualizationStyleDatatypeChangeEvent event)
    {
        myChangeSupport.notifyListeners(new Callback<VisualizationStyleRegistry.VisualizationStyleRegistryChangeListener>()
        {
            @Override
            public void notify(VisualizationStyleRegistryChangeListener listener)
            {
                listener.visualizationStyleDatatypeChanged(event);
            }
        }, ourExecutor);
    }

    /**
     * Fire visualization style installed.
     *
     * @param styleClass the style class
     * @param source the source
     */
    private void fireVisualizationStyleInstalled(final Class<? extends VisualizationStyle> styleClass, final Object source)
    {
        myChangeSupport.notifyListeners(new Callback<VisualizationStyleRegistry.VisualizationStyleRegistryChangeListener>()
        {
            @Override
            public void notify(VisualizationStyleRegistryChangeListener listener)
            {
                listener.visualizationStyleInstalled(styleClass, source);
            }
        }, ourExecutor);
    }
}
