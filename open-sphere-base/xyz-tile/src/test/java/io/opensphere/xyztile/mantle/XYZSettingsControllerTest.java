package io.opensphere.xyztile.mantle;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.xyztile.model.Projection;
import io.opensphere.xyztile.model.XYZServerInfo;
import io.opensphere.xyztile.model.XYZSettings;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/**
 * Unit test for {@link XYZSettingsController}.
 */
public class XYZSettingsControllerTest
{
    /**
     * Verifies the controller saves changes.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        XYZSettings model = new XYZSettings();
        SettingsBroker saver = support.createMock(SettingsBroker.class);
        saver.saveSettings(EasyMock.eq(model));
        XYZTileLayerInfo layer = new XYZTileLayerInfo("theid", "A Name", Projection.EPSG_4326, 2, true, 4,
                new XYZServerInfo("serverName", "http://somehost"));

        support.replayAll();

        XYZSettingsController controller = new XYZSettingsController(saver, layer, model);
        assertEquals(18, layer.getMaxLevels());
        model.setMaxZoomLevelCurrent(12);
        assertEquals(12, layer.getMaxLevels());
        controller.close();
        model.setMaxZoomLevelCurrent(10);
        assertEquals(12, layer.getMaxLevels());
        assertEquals(5, model.getMinZoomLevel());

        support.verifyAll();
    }
}
