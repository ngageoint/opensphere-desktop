package io.opensphere.kml.mantle.controller;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.jcip.annotations.ThreadSafe;

import org.apache.log4j.Logger;

import de.micromata.opengis.kml.v_2_2_0.NetworkLink;
import de.micromata.opengis.kml.v_2_2_0.Overlay;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.kml.common.model.KMLDataEvent;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.model.KMLFeatureUtils;
import io.opensphere.kml.common.model.KMLMapController;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;
import io.opensphere.kml.mantle.view.KMLBalloonDialog;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.TimeExtents;
import io.opensphere.mantle.data.element.event.DataElementDoubleClickedEvent;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.data.util.impl.DataTypeActionUtils;

/**
 * KML Mantle controller.
 */
@SuppressWarnings("PMD.GodClass")
@ThreadSafe
public class KMLMantleController extends EventListenerService implements KMLMapController
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(KMLMantleController.class);

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The mantle toolbox. */
    private final MantleToolbox myMantleToolbox;

    /** The transformer that converts the KML models into geometries. */
    private final KMLTransformer myTransformer;

    /** The feature to ID cache. */
    private final KMLFeatureIdCache myFeatureIdCache;

    /** The bundle preferences. */
    private final Preferences myBundlePreferences;

    /** The popup dialog. */
    @ThreadConfined("EDT")
    private KMLBalloonDialog myBalloonDialog;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     */
    public KMLMantleController(Toolbox toolbox)
    {
        super(toolbox.getEventManager(), 1);

        myToolbox = toolbox;
        myMantleToolbox = toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        myFeatureIdCache = new KMLFeatureIdCache();
        myTransformer = new KMLTransformer(toolbox, myFeatureIdCache);
        myBundlePreferences = toolbox.getPreferencesRegistry().getPreferences("io.opensphere.kml.BundlePrefs");

        bindEvent(DataElementDoubleClickedEvent.class, this::handleDataElementDoubleClicked);
    }

    @Override
    public void addData(KMLDataEvent dataEvent, boolean reload)
    {
        KMLDataSource dataSource = dataEvent.getDataSource();
        KMLFeature rootFeature = dataEvent.getData();

        // Get a list of new features
        Collection<KMLFeature> newFeatures = rootFeature.getAllFeatures().stream()
                .filter(f -> f.isAdded() && (f.getFeature() instanceof Placemark || f.getFeature() instanceof Overlay))
                .collect(Collectors.toList());

        // Add the data types to the DataTypeController
        if (dataSource.getCreatingFeature() == null && !reload)
        {
            for (DataTypeInfo dataType : dataSource.getDataGroupInfo().getMembers(false))
            {
                if (KMLMantleUtilities.KML.equals(dataType.getTypeName()))
                {
                    addExtendedData(dataType.getMetaDataInfo(), newFeatures);

                    String source = "KML:" + dataType.getTypeKey();
                    myMantleToolbox.getDataTypeController().addDataType(source, dataType.getDisplayName(), dataType, null);
                }
            }
        }

        // Handle reactivation
        if (reload)
        {
            // Remove old features
            if (dataEvent.getOldData() != null)
            {
                removeFeatures(dataEvent.getOldData().getAllFeatures());
            }

            updateExistingFeatures(rootFeature);
        }

        // Add any new features to the mantle
        addFeatures(newFeatures);

        // Update the selection/visibility state
        updateFeatureVisibility(newFeatures);

        DataTypeInfo dataType = myMantleToolbox.getDataTypeController().getDataTypeInfoForType(dataSource.getDataTypeKey());

        // Update the data type in mantle
        updateDataType(dataType, dataSource.getRootDataSource().getAllFeatures(), newFeatures);

        boolean zoomTo = myBundlePreferences.getBoolean("zoomTo", false);
        if (zoomTo)
        {
            DataTypeActionUtils.gotoDataType(dataType, myToolbox.getMapManager().getStandardViewer(), myToolbox, true);
        }
    }

    @Override
    public void addFeatures(Collection<? extends KMLFeature> features)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Adding " + features.size() + " features");
        }

        for (Map.Entry<KMLDataSource, List<KMLFeature>> entry : groupFeaturesByDataSource(features).entrySet())
        {
            KMLDataSource dataSource = entry.getKey();
            List<KMLFeature> dataSourceFeatures = entry.getValue();

            String dataTypeKey = dataSource.getDataTypeKey();
            DataTypeInfo dataType = myMantleToolbox.getDataTypeController().getDataTypeInfoForType(dataTypeKey);
            if (dataType != null)
            {
                List<KMLFeature> mantleAdditions = dataSourceFeatures.stream()
                        .filter(f -> f.getFeature() instanceof Placemark && f.isVisibility().booleanValue() && f.getId() == 0)
                        .collect(Collectors.toList());
                if (!mantleAdditions.isEmpty())
                {
                    KMLDataElementProvider provider = new KMLDataElementProvider(myToolbox.getServerProviderRegistry(),
                            myMantleToolbox, myToolbox.getDataRegistry(), dataSource, dataType, mantleAdditions);
                    if (provider.hasNext())
                    {
                        List<Long> ids = myMantleToolbox.getDataTypeController().addDataElements(provider, null, null, this);
                        myFeatureIdCache.addFeatures(mantleAdditions, ids);
                    }
                }

                if (!dataSourceFeatures.isEmpty())
                {
                    myTransformer.addFeatures(dataSourceFeatures, dataType);
                }
            }
            else
            {
                LOGGER.error("No DataTypeInfo found for type: " + dataTypeKey);
            }
        }
    }

    /**
     * Getter for transformer.
     *
     * @return the transformer
     */
    public KMLTransformer getTransformer()
    {
        return myTransformer;
    }

    @Override
    public void removeData(KMLDataSource dataSource)
    {
        if (dataSource.getCreatingFeature() instanceof NetworkLink)
        {
            Collection<KMLFeature> features = dataSource.getAllFeatures();
            for (KMLFeature feature : features)
            {
                feature.setVisibility(Boolean.FALSE);
            }
            updateFeatureVisibility(features);
        }
        else if (!dataSource.isStyleSource())
        {
            Collection<KMLFeature> features = dataSource.getAllFeatures();
            removeFeatures(features);
        }
    }

    @Override
    public void setFeaturesSelected(Collection<? extends KMLFeature> features, boolean isSelected)
    {
        // Set the mantle feature selection states
        for (Map.Entry<KMLDataSource, List<KMLFeature>> entry : groupFeaturesByDataSource(features).entrySet())
        {
            KMLDataSource dataSource = entry.getKey();
            List<KMLFeature> dataSourceFeatures = entry.getValue();

            List<Long> ids = KMLFeatureIdCache.getIds(dataSourceFeatures);
            myMantleToolbox.getDataElementUpdateUtils().setDataElementsSelected(isSelected, ids, dataSource.getDataTypeKey(),
                    this);
        }
    }

    @Override
    public void showFeatureDetails(final KMLFeature feature)
    {
        EventQueueUtilities.runOnEDT(() -> showBalloon(feature));
    }

    @Override
    public void updateFeatureVisibility(Collection<? extends KMLFeature> features)
    {
        Map<Boolean, List<KMLFeature>> visibilityMap = features.stream()
                .collect(Collectors.<KMLFeature>partitioningBy(f -> f.isVisibility().booleanValue() && f.isRegionActive()));
        for (Map.Entry<Boolean, List<KMLFeature>> entry : visibilityMap.entrySet())
        {
            boolean isVisible = entry.getKey().booleanValue();
            List<KMLFeature> partitionedFeatures = entry.getValue();
            if (!partitionedFeatures.isEmpty())
            {
                List<Long> ids = KMLFeatureIdCache.getIds(partitionedFeatures);
                if (!ids.isEmpty())
                {
                    myMantleToolbox.getDataElementUpdateUtils().setDataElementsVisibility(isVisible, ids, null, this);
                }
            }
        }

        myTransformer.updateVisibility(features);
    }

    /**
     * Handles a DataElementDoubleClickedEvent.
     *
     * @param event the event
     */
    private void handleDataElementDoubleClicked(DataElementDoubleClickedEvent event)
    {
        if (!event.isConsumed() && event.getDataTypeKey().startsWith(KMLMantleUtilities.KML))
        {
            event.consume();

            KMLFeature feature = myFeatureIdCache.getFeature(Long.valueOf(event.getRegistryId()));
            if (feature != null)
            {
                showFeatureDetails(feature);
            }
        }
    }

    /**
     * Removes data elements.
     *
     * @param dataSource The KML data source
     * @param ids The list of ids
     */
    private void removeDataElements(KMLDataSource dataSource, Collection<Long> ids)
    {
        if (!ids.isEmpty())
        {
            DataTypeInfo dataType = myMantleToolbox.getDataTypeController().getDataTypeInfoForType(dataSource.getDataTypeKey());
            long[] idArray = CollectionUtilities.toLongArray(ids);
            myMantleToolbox.getDataTypeController().removeDataElements(dataType, idArray);
        }
    }

    /**
     * Removes features from the controller.
     *
     * @param features The list of features
     */
    private void removeFeatures(Collection<? extends KMLFeature> features)
    {
        for (Map.Entry<KMLDataSource, List<KMLFeature>> entry : groupFeaturesByDataSource(features).entrySet())
        {
            KMLDataSource dataSource = entry.getKey();
            List<KMLFeature> dataSourceFeatures = entry.getValue();

            // Get the list of ids to remove (and remove from cache)
            Collection<Long> ids = myFeatureIdCache.removeFeatures(dataSourceFeatures);

            // Remove the features from mantle
            removeDataElements(dataSource, ids);

            myTransformer.removeFeatures(dataSourceFeatures, dataSource.getDataTypeKey());
        }
    }

    /**
     * Updates the data type in mantle.
     *
     * @param dataType The data type
     * @param existingFeatures The existing features
     * @param newFeatures The new features being added
     */
    private void updateDataType(DataTypeInfo dataType, Collection<KMLFeature> existingFeatures,
            Collection<? extends KMLFeature> newFeatures)
    {
        // Update time extents
        if (dataType instanceof DefaultDataTypeInfo)
        {
            // Get a list of all existing and new features
            Collection<KMLFeature> allFeatures = existingFeatures;
            allFeatures.addAll(newFeatures);

            TimeExtents timeExtents = KMLSpatialTemporalUtils.getTimeExtents(allFeatures);
            ((DefaultDataTypeInfo)dataType).setTimeExtents(timeExtents, this);

            GeographicBoundingBox bbox = allFeatures.stream().filter(f -> f.getGeoBoundingBox() != null)
                    .map(f -> f.getGeoBoundingBox())
                    .reduce((b1, b2) -> GeographicBoundingBox.merge(b1, b2, Altitude.ReferenceLevel.TERRAIN)).orElse(null);
            if (bbox != null)
            {
                try
                {
                    ((DefaultDataTypeInfo)dataType).addBoundingBox(bbox);
                }
                catch (IllegalArgumentException e)
                {
                    LOGGER.error(e);
                }
            }
        }
    }

    /**
     * Updates existing features in the cache.
     *
     * @param rootFeature The root feature
     */
    private void updateExistingFeatures(KMLFeature rootFeature)
    {
        Collection<KMLFeature> featuresToUpdate = rootFeature.getAllFeatures().stream()
                .filter(f -> f.getFeature() instanceof Placemark && !f.isAdded()).collect(Collectors.toList());
        if (!featuresToUpdate.isEmpty())
        {
            myFeatureIdCache.updateFeatures(featuresToUpdate);
        }
    }

    /**
     * Shows the balloon, creating the dialog if necessary.
     *
     * @param feature the feature to show
     */
    private void showBalloon(KMLFeature feature)
    {
        if (myBalloonDialog == null)
        {
            myBalloonDialog = new KMLBalloonDialog(myToolbox.getUIRegistry().getMainFrameProvider().get());
        }
        myBalloonDialog.show(feature);
    }

    /**
     * Adds extended data to the metadata info.
     *
     * @param metaDataInfo The meta data info
     * @param features The features
     */
    private static void addExtendedData(MetaDataInfo metaDataInfo, Collection<? extends KMLFeature> features)
    {
        boolean added = false;
        for (KMLFeature feature : features)
        {
            if (feature.getFeature() instanceof Placemark && feature.getExtendedData() != null)
            {
                Map<String, String> extendedDataMap = KMLFeatureUtils.getExtendedDataMap(feature.getFeature());
                for (String key : extendedDataMap.keySet())
                {
                    String cleanKey = StringUtilities.removeHTML(key);
                    if (!metaDataInfo.hasKey(cleanKey))
                    {
                        added |= metaDataInfo.addKey(cleanKey, String.class, null);
                    }
                }
            }
        }
        if (added && metaDataInfo instanceof DefaultMetaDataInfo)
        {
            ((DefaultMetaDataInfo)metaDataInfo).copyKeysToOriginalKeys();
        }
    }

    /**
     * Groups a Collection of Features into a map of data source to that data
     * source's features.
     *
     * @param features The features
     * @return The map of data source to features
     */
    private static Map<KMLDataSource, List<KMLFeature>> groupFeaturesByDataSource(Collection<? extends KMLFeature> features)
    {
        return features.stream().collect(Collectors.<KMLFeature, KMLDataSource>groupingBy(f -> f.getDataSource()));
    }
}
