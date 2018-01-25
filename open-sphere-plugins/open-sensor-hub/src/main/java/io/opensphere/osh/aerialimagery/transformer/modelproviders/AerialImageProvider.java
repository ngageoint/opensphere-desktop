package io.opensphere.osh.aerialimagery.transformer.modelproviders;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import io.opensphere.core.data.QueryException;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;
import io.opensphere.osh.aerialimagery.model.PlatformMetadataAndImage;
import io.opensphere.osh.util.OSHImageQuerier;

/**
 * Retrieves an image for a given aerial imagery layer for the specified time.
 */
public class AerialImageProvider implements ModelProvider
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(AerialImageProvider.class);

    /**
     * The querier to get the image.
     */
    private final OSHImageQuerier myQuerier;

    /**
     * Constructs a new image provider.
     *
     * @param querier The image querier.
     */
    public AerialImageProvider(OSHImageQuerier querier)
    {
        myQuerier = querier;
    }

    @Override
    public PlatformMetadata getModel(DataTypeInfo dataType, DataTypeInfo videoLayer, long time, PlatformMetadata previousModel)
    {
        PlatformMetadata metadata = previousModel;

        if (videoLayer != null && metadata != null)
        {
            TimeSpan timeSpan = TimeSpan.get(time, Milliseconds.ONE);
            try
            {
                byte[] bytes = myQuerier.queryImage(videoLayer.getTypeKey(), timeSpan);
                if (bytes != null)
                {
                    metadata = new PlatformMetadataAndImage(metadata, ByteBuffer.wrap(bytes));
                }
            }
            catch (QueryException e)
            {
                LOGGER.error(e, e);
            }
        }

        return metadata;
    }
}
