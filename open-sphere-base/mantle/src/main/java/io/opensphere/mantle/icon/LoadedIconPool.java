package io.opensphere.mantle.icon;

import io.opensphere.core.image.processor.ImageProcessor;

/**
 * The Interface LoadedIconPool.
 */
public interface LoadedIconPool
{
    /**
     * Gets the icon image provider for the specified record, will grab a pool
     * instance if one already exists.
     *
     * @param record the {@link IconRecord}
     * @return the {@link IconImageProvider}
     */
    IconImageProvider getIconImageProvider(IconRecord record);

    /**
     * Gets the icon image provider for the specified record, will grab a pool
     * instance if one already exists.
     *
     * @param record the {@link IconRecord}
     * @param imageProcessor the {@link ImageProcessor}
     * @return the {@link IconImageProvider}
     */
    IconImageProvider getIconImageProvider(IconRecord record, ImageProcessor imageProcessor);
}
