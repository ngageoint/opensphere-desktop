package io.opensphere.mantle.data.geom.style.dialog;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleController;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry;
import io.opensphere.mantle.data.geom.style.config.v1.DataTypeStyleConfig;
import io.opensphere.mantle.data.geom.style.config.v1.FeatureTypeStyleConfig;
import io.opensphere.mantle.data.geom.style.config.v1.StyleManagerConfig;
import io.opensphere.mantle.data.geom.style.config.v1.StyleParameterConfig;
import io.opensphere.mantle.data.geom.style.config.v1.StyleParameterSetConfig;
import io.opensphere.mantle.data.geom.style.dialog.DataTypeNodeUserObject.NodeKeyComponent;
import io.opensphere.mantle.data.geom.style.dialog.DataTypeNodeUserObject.NodeType;
import io.opensphere.mantle.data.geom.style.impl.AbstractFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.StyleUtils;

/**
 * The Class StyleManagerController.
 */
@SuppressWarnings("PMD.GodClass")
public class StyleManagerController implements VisualizationStyleController
{
    /** The Constant FEATURE_CLASS. */
    private static final String FEATURE_CLASS = "featureClass";

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(StyleManagerController.class);

    /** Key for the preferences. */
    private static final String PREFERENCE_KEY = "styleManagerConfig";

    /**
     * Features by name.
     */
    private static Map<String, Class<? extends VisualizationStyle>> featuresByName = new TreeMap<>();
    static
    {
        for (Class<? extends VisualizationStyle> c : StyleUtils.FEATURE_STYLES)
        {
            featuresByName.put(c.getName(), c);
        }
    }

    /** The Config. */
    private final StyleManagerConfig myConfig;

    /** The Config read write lock. */
    private final ReentrantLock myConfigLock;

    /** The preferences. */
    private final Preferences myPrefs;

    /** The Style registry. */
    private final VisualizationStyleRegistry myStyleRegistry;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /** The Types using custom. */
    private final Set<String> myTypesUsingCustom = new TreeSet<>();

    /** Alias with fewer than 60 characters. */
    private static final String SHOW_LBL_KEY = AbstractFeatureVisualizationStyle.LABEL_ENABLED_PROPERTY_KEY;

    /** Alias with fewer than 58 characters. */
    private static final String LBL_COLOR_KEY = AbstractFeatureVisualizationStyle.LABEL_COLOR_PROPERTY_KEY;

    /** Alias with fewer than 63 characters. */
    private static final String LBL_COLUMN_KEY = AbstractFeatureVisualizationStyle.LABEL_COLUMN_KEY_PROPERTY_KEY;

    /** Alias with fewer than 57 characters. */
    private static final String LBL_SIZE_KEY = AbstractFeatureVisualizationStyle.LABEL_SIZE_PROPERTY_KEY;

    /** Collection of label keys. */
    private static final Set<String> COMMON_KEYS = new TreeSet<>();
    static
    {
        COMMON_KEYS.add(SHOW_LBL_KEY);
        COMMON_KEYS.add(LBL_COLOR_KEY);
        COMMON_KEYS.add(LBL_COLUMN_KEY);
        COMMON_KEYS.add(LBL_SIZE_KEY);
    }

    /**
     * Instantiates a new style manager controller.
     *
     * @param tb the {@link Toolbox}
     * @param visStyleReg the {@link VisualizationStyleRegistry}
     */
    public StyleManagerController(Toolbox tb, VisualizationStyleRegistry visStyleReg)
    {
        myToolbox = tb;
        myConfigLock = new ReentrantLock();
        PreferencesRegistry preferencesRegistry = tb.getPreferencesRegistry();
        myPrefs = preferencesRegistry.getPreferences(StyleManagerController.class);
        StyleManagerConfig config = myPrefs.getJAXBObject(StyleManagerConfig.class, PREFERENCE_KEY, null);
        if (config == null)
        {
            config = new StyleManagerConfigMigrationHelper().loadFromDeprecatedTopic(preferencesRegistry, PREFERENCE_KEY);
        }
        myConfig = config;

        myStyleRegistry = visStyleReg;
    }

    @Override
    public Class<? extends VisualizationStyle> getSelectedVisualizationStyleClass(
            Class<? extends VisualizationSupport> featureClass, DataGroupInfo dgi, DataTypeInfo dti)
    {
        return getSelectedVisualizationStyleClass(featureClass, createDTNKey(dgi, dti));
    }

