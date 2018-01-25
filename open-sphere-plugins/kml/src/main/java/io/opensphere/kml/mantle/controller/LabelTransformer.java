package io.opensphere.kml.mantle.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.concurrent.ThreadSafe;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.util.KMLStyleCache;
import io.opensphere.kml.common.util.KMLToolboxUtils;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementHighlightChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementVisibilityChangeEvent;

/**
 * Handles label geometries.
 */
@ThreadSafe
public class LabelTransformer extends AbstractKMLTransformer
{
    /** The feature ID cache. */
    private final KMLFeatureIdCache myFeatureIdCache;

    /** The style cache. */
    private final KMLStyleCache myStyleCache;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param featureIdCache the feature to id mapping
     */
    public LabelTransformer(Toolbox toolbox, KMLFeatureIdCache featureIdCache)
    {
        super(toolbox);

        myFeatureIdCache = featureIdCache;
        myStyleCache = KMLToolboxUtils.getKmlToolbox().getStyleCache();

        getListenerService().bindEvent(ConsolidatedDataElementVisibilityChangeEvent.class,
                this::handleDataElementVisibilityChange);
        getListenerService().bindEvent(ConsolidatedDataElementHighlightChangeEvent.class,
                this::handleDataElementHighlightChangeEvent);
    }

    @Override
    public void addFeatures(Collection<? extends KMLFeature> features, DataTypeInfo dataType)
    {
        // Create and store the labels in the features
        for (KMLFeature feature : features)
        {
            if (feature.getFeature() instanceof Placemark)
            {
                LabelGeometry geom = LabelHelper.createLabel(feature, myStyleCache);
                if (geom != null)
                {
                    feature.setLabel(geom);
                }
            }
        }

        Collection<KMLFeature> labelFeatures = features.stream().filter(f -> f.getLabel() != null).collect(Collectors.toList());
        if (!labelFeatures.isEmpty())
        {
            updateVisibility(labelFeatures);

            CollectionUtilities.multiMapAddAll(getDataTypeKeyToFeatureMap(), dataType.getTypeKey(), labelFeatures, false);

            Collection<Geometry> labelGeoms = labelFeatures.stream().map(f -> f.getLabel()).collect(Collectors.toList());
            publishGeometries(labelGeoms, Collections.emptyList());
        }
    }

    @Override
    public void removeFeatures(Collection<? extends KMLFeature> features, String dataTypeKey)
    {
        Collection<KMLFeature> labelFeatures = features.stream().filter(f -> f.getLabel() != null).collect(Collectors.toSet());
        if (!labelFeatures.isEmpty())
        {
            CollectionUtilities.multiMapRemoveAll(getDataTypeKeyToFeatureMap(), dataTypeKey, labelFeatures);

            Collection<Geometry> labelGeoms = labelFeatures.stream().map(f -> f.getLabel()).collect(Collectors.toList());
            publishGeometries(Collections.emptyList(), labelGeoms);

            for (KMLFeature feature : labelFeatures)
            {
                feature.setLabel(null);
            }
        }
    }

    @Override
    public void setOpacity(DataTypeInfo dataTypeInfo, int opacity)
    {
        List<KMLFeature> features = getDataTypeKeyToFeatureMap().get(dataTypeInfo.getTypeKey());
        if (features != null)
        {
            for (KMLFeature feature : features)
            {
                LabelHelper.setOpacity(feature.getLabel(), opacity);
            }
        }
    }

    @Override
    protected void setVisibility(KMLFeature feature, boolean isVisible)
    {
        if (feature.getLabel() != null)
        {
            LabelHelper.setVisibility(feature, isVisible);
        }
    }

    /**
     * Handles a ConsolidatedDataElementVisibilityChangeEvent.
     *
     * @param event the event
     */
    private void handleDataElementVisibilityChange(final ConsolidatedDataElementVisibilityChangeEvent event)
    {
        if (event.getDataTypeKeys().stream().anyMatch(key -> key.startsWith(KMLMantleUtilities.KML)))
        {
            ThreadUtilities.runBackground(() -> updateVisibility(event));
        }
    }

    /**
     * Handles a ConsolidatedDataElementHighlightChangeEvent.
     *
     * @param event the event
     */
    private void handleDataElementHighlightChangeEvent(final ConsolidatedDataElementHighlightChangeEvent event)
    {
        if (event.getDataTypeKeys().stream().anyMatch(key -> key.startsWith(KMLMantleUtilities.KML)))
        {
            ThreadUtilities.runBackground(() -> updateHighlight(event));
        }
    }

    /**
     * Updates feature highlight states based on the event.
     *
     * @param event the event
     */
    private void updateHighlight(ConsolidatedDataElementHighlightChangeEvent event)
    {
        // we only allow one element to be highlighted at a time, so
        // unhighlight everything(the entire id set)
        for (Long id : event.getRegistryIds())
        {
            KMLFeature feature = myFeatureIdCache.getFeature(id);
            LabelHelper.setHighlighted(feature, myStyleCache, false);
        }

        // then highlight only the selected icon
        for (Long id : event.getHighlightedIdSet())
        {
            KMLFeature feature = myFeatureIdCache.getFeature(id);
            LabelHelper.setHighlighted(feature, myStyleCache, true);
        }
    }

    /**
     * Updates feature visibilities based on the event.
     *
     * @param event the event
     */
    private void updateVisibility(ConsolidatedDataElementVisibilityChangeEvent event)
    {
        if (!event.getVisibleIdSet().isEmpty())
        {
            for (KMLFeature feature : myFeatureIdCache.getFeatures(event.getVisibleIdSet()))
            {
                setVisibility(feature, true);
            }
        }

        if (!event.getInvisibleIdSet().isEmpty())
        {
            for (KMLFeature feature : myFeatureIdCache.getFeatures(event.getInvisibleIdSet()))
            {
                setVisibility(feature, false);
            }
        }
    }
}
