package io.opensphere.mantle.toolbox;

import java.io.File;
import java.util.Properties;

import io.opensphere.core.Toolbox;
import io.opensphere.core.datafilter.columns.MutableColumnMappingController;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.controller.impl.DataGroupControllerImpl;
import io.opensphere.mantle.controller.impl.DataTypeControllerImpl;
import io.opensphere.mantle.data.ColumnTypeDetector;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.analysis.DataAnalysisReporter;
import io.opensphere.mantle.data.analysis.impl.DataAnalysisReporterImpl;
import io.opensphere.mantle.data.analysis.impl.DisabledDataAnalysisReporter;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.mantle.data.cache.impl.DataElementCacheImpl;
import io.opensphere.mantle.data.columns.gui.ColumnMappingOptionsProvider;
import io.opensphere.mantle.data.columns.gui.ColumnMappingResourcesImpl;
import io.opensphere.mantle.data.dynmeta.DynamicDataElementMetadataManager;
import io.opensphere.mantle.data.dynmeta.impl.DynamicMetadataManagerImpl;
import io.opensphere.mantle.data.geom.factory.MapGeometrySupportConverterRegistry;
import io.opensphere.mantle.data.geom.factory.impl.MapGeometrySupportConverterRegistryImpl;
import io.opensphere.mantle.data.geom.style.VisualizationStyleController;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry;
import io.opensphere.mantle.data.geom.style.dialog.StyleManagerController;
import io.opensphere.mantle.data.geom.style.impl.VisualizationStyleRegistryImpl;
import io.opensphere.mantle.data.impl.AbstractDynamicMetaDataList;
import io.opensphere.mantle.data.impl.ColumnTypeDetectorImpl;
import io.opensphere.mantle.data.impl.DataTypeInfoPreferenceAssistantImpl;
import io.opensphere.mantle.data.util.DataElementActionUtils;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.mantle.data.util.DataElementUpdateUtils;
import io.opensphere.mantle.data.util.impl.DataElementActionUtilsImpl;
import io.opensphere.mantle.data.util.impl.DataElementLookupUtilsImpl;
import io.opensphere.mantle.data.util.impl.DataElementUpdateUtilsImpl;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.IconRegistryImpl;
import io.opensphere.mantle.mp.MapAnnotationPointRegistry;
import io.opensphere.mantle.mp.impl.MapAnnotationPointRegistryImpl;
import io.opensphere.mantle.plugin.queryregion.QueryRegionManager;
import io.opensphere.mantle.plugin.queryregion.impl.QueryRegionManagerImpl;
import io.opensphere.mantle.plugin.selection.SelectionCommand;
import io.opensphere.mantle.plugin.selection.SelectionHandler;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;
import io.opensphere.mantle.util.dynenum.impl.DynamicEnumerationRegistryImpl;

/**
 * Implementation of the mantleToolbox.
 */
public class MantleToolboxImpl implements MantleToolbox
{
    /** The Data analysis reporter. */
    private DataAnalysisReporter myDataAnalysisReporter;

    /** The Data element action utils. */
    private final DataElementActionUtils myDataElementActionUtils;

    /** The data element cache. */
    private final DataElementCache myDataElementCache;

    /** The Data element lookup utils. */
    private final DataElementLookupUtils myDataElementLookupUtils;

    /** The Data element update utils. */
    private final DataElementUpdateUtils myDataElementUpdateUtils;

    /** The data group controller. */
    private final DataGroupController myDataGroupController;

    /** The data type controller. */
    private final DataTypeControllerImpl myDataTypeController;

    /** The Data type info preference assistant. */
    private final DataTypeInfoPreferenceAssistant myDataTypeInfoPreferenceAssistant;

    /** The Dynamic column manager. */
    private final DynamicDataElementMetadataManager myDynamicColumnManager;

    /** The Dynamic enumeration registry. */
    private final DynamicEnumerationRegistry myDynamicEnumerationRegistry;

    /** The Icon registry. */
    private final IconRegistry myIconRegistry;

    /** The my map annotation point registry. */
    private final MapAnnotationPointRegistry myMapAnnotationPointRegistry;

    /** The map geometry support converter registry. */
    private final MapGeometrySupportConverterRegistry myMapGeometrySupportConverterRegistry;

    /** The my parent toolbox. */
    private final Toolbox myParentToolbox;

    /** The query region manager. */
    private final QueryRegionManager myQueryRegionManager;