    /**
     * Checks the configuration for the dtnKey and feature class and tries to
     * get the selected class name, if not found ( i.e. not in config ). Then
     * tries to get the selected type from the registry, data type specific if
     * the type is specific, or default if not.
     *
     * @param featureClass the {@link MapGeometrySupport} feature class
     * @param dtnKey the data type node key. See {@link DataTypeNodeUserObject}.
     * @return the VisualizationStyle class or null if not found.
     */
    public Class<? extends VisualizationStyle> getSelectedVisualizationStyleClass(
            Class<? extends VisualizationSupport> featureClass, String dtnKey)
    {
        String selName = calcCfgLock(() ->
        {
            FeatureTypeStyleConfig ftsc = myConfig.getFeatureTypeStyle(dtnKey, featureClass.getName());
            if (ftsc != null)
            {
                return ftsc.getSelectedStyleClassName();
            }
            return null;
        });
        if (selName == null || selName.isEmpty())
        {
            return null;
        }

        Class<? extends VisualizationStyle> selClass = StyleManagerUtils.getClassForNameAndType(VisualizationStyle.class,
                selName);
        if (selClass == null)
        {
            return null;
        }

        Map<NodeKeyComponent, String> nodeKeyCompMap = DataTypeNodeUserObject.decomposeNodeKey(dtnKey);
        NodeType nt = NodeType.valueOf(nodeKeyCompMap.get(NodeKeyComponent.NODE_TYPE));
        String dtiKey = nodeKeyCompMap.get(NodeKeyComponent.DATA_TYPE_INFO_ID);

        VisualizationStyle vs = null;
        if (nt.isLeafType())
        {
            vs = myStyleRegistry.getStyle(featureClass, dtiKey, true);
        }
        else if (nt.isDefaultType())
        {
            vs = myStyleRegistry.getDefaultStyle(featureClass);
        }

        if (vs != null)
        {
            selName = vs.getClass().getName();
        }
        if (selName != null && !selName.isEmpty())
        {
            selClass = StyleManagerUtils.getClassForNameAndType(VisualizationStyle.class, selName);
        }

        return selClass;
    }

    @Override
    public VisualizationStyle getStyleForEditorWithConfigValues(Class<? extends VisualizationStyle> vsClass,
            Class<? extends VisualizationSupport> featureClass, DataGroupInfo dgi, DataTypeInfo dti)
    {
        return getStyleForEditorWithConfigValues(vsClass, featureClass, createDTNKey(dgi, dti));
    }

    /**
     * Installs visualization styles.
     *
     * @param cl The style class.
     * @param vs The style.
     * @param src The installer.
     * @return The installed style.
     */
    private VisualizationStyle maybeInstall(Class<? extends VisualizationStyle> cl, VisualizationStyle vs, Object src)
    {
        if (vs != null)
        {
            return vs;
        }
        if (myStyleRegistry.installStyle(cl, src))
        {
            return myStyleRegistry.getDefaultStyleInstanceForStyleClass(cl);
        }
        LOGGER.error("Failed to install style class : " + cl.getName());
        return null;
    }

    /**
     * Gets the style for editor with config values from the configuration if it
     * is not currently the style installed and activated for the data type in
     * the registry.
     *
     * First tries to pull the style for the dti key and feature class from the
     * registry, if not retrieves the default instance from the registry and
     * derives a new style instance for the data type. If the a derived version
     * is used from the default, the derived version is updated with the config
     * values
     *
     * Note: Always returns a new style instance, not the same object that is in
     * the registry.
     *
     * @param vsClass the {@link VisualizationStyle} class
     * @param featureClass the {@link MapGeometrySupport} feature class
     * @param dtnKey the data type node key. See {@link DataTypeNodeUserObject}.
     * @return the style for editor with config values
     */
    public VisualizationStyle getStyleForEditorWithConfigValues(Class<? extends VisualizationStyle> vsClass,
            Class<? extends VisualizationSupport> featureClass, String dtnKey)
    {
        Utilities.checkNull(vsClass, "vsClass");
        Utilities.checkNull(featureClass, FEATURE_CLASS);
        Utilities.checkNull(dtnKey, "dtnKey");

        // Retrieve the default instance of the style being updated, if we don't
        // find it try to install the style in the registry so it can be used.
        // If that fails bail out and log an error.
        VisualizationStyle defaultStyle = maybeInstall(vsClass, myStyleRegistry.getDefaultStyleInstanceForStyleClass(vsClass),
                this);
        if (defaultStyle == null)
        {
            return null;
        }

        Map<NodeKeyComponent, String> keyParts = DataTypeNodeUserObject.decomposeNodeKey(dtnKey);
        NodeType nodeType = NodeType.valueOf(keyParts.get(NodeKeyComponent.NODE_TYPE));

        // If we have a default type node, grab the default style instance and
        // just make a copy for the editor to use.
        if (nodeType.isDefaultType())
        {
            return defaultStyle.clone();
        }
        if (nodeType.isCollectionRoot())
        {
            // collection roots don't do anything, we should never get here.
            LOGGER.error("Attempt to get style for a collection root, unallowed.");
            return null;
        }
        // Leaf type. Try to retrieve the current style for the data type
        // and feature class. If the style is null ( i.e. none installed )
        // or it is a different type of style class, then we need to
        // generate a new style based on the default style but updated with
        // any of our customized configuration parameters.

        // Otherwise return a copy of the version in the registry as we are
        // about to edit it.

        String dtiKey = keyParts.get(NodeKeyComponent.DATA_TYPE_INFO_ID);
        if (dtiKey == null || dtiKey.isEmpty())
        {
            return null;
        }
        VisualizationStyle style = myStyleRegistry.getStyle(featureClass, dtiKey, false);
        if (style != null && style.getClass() == vsClass)
        {
            return style.clone();
        }

        VisualizationStyle vst = defaultStyle.deriveForType(dtiKey);
        if (vst == null)
        {
            return null;
        }
        doCfgLock(() ->
        {
            StyleParameterSetConfig spsc = myConfig.getStyleParameterSet(dtnKey, featureClass.getName(), vsClass.getName());
            if (spsc != null)
            {
                StyleManagerUtils.updateStyleWithParametersFromConfig(vst, spsc, this);
            }
        });

        return vst;
    }

