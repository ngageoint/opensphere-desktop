package io.opensphere.xyztile.mantle;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.xyztile.model.Projection;
import io.opensphere.xyztile.model.XYZDataTypeInfo;
import io.opensphere.xyztile.model.XYZServerInfo;
import io.opensphere.xyztile.model.XYZSettings;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/**
 * Unit test for {@link XYZDataGroupInfoAssistant}.
 */
public class XYZDataGroupInfoAssistantTestDisplay
{
    /**
     * The layer id used for tests.
     */
    private static final String ourLayerId = "iamlayerid";

    /**
     * Tests getting the settings ui.
     */
    @Test
    public void testGetSettingsUIComponent()
    {
        EasyMockSupport support = new EasyMockSupport();

        XYZTileLayerInfo layer = new XYZTileLayerInfo(ourLayerId, "A Name", Projection.EPSG_4326, 2, true, 4,
                new XYZServerInfo("serverName", "http://somehost"));
        SettingsBroker broker = createBroker(support, layer);
        DataGroupInfo dataGroup = createDataGroup(support, layer);

        support.replayAll();

        XYZDataGroupInfoAssistant assistant = new XYZDataGroupInfoAssistant(broker);

        XYZSettingsUI settings = (XYZSettingsUI)assistant.getSettingsUIComponent(null, dataGroup);
        assertEquals(layer, settings.getLayer());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link SettingsBroker}.
     *
     * @param support Used to create the mock.
     * @param layer The layer.
     * @return The mocked settings broker.
     */
    private SettingsBroker createBroker(EasyMockSupport support, XYZTileLayerInfo layer)
    {
        SettingsBroker broker = support.createMock(SettingsBroker.class);

        EasyMock.expect(broker.getSettings(EasyMock.eq(layer))).andReturn(new XYZSettings());

        return broker;
    }

    /**
     * Creates an easy mocked {@link DataGroupInfo}.
     *
     * @param support Used to create the mock.
     * @param layerInfo The layer info.
     * @return The mocked {@link DataGroupInfo}.
     */
    private DataGroupInfo createDataGroup(EasyMockSupport support, XYZTileLayerInfo layerInfo)
    {
        XYZDataTypeInfo xyzType = new XYZDataTypeInfo(null, layerInfo);
        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);

        DataGroupInfo group = support.createMock(DataGroupInfo.class);
        EasyMock.expect(group.getMembers(EasyMock.eq(false))).andReturn(New.set(dataType, xyzType));

        return group;
    }
}
