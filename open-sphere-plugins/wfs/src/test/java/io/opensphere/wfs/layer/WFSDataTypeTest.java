package io.opensphere.wfs.layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.server.state.StateConstants;
import io.opensphere.server.toolbox.LayerConfiguration;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.wfs.util.WFSTestToolbox;

/**
 * Test for {@link WFSDataType}.
 */
public class WFSDataTypeTest
{
    /** A Constant HOSTNAME. */
    private static final String HOSTNAME = "localhost";

    /** Type 1. */
    private static final String TYPE1 = "type1";

    /** Elevation key. */
    private static final String ELEVATION_KEY = "elevation";

    /** Altitude key. */
    private static final String ALTITUDE_KEY = "altitude";

    /** A test instantiation of the core toolbox. */
    private static final Toolbox TOOLBOX = WFSTestToolbox.getToolbox();

    /**
     * Test {@link WFSDataType#equals(Object)}.
     */
    @Test
    @SuppressWarnings("PMD.StringInstantiation")
    public void testEqualsObject()
    {
        String type1 = TYPE1;
        String type2 = new String(TYPE1);
        String type3 = "type3";
        Map<String, Class<?>> props = new HashMap<>();
        props.put(ALTITUDE_KEY, Double.class);

        assertEquals(getNewType(type1, props), getNewType(type1, props));
        assertEquals(getNewType(type1, props), getNewType(type2, props));
        assertEquals(getNewType(type2, props), getNewType(type1, props));
        assertFalse(getNewType(type1, props).equals(getNewType(type3, props)));
        assertFalse(getNewType(type3, props).equals(getNewType(type1, props)));
    }

    /**
     * Test {@link WFSDataType#getTypeKey()}.
     */
    @Test
    public void testGetKey()
    {
        String name = TYPE1;
        String key = HOSTNAME + ":" + name;
        assertEquals(key, getNewType(name, Collections.<String, Class<?>>emptyMap()).getTypeKey());
    }

    /**
     * Test {@link WFSDataType#getTypeName()}.
     */
    @Test
    public void testGetName()
    {
        String name = TYPE1;
        assertEquals(name, getNewType(name, Collections.<String, Class<?>>emptyMap()).getTypeName());
    }

    /**
     * Test {@link WFSDataType#getProperties()}.
     */
    @Test
    public void testGetProperties()
    {
        Map<String, Class<?>> expected = new HashMap<>();
        expected.put(ALTITUDE_KEY, Double.class);
        expected.put(ELEVATION_KEY, Double.class);
        Map<String, Class<?>> properties = getNewType("type", expected).getProperties();
        assertEquals(expected, properties);
    }

    /**
     * Test for {@link WFSDataType#hashCode()}.
     */
    @Test
    @SuppressWarnings("PMD.StringInstantiation")
    public void testHashCode()
    {
        String type1 = TYPE1;
        String type2 = new String(TYPE1);
        String type3 = "type3";
        Map<String, Class<?>> props = new HashMap<>();
        props.put(ALTITUDE_KEY, Double.class);

        assertEquals(getNewType(type1, props).hashCode(), getNewType(type1, props).hashCode());
        assertEquals(getNewType(type1, props).hashCode(), getNewType(type2, props).hashCode());
        assertEquals(getNewType(type2, props).hashCode(), getNewType(type1, props).hashCode());
        assertFalse(getNewType(type1, props).hashCode() == getNewType(type3, props).hashCode());
        assertFalse(getNewType(type3, props).hashCode() == getNewType(type1, props).hashCode());
    }

    /**
     * Test {@link WFSDataType#providerFiltersMetaData()}.
     */
    @Test
    public void testProviderFiltersMetaData()
    {
        String name = TYPE1;
        assertTrue(getNewType(name, Collections.<String, Class<?>>emptyMap()).providerFiltersMetaData());
    }

    /**
     * Test construction with null properties.
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testWFSTypeNullProperties()
    {
        String name = "type";
        String key = HOSTNAME + ":" + name;
        LayerConfiguration configuration = ServerToolboxUtils.getServerToolbox(TOOLBOX).getLayerConfigurationManager()
                .getConfigurationFromName(StateConstants.WFS_LAYER_TYPE);
        new WFSDataType(TOOLBOX, HOSTNAME, key, name, name, null, configuration);
    }

    /**
     * Test construction with null type.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWFSTypeNullType()
    {
        getNewType(null, Collections.<String, Class<?>>emptyMap());
    }

    /**
     * Gets a new type.
     *
     * @param name the type name
     * @param props the properties
     * @return the new type
     */
    private WFSDataType getNewType(String name, Map<String, Class<?>> props)
    {
        String key = HOSTNAME + ":" + name;
        DefaultMetaDataInfo mdi = new DefaultMetaDataInfo();
        if (props != null && !props.isEmpty())
        {
            for (Entry<String, Class<?>> prop : props.entrySet())
            {
                mdi.addKey(prop.getKey(), prop.getValue(), this);
            }
        }
        mdi.copyKeysToOriginalKeys();

        LayerConfiguration configuration = ServerToolboxUtils.getServerToolbox(TOOLBOX).getLayerConfigurationManager()
                .getConfigurationFromName(StateConstants.WFS_LAYER_TYPE);

        return new WFSDataType(TOOLBOX, HOSTNAME, key, name, name, mdi, configuration);
    }
}
