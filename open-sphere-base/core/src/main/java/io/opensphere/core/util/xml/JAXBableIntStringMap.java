package io.opensphere.core.util.xml;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import io.opensphere.core.util.JAXBable;
import io.opensphere.core.util.JAXBableXMLAdapter;

/**
 * A map of strings to strings that can be used in a JAXB object.
 */
public class JAXBableIntStringMap implements JAXBable<IntStringMap>
{
    /** The map wrapped by this class. */
    private final TIntObjectMap<String> myMap;

    /**
     * Constructor.
     *
     * @param map The wrapped map.
     */
    public JAXBableIntStringMap(TIntObjectMap<String> map)
    {
        myMap = new TIntObjectHashMap<>(map);
    }

    /**
     * Get the map wrapped by this class.
     *
     * @return the map wrapped by this class.
     */
    public TIntObjectMap<String> getMap()
    {
        return myMap;
    }

    @Override
    public IntStringMap getWrapper()
    {
        return new IntStringMap(myMap);
    }

    /**
     * XML adapter that converts {@link JAXBableIntStringMap} to and from
     * {@link IntStringMap}.
     */
    public static class JAXBableIntStringMapAdapter extends JAXBableXMLAdapter<IntStringMap, JAXBableIntStringMap>
    {
    }
}
