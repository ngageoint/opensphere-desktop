package io.opensphere.kml.mantle.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.collections.New;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.mantle.data.DataTypeInfo;

/** Base class for KML transformers. */
public abstract class AbstractKMLTransformer extends DefaultTransformer implements Service
{
    /** The event listener service. */
    private final EventListenerService myListenerService;

    /** A map of data type keys to features. */
    private final Map<String, List<KMLFeature>> myDataTypeKeyToFeatureMap = Collections
            .synchronizedMap(New.<String, List<KMLFeature>>map());

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public AbstractKMLTransformer(Toolbox toolbox)
    {
        super(toolbox.getDataRegistry());
        myListenerService = new EventListenerService(toolbox.getEventManager(), 5);
    }

    @Override
    public void open()
    {
        super.open();
        myListenerService.open();
    }

    @Override
    public void close()
    {
        myListenerService.close();
        super.close();
    }

    /**
     * Updates the visibility of all features for the data type.
     *
     * @param dataType The data type
     */
    public void updateVisibility(DataTypeInfo dataType)
    {
        List<KMLFeature> features = myDataTypeKeyToFeatureMap.get(dataType.getTypeKey());
        if (features != null)
        {
            updateVisibility(features);
        }
    }

    /**
     * Updates the visibility of the given features.
     *
     * @param features The features
     */
    public void updateVisibility(Collection<? extends KMLFeature> features)
    {
        for (KMLFeature feature : features)
        {
            setVisibility(feature, KMLMantleUtilities.isFeatureVisible(feature));
        }
    }

    /**
     * Reloads the data type.
     *
     * @param dataType the data type
     */
    public void reload(DataTypeInfo dataType)
    {
        List<KMLFeature> features = myDataTypeKeyToFeatureMap.get(dataType.getTypeKey());
        if (features != null)
        {
            // Make a copy of the list so it doesn't get wiped out in the remove
            features = New.list(features);
            removeFeatures(features, dataType.getTypeKey());
            addFeatures(features, dataType);
        }
    }

    /**
     * Adds features to the transformer.
     *
     * @param features The features
     * @param dataType The data type
     */
    public abstract void addFeatures(Collection<? extends KMLFeature> features, DataTypeInfo dataType);

    /**
     * Removes features from the transformer.
     *
     * @param features The features
     * @param dataTypeKey The data type key
     */
    public abstract void removeFeatures(Collection<? extends KMLFeature> features, String dataTypeKey);

    /**
     * Sets the opacity of the features with the given DataTypeInfo.
     *
     * @param dataTypeInfo The data type info
     * @param opacity the opacity to set
     */
    public abstract void setOpacity(DataTypeInfo dataTypeInfo, int opacity);

    /**
     * Sets the visibility of the given feature.
     *
     * @param feature The feature
     * @param isVisible Whether the feature is to be visible
     */
    protected abstract void setVisibility(KMLFeature feature, boolean isVisible);

    /**
     * Gets the listenerService.
     *
     * @return the listenerService
     */
    protected final EventListenerService getListenerService()
    {
        return myListenerService;
    }

    /**
     * Gets the dataTypeKeyToFeatureMap.
     *
     * @return the dataTypeKeyToFeatureMap
     */
    protected final Map<String, List<KMLFeature>> getDataTypeKeyToFeatureMap()
    {
        return myDataTypeKeyToFeatureMap;
    }
}
