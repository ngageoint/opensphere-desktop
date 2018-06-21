package io.opensphere.kml.mantle.controller;

import java.util.Collection;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoLoadsToChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;

/**
 * Handles geometries that Mantle can't, such as labels and ground/screen
 * overlays.
 */
@ThreadSafe
public class KMLTransformer extends AbstractKMLTransformer
{
    /** The transformers. */
    private final List<AbstractKMLTransformer> myTransformers;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param featureIdCache the feature to id mapping
     */
    public KMLTransformer(Toolbox toolbox, KMLFeatureIdCache featureIdCache)
    {
        super(toolbox);

        myTransformers = New.list(new LabelTransformer(toolbox, featureIdCache), new OverlayTransformer(toolbox),
                new ModelTransformer(toolbox));

        for (AbstractKMLTransformer transformer : myTransformers)
        {
            getListenerService().addService(transformer);
        }
        getListenerService().bindEvent(DataTypeVisibilityChangeEvent.class, this::handleDataTypeVisibilityChange);
        getListenerService().bindEvent(DataTypeInfoLoadsToChangeEvent.class, this::handleDataTypeInfoLoadsToChange);
        getListenerService().bindEvent(DataTypeInfoColorChangeEvent.class, this::handleDataTypeInfoColorChange);
    }

    @Override
    public void addSubscriber(GenericSubscriber<Geometry> receiver)
    {
        for (AbstractKMLTransformer transformer : myTransformers)
        {
            transformer.addSubscriber(receiver);
        }
    }

    @Override
    public void removeSubscriber(GenericSubscriber<Geometry> receiver)
    {
        for (AbstractKMLTransformer transformer : myTransformers)
        {
            transformer.removeSubscriber(receiver);
        }
    }

    @Override
    public void addFeatures(Collection<? extends KMLFeature> features, DataTypeInfo dataType)
    {
        for (AbstractKMLTransformer transformer : myTransformers)
        {
            transformer.addFeatures(features, dataType);
        }
    }

    @Override
    public void removeFeatures(Collection<? extends KMLFeature> features, String dataTypeKey)
    {
        for (AbstractKMLTransformer transformer : myTransformers)
        {
            transformer.removeFeatures(features, dataTypeKey);
        }
    }

    @Override
    public void setOpacity(DataTypeInfo dataTypeInfo, int opacity)
    {
        for (AbstractKMLTransformer transformer : myTransformers)
        {
            transformer.setOpacity(dataTypeInfo, opacity);
        }
    }

    @Override
    public void updateVisibility(Collection<? extends KMLFeature> features)
    {
        for (AbstractKMLTransformer transformer : myTransformers)
        {
            transformer.updateVisibility(features);
        }
    }

    @Override
    public void updateVisibility(DataTypeInfo dataType)
    {
        for (AbstractKMLTransformer transformer : myTransformers)
        {
            transformer.updateVisibility(dataType);
        }
    }

    @Override
    public void reload(DataTypeInfo dataType)
    {
        for (AbstractKMLTransformer transformer : myTransformers)
        {
            transformer.reload(dataType);
        }
    }

    @Override
    protected void setVisibility(KMLFeature feature, boolean isVisible)
    {
        // Not needed because the updateVisibility methods delegate directly to
        // each transformer
    }

    /**
     * Handles a DataTypeVisibilityChangeEvent.
     *
     * @param event the event
     */
    private void handleDataTypeVisibilityChange(DataTypeVisibilityChangeEvent event)
    {
        final DataTypeInfo dataType = event.getDataTypeInfo();
        if (KMLMantleUtilities.KML.equals(dataType.getTypeName()))
        {
            ThreadUtilities.runBackground(() -> updateVisibility(dataType));
        }
    }

    /**
     * Handles a DataTypeInfoLoadsToChangeEvent.
     *
     * @param event the event
     */
    private void handleDataTypeInfoLoadsToChange(DataTypeInfoLoadsToChangeEvent event)
    {
        final DataTypeInfo dataType = event.getDataTypeInfo();
        if (KMLMantleUtilities.KML.equals(dataType.getTypeName()))
        {
            ThreadUtilities.runBackground(() -> reload(dataType));
        }
    }

    /**
     * Handles a DataTypeInfoColorChangeEvent.
     *
     * @param event the event
     */
    private void handleDataTypeInfoColorChange(DataTypeInfoColorChangeEvent event)
    {
        final DataTypeInfo dataType = event.getDataTypeInfo();
        if (KMLMantleUtilities.KML.equals(dataType.getTypeName()))
        {
            final int alpha = event.getColor().getAlpha();
            ThreadUtilities.runBackground(() -> setOpacity(dataType, alpha));
        }
    }
}
