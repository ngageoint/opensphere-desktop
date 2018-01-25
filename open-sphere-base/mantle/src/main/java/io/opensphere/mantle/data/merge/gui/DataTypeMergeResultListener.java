package io.opensphere.mantle.data.merge.gui;

import io.opensphere.mantle.data.merge.DataTypeMergeMap;

/**
 * The listener interface DataTypeMergeResultListener.
 */
public interface DataTypeMergeResultListener
{
    /**
     * Merge cancelled.
     */
    void mergeCancelled();

    /**
     * Merge complete.
     *
     * @param map the map
     */
    void mergeComplete(DataTypeMergeMap map);
}
