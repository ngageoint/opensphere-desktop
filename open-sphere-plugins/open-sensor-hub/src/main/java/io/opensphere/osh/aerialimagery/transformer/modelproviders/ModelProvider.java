package io.opensphere.osh.aerialimagery.transformer.modelproviders;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;

/**
 * Interface to an object that will supply a model to be used to create
 * geometries from. The model will represent data from a given layer at the time
 * specified.
 */
public interface ModelProvider
{
    /**
     * Gets the metadata for the given uav layer and time.
     *
     * @param dataType The uav layer to get metadata for.
     * @param videoLayer The layer that contains the video data.
     * @param time The time to get the metadata for.
     * @param previousModel A previously created metadata in the model provider
     *            chain.
     * @return The metadata that is close to the time specified.
     */
    PlatformMetadata getModel(DataTypeInfo dataType, DataTypeInfo videoLayer, long time, PlatformMetadata previousModel);
}