    /**
     * Initialize registry from config.
     */
    public void initializeRegistryFromConfig()
    {
        doCfgLock(() ->
        {
            if (myConfig == null)
            {
                return;
            }
            fixDuplicateEntries();
            initializeDefaultStylesInRegistry();
            initializeCustomTypes();
        });
    }

    /**
     * Fixes duplicate entries in the config.
     */
    private void fixDuplicateEntries()
    {
        boolean updated = false;

        // Get the duplicate data type keys
        List<String> duplicateTypeKeys = myConfig.getDataTypeStyles().stream()
                .collect(Collectors.groupingBy(StyleManagerController::getTypeKey, Collectors.counting())).entrySet().stream()
                .filter(e -> e.getValue().longValue() > 1).map(e -> e.getKey()).collect(Collectors.toList());
        for (String typeKey : duplicateTypeKeys)
        {
            // Fix duplicate entries by removing all but the last entry
            List<String> matchingNodeKeys = myConfig.getDataTypeStyles().stream().filter(s -> typeKey.equals(getTypeKey(s)))
                    .map(s -> s.getDataTypeKey()).collect(Collectors.toList());
            if (matchingNodeKeys.size() > 1)
            {
                matchingNodeKeys.remove(matchingNodeKeys.size() - 1);

                for (String nodeKey : matchingNodeKeys)
                {
                    LOGGER.info("Removing duplicate style key: " + nodeKey);
                    myConfig.removeDataTypeStyle(nodeKey);
                    myConfig.removeUseCustomTypeKey(nodeKey);
                }
                updated = true;
            }
        }

        if (updated)
        {
            saveConfigState();
        }
    }

    /**
     * Gets the type key from the config.
     *
     * @param config the config
     * @return the type key
     */
    private static String getTypeKey(DataTypeStyleConfig config)
    {
        return DataTypeNodeUserObject.decomposeNodeKey(config.getDataTypeKey()).get(NodeKeyComponent.DATA_TYPE_INFO_ID);
    }

    @Override
    public boolean isTypeUsingCustom(DataGroupInfo dgi, DataTypeInfo dti)
    {
        return isTypeUsingCustom(createDTNKey(dgi, dti));
    }

    /**
     * Checks if is type using custom.
     *
     * @param dtnKey the data type node key. See {@link DataTypeNodeUserObject}.
     * @return true, if is type using custom
     */
    public boolean isTypeUsingCustom(String dtnKey)
    {
        boolean isUsing = false;
        synchronized (myTypesUsingCustom)
        {
            isUsing = myTypesUsingCustom.contains(dtnKey);
        }
        return isUsing;
    }

    @Override
    public void removeStyle(DataGroupInfo dgi, DataTypeInfo dti)
    {
        String key = createDTNKey(dgi, dti);
        doCfgLock(() ->
        {
            myConfig.removeDataTypeStyle(key);
            myConfig.removeUseCustomTypeKey(key);
            saveConfigState();
        });
    }

    @Override
    public void resetAllStyleSettings(Object source)
    {
        Set<String> typesToReset = null;
        synchronized (myTypesUsingCustom)
        {
            typesToReset = new TreeSet<>(myTypesUsingCustom);
        }
        if (!typesToReset.isEmpty())
        {
            for (String type : typesToReset)
            {
                setUseCustomStyleForDataType(type, false, source);
            }
            doCfgLock(() -> myConfig.clear());
            saveConfigState();
        }
    }

    @Override
    public void resetStyle(VisualizationStyle aStyle, Class<? extends MapGeometrySupport> featureClass, DataGroupInfo dgi,
            DataTypeInfo dti)
    {
        resetStyle(aStyle, featureClass, createDTNKey(dgi, dti));
    }

