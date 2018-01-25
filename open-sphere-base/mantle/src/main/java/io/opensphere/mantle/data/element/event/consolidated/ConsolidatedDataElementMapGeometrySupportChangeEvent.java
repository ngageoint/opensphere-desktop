package io.opensphere.mantle.data.element.event.consolidated;

import java.util.List;
import java.util.Set;

import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * The Class ConsolidatedDataElementMapGeometrySupportChangeEvent.
 */
public class ConsolidatedDataElementMapGeometrySupportChangeEvent extends AbstractConsolidatedDataElementChangeEvent
{
    /** The id to MapGeometrySupport map. */
    private final TLongObjectHashMap<MapGeometrySupport> myMGSMap;

    /**
     * Instantiates a new MapGeometrySupport change consolidated data element
     * change event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param idToMGSMap the registry id to {@link MapGeometrySupport} map
     * @param source the source
     */
    public ConsolidatedDataElementMapGeometrySupportChangeEvent(List<Long> regIds, Set<String> dataTypeKeys,
            TLongObjectHashMap<MapGeometrySupport> idToMGSMap, Object source)
    {
        super(regIds, dataTypeKeys, source);
        myMGSMap = idToMGSMap;
    }

    /**
     * Instantiates a new MapGeometrySupport change consolidated data element
     * change event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKey the data type key
     * @param idToMGSMap the registry id to {@link MapGeometrySupport} map
     * @param source the source
     */
    public ConsolidatedDataElementMapGeometrySupportChangeEvent(long regIds, String dataTypeKey,
            TLongObjectHashMap<MapGeometrySupport> idToMGSMap, Object source)
    {
        super(regIds, dataTypeKey, source);
        myMGSMap = idToMGSMap;
    }

    /**
     * Instantiates a new MapGeometrySupport change consolidated data element
     * change event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param idToMGSMap the registry id to {@link MapGeometrySupport} map
     * @param source the source
     */
    public ConsolidatedDataElementMapGeometrySupportChangeEvent(long[] regIds, Set<String> dataTypeKeys,
            TLongObjectHashMap<MapGeometrySupport> idToMGSMap, Object source)
    {
        super(regIds, dataTypeKeys, source);
        myMGSMap = idToMGSMap;
    }

    /**
     * Gets the id to {@link MapGeometrySupport} map.
     *
     * @return the mgs map
     */
    public TLongObjectHashMap<MapGeometrySupport> getIdToMapGeometrySupportMap()
    {
        return myMGSMap;
    }
}
