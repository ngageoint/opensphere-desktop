package io.opensphere.mantle;

import io.opensphere.core.PluginToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.ColumnTypeDetector;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.analysis.DataAnalysisReporter;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.mantle.data.dynmeta.DynamicDataElementMetadataManager;
import io.opensphere.mantle.data.geom.factory.MapGeometrySupportConverterRegistry;
import io.opensphere.mantle.data.geom.style.VisualizationStyleController;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry;
import io.opensphere.mantle.data.util.DataElementActionUtils;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.mantle.data.util.DataElementUpdateUtils;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.mp.MapAnnotationPointRegistry;
import io.opensphere.mantle.plugin.queryregion.QueryRegionManager;
import io.opensphere.mantle.plugin.selection.SelectionHandler;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;

/**
 * A Toolbox For Mantle Services.
 */
public interface MantleToolbox extends PluginToolbox
{
    /**
     * Gets the columnTypeDetector.
     *
     * @return the columnTypeDetector.
     */
    ColumnTypeDetector getColumnTypeDetector();

    /**
     * Gets the data analysis reporter.
     *
     * @return the data analysis reporter
     */
    DataAnalysisReporter getDataAnalysisReporter();

    /**
     * Gets the {@link DataElementActionUtils}.
     *
     * @return the data element action utils
     */
    DataElementActionUtils getDataElementActionUtils();

    /**
     * Gets the data element cache.
     *
     * @return the data element cache
     */
    DataElementCache getDataElementCache();

    /**
     * Gets the {@link DataElementLookupUtils}.
     *
     * @return the data element lookup utils
     */
    DataElementLookupUtils getDataElementLookupUtils();

    /**
     * Gets the {@link DataElementUpdateUtils}.
     *
     * @return the data element update utils
     */
    DataElementUpdateUtils getDataElementUpdateUtils();

    /**
     * Gets the data group controller.
     *
     * @return the data group controller.
     */
    DataGroupController getDataGroupController();

    /**
     * Gets the data type controller.
     *
     * @return the data type controller
     */
    DataTypeController getDataTypeController();

    /**
     * Gets the data type info preference assistant.
     *
     * @return the data type info preference assistant
     */
    DataTypeInfoPreferenceAssistant getDataTypeInfoPreferenceAssistant();

    /**
     * Gets the dynamic data element metadata manager.
     *
     * @return the dynamic data element metadata manager
     */
    DynamicDataElementMetadataManager getDynamicDataElementMetadataManager();

    /**
     * Gets the dynamic enumeration registry.
     *
     * @return the dynamic enumeration registry
     */
    DynamicEnumerationRegistry getDynamicEnumerationRegistry();

    /**
     * Gets the icon registry.
     *
     * @return the icon registry
     */
    IconRegistry getIconRegistry();

    /**
     * Gets the map annotation point registry.
     *
     * @return the map annotation point registry
     */
    MapAnnotationPointRegistry getMapAnnotationPointRegistry();

    /**
     * Gets the map geometry support converter registry.
     *
     * @return the map geometry support converter registry
     */
    MapGeometrySupportConverterRegistry getMapGeometrySupportConverterRegistry();

    /**
     * Gets the query region manager.
     *
     * @return the query region manager
     */
    QueryRegionManager getQueryRegionManager();

    /**
     * Gets the selection handler.
     *
     * @return the selection handler
     */
    SelectionHandler getSelectionHandler();

    /**
     * Gets the {@link VisualizationStyleController}.
     *
     * @return the {@link VisualizationStyleController}
     */
    VisualizationStyleController getVisualizationStyleController();

    /**
     * Gets the visualization style registry.
     *
     * @return the visualization style registry
     */
    VisualizationStyleRegistry getVisualizationStyleRegistry();

    /**
     * Gets the data type info from the data type key.
     *
     * @param typeKey the {@link DataTypeInfo} key.
     * @return the {@link DataTypeInfo} or null if not found
     */
    default DataTypeInfo getDataTypeInfoFromKey(String typeKey)
    {
        DataTypeInfo dataType = getDataTypeController().getDataTypeInfoForType(typeKey);
        if (dataType == null)
        {
            dataType = getDataGroupController().findMemberById(typeKey);
        }
        return dataType;
    }
}