    /**
     * Reset style for a style/feature/dtnKey combo in the config and if in use,
     * in the registry.
     *
     * @param aStyle the {@link VisualizationStyle} style to be reset .
     * @param featureClass the {@link MapGeometrySupport} feature class
     * @param dtnKey the data type node key. See {@link DataTypeNodeUserObject}.
     */
    public void resetStyle(VisualizationStyle aStyle, Class<? extends MapGeometrySupport> featureClass, String dtnKey)
    {
        Utilities.checkNull(aStyle, "aStyle");
        Utilities.checkNull(featureClass, FEATURE_CLASS);
        Utilities.checkNull(dtnKey, "dtiKey");

        // First update with the no change parameter set for the reset.
        Set<VisualizationStyleParameter> changedParams = new HashSet<>();
        StyleParameterSetConfig spsc = new StyleParameterSetConfig(aStyle.getClass().getName(), changedParams);

        doCfgLock(() -> myConfig.setOrUpdateStyleParameterSet(spsc, featureClass.getName(), dtnKey));
        saveConfigState();

        // Now determine if we need to update the registry.
        Map<NodeKeyComponent, String> keyParts = DataTypeNodeUserObject.decomposeNodeKey(dtnKey);
        String dtiKey = keyParts.get(NodeKeyComponent.DATA_TYPE_INFO_ID);
        NodeType nodeType = NodeType.valueOf(keyParts.get(NodeKeyComponent.NODE_TYPE));
        if (nodeType.isDefaultType() || isTypeUsingCustom(dtnKey))
        {
            // Create a new unaltered instance of the style type.
            VisualizationStyle dStyle = StyleManagerUtils.getNewInstance(myToolbox, aStyle.getClass());
            dStyle.initialize();

            // Retrieve the instance from the registry to be reset.
            VisualizationStyle regStyle = null;
            if (nodeType.isDefaultType())
            {
                regStyle = myStyleRegistry.getDefaultStyle(featureClass);
            }
            else if (nodeType.isLeafType())
            {
                regStyle = myStyleRegistry.getStyle(featureClass, dtiKey, false);

                // If we had the specific data type style, then we need to take
                // our clean copy and make a new version initialized for our
                // data type.
                if (regStyle != null)
                {
                    dStyle = dStyle.deriveForType(dtiKey);
                }
            }

            // If we retrieved the registry style, now re-initialize it with the
            // clean parameter set.
            if (regStyle != null)
            {
                regStyle.setParameters(dStyle.getStyleParameterSet(), this);
            }
        }
    }

    @Override
    public void setSelectedStyleClass(VisualizationStyle aStyle, Class<? extends VisualizationSupport> featureClass,
            DataGroupInfo dgi, DataTypeInfo dti, Object source)
    {
        setSelectedStyleClass(aStyle, featureClass, createDTNKey(dgi, dti), source);
    }

    /**
     * Sets the selected style class for a data type.
     *
     * First alters the configuration with the selected style class, and also
     * preserves the parameter set from the passed in style.
     *
     * Then if the style is for a specific data type or a default type and the
     * registry needs to be updated updates the registry.
     *
     * @param aStyle the {@link VisualizationStyle} to switch to as current.
     * @param featureClass the {@link MapGeometrySupport} feature class to apply
     *            to
     * @param dtnKey the data type node key. See {@link DataTypeNodeUserObject}.
     * @param source the source
     */
    public void setSelectedStyleClass(VisualizationStyle aStyle, Class<? extends VisualizationSupport> featureClass,
            String dtnKey, Object source)
    {
        Utilities.checkNull(aStyle, "aStyle");
        Utilities.checkNull(featureClass, FEATURE_CLASS);
        Utilities.checkNull(dtnKey, "dtiKey");

        VisualizationStyle dStyle = StyleManagerUtils.getNewInstance(myToolbox, aStyle.getClass());
        dStyle.initialize();
        Set<VisualizationStyleParameter> changedParams = aStyle.getChangedParameters(dStyle);
        Set<VisualizationStyleParameter> alwaysSaveParams = aStyle.getAlwaysSaveParameters();
        if (alwaysSaveParams != null && !alwaysSaveParams.isEmpty())
        {
            changedParams = new HashSet<>(changedParams);
            changedParams.addAll(alwaysSaveParams);
        }

        StyleParameterSetConfig paramUpdate = new StyleParameterSetConfig(aStyle.getClass().getName(), changedParams);

        // Update the config style parameters and selected style class.
        doCfgLock(() ->
        {
            myConfig.setOrUpdateStyleParameterSet(paramUpdate, featureClass.getName(), dtnKey);
            myConfig.setSelectedStyleClass(dtnKey, featureClass.getName(), aStyle.getClass().getName());
        });
        saveConfigState();

        Map<NodeKeyComponent, String> keyParts = DataTypeNodeUserObject.decomposeNodeKey(dtnKey);
        String dtiKey = keyParts.get(NodeKeyComponent.DATA_TYPE_INFO_ID);
        NodeType nodeType = NodeType.valueOf(keyParts.get(NodeKeyComponent.NODE_TYPE));

        // If this type is using custom, then we need to update the registry
        // with the new values. If the type has changed switch to the new type,
        // otherwise update the parameters if needed.
        if (nodeType.isDefaultType())
        {
            // For default types, first see if we have a style for that
            // class installed, if not, then install the style and update it
            // with our style parameters. If we are already using that style
            // class, then just update it with any changed parameters.
            VisualizationStyle vs = myStyleRegistry.getDefaultStyle(featureClass);
            if (vs == null || vs.getClass() != aStyle.getClass())
            {
                myStyleRegistry.setDefaultStyle(featureClass, aStyle.getClass(), source);
                vs = myStyleRegistry.getDefaultStyle(featureClass);
            }
            if (vs != null)
            {
                vs.setParameters(aStyle.getStyleParameterSet(), source);
            }
        }
        else if (isTypeUsingCustom(dtnKey) && nodeType.isLeafType())
        {
            // For specific data types, see if we already have a style for
            // the feature class if not, then just install a copy of our
            // style. If we do and it is our same style class then just
            // update the parameters from our selected style if necessary.
            VisualizationStyle vs = myStyleRegistry.getStyle(featureClass, dtiKey, false);
            if (vs == null || vs.getClass() != aStyle.getClass())
            {
                myStyleRegistry.setStyle(featureClass, dtiKey, aStyle.clone(), source);
            }
            else
            {
                vs.setParameters(aStyle.getStyleParameterSet(), source);
            }
        }
    }

