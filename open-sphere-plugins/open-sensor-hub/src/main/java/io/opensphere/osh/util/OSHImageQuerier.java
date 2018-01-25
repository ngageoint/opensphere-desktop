package io.opensphere.osh.util;

import io.opensphere.core.data.QueryException;
import io.opensphere.core.model.time.TimeSpan;

/**
 * Interface to an object that knows how to get a sensor's image at a given
 * time.
 */
public interface OSHImageQuerier
{
    /**
     * Queries a video image from the data registry.
     *
     * @param typeKey the data type key
     * @param timeSpan the time span to query
     * @return the image bytes
     * @throws QueryException if a problem occurred querying the data registry
     */
    byte[] queryImage(String typeKey, TimeSpan timeSpan) throws QueryException;
}
