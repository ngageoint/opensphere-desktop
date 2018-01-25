package io.opensphere.featureactions.editor.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.util.Collection;

import javax.swing.JMenuItem;

import org.junit.Test;

import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;

/**
 * Unit test for {@link FeatureActionsMenuProvider}.
 */
public class FeatureActionsMenuProviderTest
{
    /** The test layer id. */
    private static final String ourLayerId = "layerId";

    /** The test layer name. */
    private static final String ourLayerName = "We are layer";

    /** The test layer itself. */
    private static final DefaultDataTypeInfo ourLayer =
            new DefaultDataTypeInfo(null, "bla", ourLayerId, ourLayerName, ourLayerName, true);
    static
    {
        ourLayer.setBasicVisualizationInfo(new DefaultBasicVisualizationInfo(LoadsTo.STATIC, Color.RED, true));
    }

    /** Tests getting menu items for a single feature layer. */
    @Test
    public void test()
    {
        FeatureActionsMenuProvider menuProvider = new FeatureActionsMenuProvider(null, null);
        Collection<? extends Component> menuItems = menuProvider.menuItemsForLayer(ourLayer);

        assertEquals(1, menuItems.size());
        JMenuItem menuItem = (JMenuItem)menuItems.iterator().next();
        assertEquals("Feature Actions...", menuItem.getText());
    }

    /** Tests getting the single feature layer within a group. */
    @Test
    public void testGroup()
    {
        DataTypeInfo tileLayer = new DefaultDataTypeInfo(null, "bla", ourLayerId, ourLayerName, ourLayerName, true);
        DataGroupInfo group = createDataGroup(tileLayer, ourLayer);

        FeatureActionsMenuProvider menuProvider = new FeatureActionsMenuProvider(null, null);

        DataGroupContextKey key = new DataGroupContextKey(group, ourLayer);
        DataTypeInfo gotLayer = menuProvider.getDataTypes(key);
        assertEquals(gotLayer, ourLayer);
    }

    /**
     * Tests getting menu items for a single tile layer.
     */
    @Test
    public void testTileLayer()
    {
        DataTypeInfo tileLayer = new DefaultDataTypeInfo(null, "bla", ourLayerId, ourLayerName, ourLayerName, true);
        DataGroupInfo group = createDataGroup();

        FeatureActionsMenuProvider menuProvider = new FeatureActionsMenuProvider(null, null);

        DataGroupContextKey key = new DataGroupContextKey(group, tileLayer);
        Collection<? extends Component> menuItems = menuProvider.getMenuItems(ourLayerId, key);

        assertTrue(menuItems.isEmpty());
    }

    /**
     * Creates a data group info with the specified member data types.
     *
     * @param members The members of the group.
     * @return The group.
     */
    private DataGroupInfo createDataGroup(DataTypeInfo... members)
    {
        DefaultDataGroupInfo ret = new DefaultDataGroupInfo(false, null, "bla", "bla");
        for (DataTypeInfo inf :  members)
        {
            ret.addMember(inf, null);
        }
        return ret;
    }
}