    @Override
    public void setUseCustomStyleForDataType(DataGroupInfo dgi, DataTypeInfo dti, boolean useCustom, Object source)
    {
        setUseCustomStyleForDataType(createDTNKey(dgi, dti), useCustom, source);
    }

    /**
     * Updates the configuration to reflect that a data type is to use custom or
     * default, then either deactivates the custom types in the registry, or
     * installs and activates the custom types for a data type in the registry.
     *
     * @param dtnKey the data type node key. See {@link DataTypeNodeUserObject}.
     * @param useCustom the use custom style, false to revert to the default
     *            style.
     * @param source the source
     */
    public void setUseCustomStyleForDataType(String dtnKey, boolean useCustom, Object source)
    {
        Utilities.checkNull(dtnKey, "dtnKey");
        Map<NodeKeyComponent, String> keyParts = DataTypeNodeUserObject.decomposeNodeKey(dtnKey);
        String dtiKey = keyParts.get(NodeKeyComponent.DATA_TYPE_INFO_ID);
        NodeType nodeType = NodeType.valueOf(keyParts.get(NodeKeyComponent.NODE_TYPE));
        synchronized (myTypesUsingCustom)
        {
            if (!nodeType.isLeafType())
            {
                return;
            }
            if (!useCustom)
            {
                // If we are turning off the custom types, update the config,
                // and then reset the styles for all overridden feature types.
                if (myTypesUsingCustom.remove(dtnKey))
                {
                    doCfgLock(() -> myConfig.removeUseCustomTypeKey(dtnKey));
                    saveConfigState();
                }
                Set<Class<? extends VisualizationSupport>> featureTypes = myStyleRegistry.getFeatureTypes(dtiKey);
                if (featureTypes != null)
                {
                    for (Class<? extends VisualizationSupport> ft : featureTypes)
                    {
                        myStyleRegistry.resetStyle(ft, dtiKey, source);
                    }
                }
            }
            else
            {
                if (myTypesUsingCustom.add(dtnKey))
                {
                    doCfgLock(() -> myConfig.addUseCustomTypeKey(dtnKey));
                    saveConfigState();
                }
                if (!dtnKey.isEmpty())
                {
                    doCfgLock(() -> activateCustomTypesForDataType(dtnKey, true, source));
                }
            }
        }
    }

    @Override
    public void updateStyle(VisualizationStyle aStyle, Class<? extends VisualizationSupport> featureClass, DataGroupInfo dgi,
            DataTypeInfo dti, Object source)
    {
        updateStyleFromEditor(aStyle, featureClass, createDTNKey(dgi, dti), source);
    }

    /**
     * Perform the specified task while the config lock is locked. Use of this
     * method normally saves eight lines of obstructive boilerplate.
     *
     * @param r a Runnable
     */
    private void doCfgLock(Runnable r)
    {
        myConfigLock.lock();
        try
        {
            r.run();
        }
        finally
        {
            myConfigLock.unlock();
        }
    }

    /**
     * Same as doCfgLock except that it returns a value.
     *
     * @param s a Supplier
     * @param<T> the return type
     * @return something
     */
    private <T> T calcCfgLock(Supplier<T> s)
    {
        myConfigLock.lock();
        try
        {
            return s.get();
        }
        finally
        {
            myConfigLock.unlock();
        }
    }

    /**
     * Create a new VisualizationStyle for the given class and layer, if
     * possible. If the specified class cannot be registered with the
     * VisualizationStyleRegistry, then this method returns null.
     *
     * @param cl the class of the desired instance
     * @param typeKey the layer identifier (used to populate defaults)
     * @param src source of the call
     * @return a newly minted VisualizationStyle or null
     */
    private VisualizationStyle getVisStyle(Class<? extends VisualizationStyle> cl, String typeKey, Object src)
    {
        myStyleRegistry.installStyle(cl, src);
        VisualizationStyle vs = myStyleRegistry.getDefaultStyleInstanceForStyleClass(cl);
        if (vs == null)
        {
            return null;
        }
        vs = vs.newInstance(vs.getToolbox());
        vs.setDTIKey(typeKey);
        vs.initializeFromDataType();
        return vs;
    }

