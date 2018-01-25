package io.opensphere.mantle.data.element.event.consolidated;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;

/**
 * The Class ConsolidatedDataElementColorChangeEvent.
 */
public class ConsolidatedDataElementColorChangeEvent extends AbstractConsolidatedDataElementChangeEvent
{
    /** The color count. */
    private int myColorCount = -1;

    /** The colors. */
    private final TLongObjectHashMap<Color> myColorMap;

    /** The Opacity change only. */
    private final boolean myOpacityChangeOnly;

    /** Whether this event is intended only for external recipients (i.e. ignored my mantle). */
    private boolean myExternalOnly;

    /**
     * Instantiates a new color change consolidated data element change event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param colors the color map
     * @param opacityChangeOnly the opacity change only
     * @param source the source
     */
    public ConsolidatedDataElementColorChangeEvent(List<Long> regIds, Set<String> dataTypeKeys, TLongObjectHashMap<Color> colors,
            boolean opacityChangeOnly, Object source)
    {
        super(regIds, dataTypeKeys, source);
        myColorMap = colors;
        myOpacityChangeOnly = opacityChangeOnly;
    }

    /**
     * Instantiates a new color change consolidated data element change event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKey the data type key
     * @param colors the color map
     * @param opacityChangeOnly the opacity change only
     * @param source the source
     */
    public ConsolidatedDataElementColorChangeEvent(long regIds, String dataTypeKey, TLongObjectHashMap<Color> colors,
            boolean opacityChangeOnly, Object source)
    {
        super(regIds, dataTypeKey, source);
        myColorMap = colors;
        myOpacityChangeOnly = opacityChangeOnly;
    }

    /**
     * Instantiates a new color change consolidated data element change event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param colors the color map
     * @param opacityChangeOnly the opacity change only
     * @param source the source
     */
    public ConsolidatedDataElementColorChangeEvent(long[] regIds, Set<String> dataTypeKeys, TLongObjectHashMap<Color> colors,
            boolean opacityChangeOnly, Object source)
    {
        super(regIds, dataTypeKeys, source);
        myColorMap = colors;
        myOpacityChangeOnly = opacityChangeOnly;
    }

    /**
     * Gets the id to color map.
     *
     * @return the colors
     */
    public TLongObjectHashMap<Color> getIdToColorMap()
    {
        return myColorMap;
    }

    /**
     * Gets the unique color count.
     *
     * @return the unique color count
     */
    public int getUniqueColorCount()
    {
        if (myColorCount == -1)
        {
            myColorCount = 0;
            if (myColorMap != null)
            {
                UniqueColorCounter procedure = new UniqueColorCounter();
                myColorMap.forEachValue(procedure);
                myColorCount = procedure.getCount();
            }
        }
        return myColorCount;
    }

    /**
     * Checks if is opacity change only.
     *
     * @return true, if is opacity change only
     */
    public boolean isOpacityChangeOnly()
    {
        return myOpacityChangeOnly;
    }

    /**
     * Gets the externalOnly.
     *
     * @return the externalOnly
     */
    public boolean isExternalOnly()
    {
        return myExternalOnly;
    }

    /**
     * Sets the externalOnly.
     *
     * @param externalOnly the externalOnly
     */
    public void setExternalOnly(boolean externalOnly)
    {
        myExternalOnly = externalOnly;
    }

    /**
     * The Class UniqueAltitudeCounter.
     */
    private static class UniqueColorCounter implements TObjectProcedure<Color>
    {
        /** The my float set. */
        private final Set<Color> myUniqueSet;

        /**
         * Instantiates a new unique altitude.
         *
         */
        public UniqueColorCounter()
        {
            myUniqueSet = new HashSet<>();
        }

        @Override
        public boolean execute(Color object)
        {
            myUniqueSet.add(object);
            return true;
        }

        /**
         * Gets the count.
         *
         * @return the count
         */
        public int getCount()
        {
            return myUniqueSet == null ? 0 : myUniqueSet.size();
        }
    }
}
