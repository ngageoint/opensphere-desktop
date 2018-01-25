package io.opensphere.core.util.xml;

import java.util.Map;

import io.opensphere.core.util.JAXBable;
import io.opensphere.core.util.JAXBableXMLAdapter;
import io.opensphere.core.util.collections.WrappedMap;

/**
 * A map of strings to strings that can be used in a JAXB object.
 */
public class JAXBableStringMap extends WrappedMap<String, String> implements JAXBable<StringMap>
{
    /**
     * Constructor.
     *
     * @param map The wrapped map.
     */
    public JAXBableStringMap(Map<String, String> map)
    {
        super(map);
    }

    @Override
    public StringMap getWrapper()
    {
        return new StringMap(getMap());
    }

    /**
     * XML adapter that converts {@link JAXBableStringMap} to and from
     * {@link StringMap}.
     */
    public static class JAXBableStringMapAdapter extends JAXBableXMLAdapter<StringMap, JAXBableStringMap>
    {
    }
}