    /**
     * Updates the style in the config to reflect changes from the editor, if
     * active updates/replaces the style in the registry.
     *
     * @param aStyle the {@link VisualizationStyle} class that is being updated.
     * @param featureClass the {@link MapGeometrySupport} feature class.
     * @param dtnKey the data type node key. See {@link DataTypeNodeUserObject}.
     * @param source the source requesting the change
     */
    public void updateStyleFromEditor(VisualizationStyle aStyle, Class<? extends VisualizationSupport> featureClass,
            String dtnKey, Object source)
    {
        Utilities.checkNull(aStyle, "aStyle");
        Utilities.checkNull(featureClass, FEATURE_CLASS);
        Utilities.checkNull(dtnKey, "dtiKey");

        VisualizationStyle dStyle = StyleManagerUtils.getNewInstance(myToolbox, aStyle.getClass());
        dStyle.initialize();
        Set<VisualizationStyleParameter> changedParams = aStyle.getChangedParameters(dStyle);
        Set<VisualizationStyleParameter> alwaysSaveParams = aStyle.getAlwaysSaveParameters();
        if (alwaysSaveParams != null && !alwaysSaveParams.isEmpty())
        {
            changedParams = new HashSet<>(changedParams);
            changedParams.addAll(alwaysSaveParams);
        }

        // collect the common configuration parameters
        Map<String, VisualizationStyleParameter> vspMap = new TreeMap<>();
        for (VisualizationStyleParameter p : changedParams)
        {
            if (COMMON_KEYS.contains(p.getKey()))
            {
                vspMap.put(p.getKey(), p);
            }
        }

        StyleParameterSetConfig spsc = new StyleParameterSetConfig(aStyle.getClass().getName(), changedParams);
        myConfigLock.lock();
        try
        {
            // reflect the editor changes in the StyleManagerConfig
            myConfig.setOrUpdateStyleParameterSet(spsc, featureClass.getName(), dtnKey);

            // also replicate common params for other geometry types
            FeatureTypeStyleConfig ftsc = myConfig.getFeatureTypeStyle(dtnKey, featureClass.getName());
            // create unconfigured geometry types
            String typeKey = aStyle.getDTIKey();
            Set<String> needed = new TreeSet<>(featuresByName.keySet());
            for (StyleParameterSetConfig c : ftsc.getStyleParameterSetConfigList())
            {
                needed.remove(c.getStyleClassName());
            }
            for (String n : needed)
            {
                ftsc.getStyleParameterSetConfigList()
                        .add(new StyleParameterSetConfig(getVisStyle(featuresByName.get(n), typeKey, source)));
            }

            // now perform replication
            for (StyleParameterSetConfig c : ftsc.getStyleParameterSetConfigList())
            {
                // skip the one we already updated
                if (c.getStyleClassName().equals(spsc.getStyleClassName()))
                {
                    continue;
                }
                // remove pre-existing parameters
                c.getParameterSet().removeIf(x -> vspMap.containsKey(x.getParameterKey()));
                // introduce the new settings
                vspMap.values().stream().forEach(x -> c.getParameterSet().add(new StyleParameterConfig(x)));
            }
        }
        finally
        {
            myConfigLock.unlock();
        }
        saveConfigState();

        Map<NodeKeyComponent, String> keyParts = DataTypeNodeUserObject.decomposeNodeKey(dtnKey);
        String dtiKey = keyParts.get(NodeKeyComponent.DATA_TYPE_INFO_ID);
        NodeType nodeType = NodeType.valueOf(keyParts.get(NodeKeyComponent.NODE_TYPE));
        if (nodeType.isDefaultType())
        {
            // If this is a default type node, then we need to get the
            // default style instance from the registry. If we can't find it
            // try to create and install it.
            VisualizationStyle regStyle = myStyleRegistry.getDefaultStyle(featureClass);
            boolean setToDefaultForFeatureType = false;
            if (regStyle == null)
            {
                LOGGER.error("Did not find default style for the feature class " + featureClass.getName() + " in the registry.");
                myStyleRegistry.installStyle(aStyle.getClass(), this);
                regStyle = myStyleRegistry.getDefaultStyleInstanceForStyleClass(aStyle.getClass());
                setToDefaultForFeatureType = true;
            }

            // If we can update the default instance of the style in the
            // registry then update it based on the new parameter set.
            if (regStyle != null)
            {
                regStyle.setParameters(aStyle.getStyleParameterSet(), this);
            }

            // If we needed to switch the default style class in the
            // registry do so here.
            if (setToDefaultForFeatureType && regStyle != null)
            {
                myStyleRegistry.setDefaultStyle(featureClass, regStyle.getClass(), this);
            }
        }
        else if (isTypeUsingCustom(dtnKey) && nodeType.isLeafType())
        {
            // This is a change for a specific data type. So try to retrieve
            // the existing style for that data type and feature type.
            VisualizationStyle regStyle = myStyleRegistry.getStyle(featureClass, dtiKey, false);
            if (regStyle == null)
            {
                // If we could not find a style for that data type and
                // feature type, then we will create the new style instance
                // update it with our parameter set, and install it as the
                // custom type to be used for that data type/feature type
                // combination.
                createDataTypeStyleUpdateAndInstall(dtnKey, dtiKey, featureClass, aStyle.getClass(), spsc, null, source);
            }
            else if (regStyle.getClass() == aStyle.getClass())
            {
                // There was already a style associated with that data
                // type/feature type combination. If they are of the same
                // class, update the style that is already in the registry
                // with our parameter set. If not then set the style for
                // that data/feature type combination.
                regStyle.setParameters(aStyle.getStyleParameterSet(), source);
            }
        }
    }

