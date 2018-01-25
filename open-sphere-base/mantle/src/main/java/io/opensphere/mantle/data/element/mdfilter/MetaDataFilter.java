package io.opensphere.mantle.data.element.mdfilter;

import io.opensphere.mantle.data.element.DataElement;

/**
 * The Interface MetaDataFilter.
 */
public interface MetaDataFilter
{
    /**
     * Checks to see if the MetaDataFilter will accept a given
     * {@link DataElement}.
     *
     * @param de the de
     * @return true, if successful
     */
    boolean accepts(DataElement de);
}
