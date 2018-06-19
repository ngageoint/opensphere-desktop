package io.opensphere.mantle.infinity;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;

/** Tests for {@link InfinityUtilities}. */
public class InfinityUtilitiesTest
{
    /** Test for {@link InfinityUtilities#getTagValue(String, DataTypeInfo)}. */
    @Test
    public void testGetTagValue()
    {
        DataTypeInfo dataType = new DefaultDataTypeInfo(null, null, "typeKey", null, null, true);
        dataType.addTag("bubba=gump", null);
        dataType.addTag(".ben=jerrys", null);
        Assert.assertEquals("gump", InfinityUtilities.getTagValue("bubba", dataType));
        Assert.assertEquals("jerrys", InfinityUtilities.getTagValue(".ben", dataType));
        Assert.assertNull(InfinityUtilities.getTagValue("something", dataType));
    }

    /** Test for {@link InfinityUtilities#getUrl(DataTypeInfo)}. */
    @Test
    public void testGetUrl()
    {
        DataTypeInfo dataType = new DefaultDataTypeInfo(null, null, "typeKey", null, null, true);
        dataType.addTag(".es-url=http://google.com", null);
        dataType.addTag(".es-index=layer=layer1&index=index1", null);
        Assert.assertEquals("http://google.com?layer=layer1&index=index1", InfinityUtilities.getUrl(dataType));
    }
}