    /**
     * Save config state.
     */
    protected void saveConfigState()
    {
        myPrefs.putJAXBObject(PREFERENCE_KEY, myConfig, false, this);
    }

    /**
     * Activate custom types for data type by going through all the custom types
     * in the config and create/install the types in the registry.
     *
     * @param dtnKey the data type node key. See {@link DataTypeNodeUserObject}.
     * @param addIfNotFound the install defaults if not found
     * @param source the source of the activation request
     */
    private void activateCustomTypesForDataType(String dtnKey, boolean addIfNotFound, Object source)
    {
        Map<NodeKeyComponent, String> nodeKeyCompMap = DataTypeNodeUserObject.decomposeNodeKey(dtnKey);
        String dtiKey = nodeKeyCompMap.get(NodeKeyComponent.DATA_TYPE_INFO_ID);
        if (dtiKey == null || dtiKey.isEmpty())
        {
            return;
        }
        NodeType nt = NodeType.valueOf(nodeKeyCompMap.get(NodeKeyComponent.NODE_TYPE));
        if (!nt.isLeafType())
        {
            return;
        }

        DataTypeInfo dti = StyleManagerUtils.getDataTypeInfo(myToolbox, dtnKey);
        List<Class<? extends VisualizationSupport>> featureTypeSet = StyleManagerUtils
                .getDefaultFeatureClassesForDataType(myToolbox, nt, dti);

        DataTypeStyleConfig dtsc = myConfig.getDataTypeStyle(dtnKey);
        if (dtsc != null && dtsc.getFeatureTypeStyleConfigList() != null)
        {
            for (FeatureTypeStyleConfig fts : dtsc.getFeatureTypeStyleConfigList())
            {
                if (fts == null)
                {
                    continue;
                }
                String mgsName = fts.getBaseMGSClassName();
                if (mgsName == null || mgsName.isEmpty())
                {
                    continue;
                }
                String styleName = fts.getSelectedStyleClassName();
                if (styleName == null || styleName.isEmpty())
                {
                    continue;
                }
                StyleParameterSetConfig spsc = fts.getStyleParameterSetConfigForStyleClass(styleName);
                if (spsc == null)
                {
                    continue;
                }

                Class<? extends VisualizationSupport> mgsClass = StyleManagerUtils
                        .getClassForNameAndType(VisualizationSupport.class, mgsName);
                Class<? extends VisualizationStyle> selStyleClass = StyleManagerUtils
                        .getClassForNameAndType(VisualizationStyle.class, styleName);

                featureTypeSet.remove(mgsClass);
                VisualizationStyle vs = myStyleRegistry.getDefaultStyleInstanceForStyleClass(selStyleClass);
                createDataTypeStyleUpdateAndInstall(dtnKey, dtiKey, mgsClass, selStyleClass, spsc, vs, source);
            }
        }

        // If we need to install default styles as base for data
        // types that don't yet have any customization do so here
        // for the feature types we have not yet installed for.
        if (addIfNotFound && featureTypeSet != null && !featureTypeSet.isEmpty())
        {
            for (Class<? extends VisualizationSupport> ft : featureTypeSet)
            {
                VisualizationStyle vs = myStyleRegistry.getDefaultStyle(ft);
                if (vs != null)
                {
                    // Derive the new type for our data type and
                    // then save those to the config.
                    VisualizationStyle vsForDataType = vs.deriveForType(dtiKey);
                    VisualizationStyle dStyle = StyleManagerUtils.getNewInstance(myToolbox, vs.getClass());
                    dStyle.initialize();
                    Set<VisualizationStyleParameter> changedParams = vs.getChangedParameters(dStyle);
                    Set<VisualizationStyleParameter> alwaysSaveParams = vs.getAlwaysSaveParameters();
                    if (alwaysSaveParams != null && !alwaysSaveParams.isEmpty())
                    {
                        changedParams = new HashSet<>(changedParams);
                        changedParams.addAll(alwaysSaveParams);
                    }

                    StyleParameterSetConfig spsc = new StyleParameterSetConfig(vs.getClass().getName(), changedParams);
                    myConfig.setOrUpdateStyleParameterSet(spsc, ft.getName(), dtnKey);
                    saveConfigState();
                    myStyleRegistry.setStyle(ft, dtiKey, vsForDataType, source);
                }
            }
        }
    }

