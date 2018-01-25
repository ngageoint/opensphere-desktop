package io.opensphere.controlpanels.layers.base;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;

/** Test for {@link LayerSelectionUtilities}. */
public class LayerSelectionUtilitiesTest
{
    /**
     * Test for {@link LayerSelectionUtilities#filter(java.util.Collection)}.
     */
    @Test
    public void testFilter()
    {
        DataGroupInfo a = createDataGroup("A");
        DataGroupInfo b = createDataGroup("B");
        DataTypeInfo c = createDataType("C");
        DataTypeInfo d = createDataType("D");
        DataGroupInfo e = createDataGroup("E");
        DataTypeInfo f = createDataType("F");
        DataTypeInfo g = createDataType("G");

        a.addChild(b, this);
        b.addMember(c, this);
        b.addMember(d, this);
        a.addChild(e, this);
        a.addMember(f, this);
        a.addMember(g, this);

        List<?> input;
        List<?> expected;

        input = New.list(a, b, c, d, e, f, g);
        expected = New.list(a);
        Assert.assertEquals(expected, LayerSelectionUtilities.filter(input));

        input = New.list(b, d);
        expected = New.list(b);
        Assert.assertEquals(expected, LayerSelectionUtilities.filter(input));

        input = New.list(c, d, f, g);
        expected = New.list(c, d, f, g);
        Assert.assertEquals(expected, LayerSelectionUtilities.filter(input));

        input = New.list(b, c, d, e, f, g);
        expected = New.list(b, e, f, g);
        Assert.assertEquals(expected, LayerSelectionUtilities.filter(input));
    }

    /**
     * Creates a test data group.
     *
     * @param name the name
     * @return the data group
     */
    private static DataGroupInfo createDataGroup(String name)
    {
        return new DefaultDataGroupInfo(false, null, name, name);
    }

    /**
     * Creates a test data type.
     *
     * @param name the name
     * @return the data type
     */
    private static DataTypeInfo createDataType(String name)
    {
        return new DefaultDataTypeInfo(null, name, name, name, name, false);
    }
}