    /** The selection handler. */
    private final SelectionHandler mySelectionHandler;

    /** The Visualization style controller. */
    private final VisualizationStyleController myVisualizationStyleController;

    /** The Visualization style registry. */
    private final VisualizationStyleRegistry myVisualizationStyleRegistry;

    /** The timeline registry controller. */
    private final TimelineRegistryController myTimelineRegistryController;

    /** The column type detector. */
    private final ColumnTypeDetector myColumnTypeDetector;

    /**
     * Instantiates a new mantle toolbox.
     *
     * @param aToolbox the toolbox
     * @param pluginProperties the plugin properties
     */
    public MantleToolboxImpl(Toolbox aToolbox, Properties pluginProperties)
    {
        myParentToolbox = aToolbox;
        myDataElementLookupUtils = new DataElementLookupUtilsImpl(myParentToolbox);
        myDataElementUpdateUtils = new DataElementUpdateUtilsImpl(myParentToolbox);
        myDataElementActionUtils = new DataElementActionUtilsImpl(myParentToolbox);
        myDynamicColumnManager = new DynamicMetadataManagerImpl(aToolbox);
        myDynamicEnumerationRegistry = new DynamicEnumerationRegistryImpl();
        AbstractDynamicMetaDataList.setDynamicEnumRegistry(myDynamicEnumerationRegistry);
        myDataElementCache = new DataElementCacheImpl(aToolbox, MantleCacheUtils.getElementCacheConfiguration(pluginProperties),
                (DynamicMetadataManagerImpl)myDynamicColumnManager, myDynamicEnumerationRegistry);
        myColumnTypeDetector = new ColumnTypeDetectorImpl();
        myDataTypeController = new DataTypeControllerImpl(aToolbox, (DataElementCacheImpl)myDataElementCache, myColumnTypeDetector);
        myDataTypeController.initialize();
        myDataGroupController = new DataGroupControllerImpl(aToolbox);
        final QueryRegionManagerImpl qrmi = new QueryRegionManagerImpl(aToolbox, myDataGroupController);
        qrmi.open();
        myQueryRegionManager = qrmi;
        mySelectionHandler = new SelectionHandler(aToolbox, myDataGroupController, myDataTypeController, qrmi, myDataElementCache,
                myDataElementUpdateUtils);
        mySelectionHandler.install(aToolbox);
        mySelectionHandler.registerSelectionCommandProcessor(SelectionCommand.ADD_FEATURES, qrmi);
        mySelectionHandler.registerSelectionCommandProcessor(SelectionCommand.ADD_FEATURES_CURRENT_FRAME, qrmi);
        mySelectionHandler.registerSelectionCommandProcessor(SelectionCommand.LOAD_FEATURES, qrmi);
        mySelectionHandler.registerSelectionCommandProcessor(SelectionCommand.LOAD_FEATURES_CURRENT_FRAME, qrmi);
        mySelectionHandler.registerSelectionCommandProcessor(SelectionCommand.CANCEL_QUERY, qrmi);
        myDataTypeInfoPreferenceAssistant = new DataTypeInfoPreferenceAssistantImpl(myParentToolbox);
        myIconRegistry = createIconRegistry(pluginProperties);
        myMapGeometrySupportConverterRegistry = new MapGeometrySupportConverterRegistryImpl(aToolbox);
        myMapAnnotationPointRegistry = createMapAnnotationPointRegistry(aToolbox, pluginProperties);
        final String enableColumnDataAnalysisStr = pluginProperties.getProperty("enableColumnDataAnalysis", "false");
        if (!"true".equalsIgnoreCase(enableColumnDataAnalysisStr))
        {
            myDataAnalysisReporter = new DisabledDataAnalysisReporter();
        }
        myTimelineRegistryController = new TimelineRegistryController(aToolbox.getEventManager(), myDataElementCache,
                myDataTypeController, aToolbox.getUIRegistry().getTimelineRegistry());
        myTimelineRegistryController.open();
        initColumnMappings(aToolbox);
        myVisualizationStyleRegistry = new VisualizationStyleRegistryImpl(aToolbox);
        myVisualizationStyleController = new StyleManagerController(aToolbox, myVisualizationStyleRegistry);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.MantleToolbox#getColumnTypeDetector()
     */
    @Override
    public ColumnTypeDetector getColumnTypeDetector()
    {
        return myColumnTypeDetector;
    }

    @Override
    public synchronized DataAnalysisReporter getDataAnalysisReporter()
    {
        if (myDataAnalysisReporter == null)
        {
            myDataAnalysisReporter = new DataAnalysisReporterImpl(myParentToolbox);
        }
        return myDataAnalysisReporter;
    }

    @Override
    public DataElementActionUtils getDataElementActionUtils()
    {
        return myDataElementActionUtils;
    }

    @Override
    public DataElementCache getDataElementCache()
    {
        return myDataElementCache;
    }

    @Override
    public DataElementLookupUtils getDataElementLookupUtils()
    {
        return myDataElementLookupUtils;
    }

    @Override
    public DataElementUpdateUtils getDataElementUpdateUtils()
    {
        return myDataElementUpdateUtils;
    }

    @Override
    public DataGroupController getDataGroupController()
    {
        return myDataGroupController;
    }

    @Override
    public DataTypeController getDataTypeController()
    {
        return myDataTypeController;
    }

    @Override
    public DataTypeInfoPreferenceAssistant getDataTypeInfoPreferenceAssistant()
    {
        return myDataTypeInfoPreferenceAssistant;
    }

    @Override
    public String getDescription()
    {
        return "A toolbox extension for the Mantle";
    }

    @Override
    public DynamicDataElementMetadataManager getDynamicDataElementMetadataManager()
    {
        return myDynamicColumnManager;
    }

    @Override
    public DynamicEnumerationRegistry getDynamicEnumerationRegistry()
    {
        return myDynamicEnumerationRegistry;
    }

    @Override
    public IconRegistry getIconRegistry()
    {
        return myIconRegistry;
    }

    @Override
    public MapAnnotationPointRegistry getMapAnnotationPointRegistry()
    {
        return myMapAnnotationPointRegistry;
    }

    @Override
    public MapGeometrySupportConverterRegistry getMapGeometrySupportConverterRegistry()
    {
        return myMapGeometrySupportConverterRegistry;
    }

    @Override
    public QueryRegionManager getQueryRegionManager()
    {
        return myQueryRegionManager;
    }

    @Override
    public SelectionHandler getSelectionHandler()
    {
        return mySelectionHandler;
    }

    @Override
    public VisualizationStyleController getVisualizationStyleController()
    {
        return myVisualizationStyleController;
    }

    @Override
    public VisualizationStyleRegistry getVisualizationStyleRegistry()
    {
        return myVisualizationStyleRegistry;
    }

    /**
     * Creates the icon registry.
     *
     * @param pluginProperties the plugin properties
     * @return the icon registry
     */
    private IconRegistry createIconRegistry(Properties pluginProperties)
    {
        final String runtimeDir = StringUtilities.expandProperties(System.getProperty("opensphere.path.runtime"), System.getProperties());
        final File iconCacheParent = new File(runtimeDir + File.separator + "iconCache");
        final String iconCacheLocation = pluginProperties.getProperty("iconCacheLocation", iconCacheParent.getAbsolutePath());
        final File iconCache = new File(iconCacheLocation);
        return new IconRegistryImpl(myParentToolbox, iconCache);
    }

    /**
     * Creates the map annotation point registry.
     *
     * @param aToolbox the a toolbox
     * @param pluginProperties the plugin properties
     * @return the map annotation point registry
     */
    private MapAnnotationPointRegistry createMapAnnotationPointRegistry(Toolbox aToolbox, Properties pluginProperties)
    {
        final String persistenceHelperClass = pluginProperties.getProperty("mapAnnotationRegistryPersistenceHelperClass");
        final MapAnnotationPointRegistryImpl reg = new MapAnnotationPointRegistryImpl(aToolbox, persistenceHelperClass);
        reg.initialize();
        return reg;
    }

    /**
     * Initializes the column mapping UI.
     *
     * @param toolbox the toolbox
     */
    private void initColumnMappings(Toolbox toolbox)
    {
        final ColumnMappingResourcesImpl resources = new ColumnMappingResourcesImpl(
                (MutableColumnMappingController)toolbox.getDataFilterRegistry().getColumnMappingController(),
                toolbox.getUIRegistry().getMainFrameProvider(), myDataGroupController);
        toolbox.getUIRegistry().getOptionsRegistry().addOptionsProvider(new ColumnMappingOptionsProvider(myParentToolbox, resources));
    }
}
