package io.opensphere.mantle.data.element.event.consolidated;

import java.util.List;
import java.util.Set;

import gnu.trove.map.hash.TLongFloatHashMap;
import gnu.trove.procedure.TFloatProcedure;
import gnu.trove.set.hash.TFloatHashSet;

/**
 * The Class ConsolidatedDataElementAltitudeChangeEvent.
 */
public class ConsolidatedDataElementAltitudeChangeEvent extends AbstractConsolidatedDataElementChangeEvent
{
    /** The alt count. */
    private int myAltCount = -1;

    /** The id to alt map. */
    private final TLongFloatHashMap myIdToAltMap;

    /**
     * Instantiates a new altitude change consolidated data element change
     * event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param idToAltMap a map that maps id to the altitude
     * @param source the source
     */
    public ConsolidatedDataElementAltitudeChangeEvent(List<Long> regIds, Set<String> dataTypeKeys, TLongFloatHashMap idToAltMap,
            Object source)
    {
        super(regIds, dataTypeKeys, source);
        myIdToAltMap = idToAltMap;
    }

    /**
     * Instantiates a new altitude change consolidated data element change
     * event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKey the data type key
     * @param idToAltMap a map that maps id to the altitude
     * @param source the source
     */
    public ConsolidatedDataElementAltitudeChangeEvent(long regIds, String dataTypeKey, TLongFloatHashMap idToAltMap,
            Object source)
    {
        super(regIds, dataTypeKey, source);
        myIdToAltMap = idToAltMap;
    }

    /**
     * Instantiates a new altitude change consolidated data element change
     * event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param idToAltMap a map that maps id to the altitude
     * @param source the source
     */
    public ConsolidatedDataElementAltitudeChangeEvent(long[] regIds, Set<String> dataTypeKeys, TLongFloatHashMap idToAltMap,
            Object source)
    {
        super(regIds, dataTypeKeys, source);
        myIdToAltMap = idToAltMap;
    }

    @Override
    public String getDescription()
    {
        return "Consolidated Data Element Altitude Change Event";
    }

    /**
     * Gets the id to color map.
     *
     * @return the colors
     */
    public TLongFloatHashMap getIdToAltitudeMap()
    {
        return myIdToAltMap;
    }

    /**
     * Gets the unique color count.
     *
     * @return the unique color count
     */
    public int getUniqueAltitudeCount()
    {
        if (myAltCount == -1)
        {
            myAltCount = 0;
            if (myIdToAltMap != null)
            {
                UniqueAltitudeCounter procedure = new UniqueAltitudeCounter();
                myIdToAltMap.forEachValue(procedure);
                myAltCount = procedure.getCount();
            }
        }
        return myAltCount;
    }

    /**
     * The Class UniqueAltitudeCounter.
     */
    private static class UniqueAltitudeCounter implements TFloatProcedure
    {
        /** The my float set. */
        private final TFloatHashSet myFloatSet;

        /**
         * Instantiates a new unique altitude counter.
         *
         */
        public UniqueAltitudeCounter()
        {
            myFloatSet = new TFloatHashSet();
        }

        @Override
        public boolean execute(float value)
        {
            myFloatSet.add(value);
            return true;
        }

        /**
         * Gets the count.
         *
         * @return the count
         */
        public int getCount()
        {
            return myFloatSet == null ? 0 : myFloatSet.size();
        }
    }
}
