package io.opensphere.mantle.data.element.event.consolidated;

import java.util.List;
import java.util.Set;

/**
 * The Class ConsolidatedDataElementMetadataValueChangeEvent.
 */
public class ConsolidatedDataElementMetadataValueChangeEvent extends AbstractConsolidatedDataElementChangeEvent
{
    /**
     * Instantiates a new meta data value change consolidated data element
     * change event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param source the source
     */
    public ConsolidatedDataElementMetadataValueChangeEvent(List<Long> regIds, Set<String> dataTypeKeys, Object source)
    {
        super(regIds, dataTypeKeys, source);
    }

    /**
     * Instantiates a new meta data value change consolidated data element
     * change event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKey the data type key
     * @param source the source
     */
    public ConsolidatedDataElementMetadataValueChangeEvent(long regIds, String dataTypeKey, Object source)
    {
        super(regIds, dataTypeKey, source);
    }

    /**
     * Instantiates a new meta data value change consolidated data element
     * change event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param source the source
     */
    public ConsolidatedDataElementMetadataValueChangeEvent(long[] regIds, Set<String> dataTypeKeys, Object source)
    {
        super(regIds, dataTypeKeys, source);
    }

    @Override
    public String getDescription()
    {
        return "Consolidated Data Element MetaDataValue Change Event";
    }
}