    /**
     * Creates the data type style update and install.
     *
     * @param dtnKey the data type node key. See {@link DataTypeNodeUserObject}.
     * @param dtiKey the {@link DataTypeInfo} key.
     * @param mgsClass the {@link MapGeometrySupport} feature class
     * @param selStyleClass the {@link VisualizationStyle} class to be selected.
     * @param spsc the {@link StyleParameterSetConfig} for the style
     * @param pVisStyle the {@link VisualizationStyle} class to be used.
     * @param source the source of the change
     */
    private void createDataTypeStyleUpdateAndInstall(String dtnKey, String dtiKey, Class<? extends VisualizationSupport> mgsClass,
            Class<? extends VisualizationStyle> selStyleClass, StyleParameterSetConfig spsc, VisualizationStyle pVisStyle,
            Object source)
    {
        VisualizationStyle vs = pVisStyle;
        if (vs == null)
        {
            vs = maybeInstall(selStyleClass, myStyleRegistry.getDefaultStyle(mgsClass), source);
        }
        if (vs == null)
        {
            return;
        }

        VisualizationStyle typedStyle = vs.deriveForType(dtiKey);
        if (typedStyle != null)
        {
            StyleManagerUtils.updateStyleWithParametersFromConfig(typedStyle, spsc, this);
        }

        myStyleRegistry.setStyle(mgsClass, dtiKey, typedStyle, source);
        synchronized (myTypesUsingCustom)
        {
            myTypesUsingCustom.add(dtnKey);
        }
    }

    /**
     * Creates the dtn key.
     *
     * @param dgi the dgi
     * @param dti the dti
     * @return the string
     */
    private String createDTNKey(DataGroupInfo dgi, DataTypeInfo dti)
    {
        Utilities.checkNull(dgi, "dgi");
        Utilities.checkNull(dti, "dti");
        NodeType nt = NodeType.FEATURE_TYPE_LEAF;
        if (dti.getMapVisualizationInfo() != null)
        {
            if (dti.getMapVisualizationInfo().isImageTileType() || dti.getMapVisualizationInfo().isImageType())
            {
                nt = NodeType.TILE_TYPE_LEAF;
            }
            else if (dti.getMapVisualizationInfo().getVisualizationType().isHeatmapType())
            {
                nt = NodeType.HEATMAP_TYPE_LEAF;
            }
        }
        return DataTypeNodeUserObject.createNodeKey(nt, dgi, dti, dti.getDisplayName());
    }

    /**
     * Process the types that are use custom and configure the registry.
     */
    private void initializeCustomTypes()
    {
        for (String dtnKey : myConfig.getUseCustomTypeKeysSet())
        {
            if (dtnKey != null && !dtnKey.isEmpty())
            {
                doCfgLock(() -> activateCustomTypesForDataType(dtnKey, false, this));
            }
        }
    }

    /**
     * Initialize default styles in registry.
     */
    private void initializeDefaultStylesInRegistry()
    {
        for (DataTypeStyleConfig dts : myConfig.getDataTypeStyles())
        {
            if (dts == null || dts.getDataTypeKey().isEmpty())
            {
                continue;
            }

            Map<NodeKeyComponent, String> nodeKeyCompMap = DataTypeNodeUserObject.decomposeNodeKey(dts.getDataTypeKey());
            NodeType nt = NodeType.valueOf(nodeKeyCompMap.get(NodeKeyComponent.NODE_TYPE));
            if (nt != NodeType.DEFAULT_ROOT_FEATURE && nt != NodeType.DEFAULT_ROOT_TILE
                    || dts.getFeatureTypeStyleConfigList() == null)
            {
                continue;
            }

            for (FeatureTypeStyleConfig fts : dts.getFeatureTypeStyleConfigList())
            {
                Class<? extends VisualizationSupport> mgsClass = StyleManagerUtils
                        .getClassForNameAndType(VisualizationSupport.class, fts.getBaseMGSClassName());

                if (mgsClass != null)
                {
                    List<StyleParameterSetConfig> psetList = fts.getStyleParameterSetConfigList();
                    for (StyleParameterSetConfig spsc : psetList)
                    {
                        Class<? extends VisualizationStyle> styleClass = StyleManagerUtils
                                .getClassForNameAndType(VisualizationStyle.class, spsc.getStyleClassName());
                        if (styleClass == null)
                        {
                            continue;
                        }

                        VisualizationStyle defaultStyle = maybeInstall(styleClass,
                                myStyleRegistry.getDefaultStyleInstanceForStyleClass(styleClass), this);
                        if (defaultStyle != null)
                        {
                            StyleManagerUtils.updateStyleWithParametersFromConfig(defaultStyle, spsc, this);
                        }
                    }
                }

                Class<? extends VisualizationStyle> selStyleClass = StyleManagerUtils
                        .getClassForNameAndType(VisualizationStyle.class, fts.getSelectedStyleClassName());

                myStyleRegistry.setDefaultStyle(mgsClass, selStyleClass, this);
            }
        }
    }
}
