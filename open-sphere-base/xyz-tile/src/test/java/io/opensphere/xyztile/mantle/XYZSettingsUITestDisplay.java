package io.opensphere.xyztile.mantle;

import static org.junit.Assert.assertEquals;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.sun.javafx.application.PlatformImpl;

import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.xyztile.model.Projection;
import io.opensphere.xyztile.model.XYZServerInfo;
import io.opensphere.xyztile.model.XYZSettings;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/**
 * Unit test for {@link XYZSettingsUI}.
 */
public class XYZSettingsUITestDisplay
{
    /**
     * The layer id used for tests.
     */
    private static final String ourLayerId = "iamlayerid";

    /**
     * Tests the ui.
     */
    @Test
    public void test()
    {
        PlatformImpl.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        XYZTileLayerInfo layer = new XYZTileLayerInfo(ourLayerId, "A Name", Projection.EPSG_4326, 2, true, 4,
                new XYZServerInfo("serverName", "http://somehost"));

        XYZSettings settings = new XYZSettings();
        settings.setLayerId(layer.getName());
        settings.setMaxZoomLevelCurrent(16);
        settings.setMaxZoomLevelDefault(18);

        SettingsBroker broker = createBroker(support, layer, settings);

        support.replayAll();

        XYZSettingsUI ui = new XYZSettingsUI(layer, broker);
        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

        XYZSettingsPanel panel = ui.getSettingsPanel();
        Spinner<Integer> spinner = panel.getMaxZoomSpinner();

        assertEquals(16, spinner.getValue().intValue());
        assertEquals(5, ((IntegerSpinnerValueFactory)spinner.getValueFactory()).getMin());
        assertEquals(18, ((IntegerSpinnerValueFactory)spinner.getValueFactory()).getMax());

        spinner.getValueFactory().setValue(Integer.valueOf(14));

        assertEquals(14, settings.getMaxZoomLevelCurrent());

        ui.close();

        assertEquals(0, settings.countObservers());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link SettingsBroker}.
     *
     * @param support Used to create the mock.
     * @param layer The layer.
     * @param settings The settings to return.
     * @return The mocked settings broker.
     */
    private SettingsBroker createBroker(EasyMockSupport support, XYZTileLayerInfo layer, XYZSettings settings)
    {
        SettingsBroker broker = support.createMock(SettingsBroker.class);

        EasyMock.expect(broker.getSettings(EasyMock.eq(layer))).andReturn(settings);
        broker.saveSettings(EasyMock.eq(settings));

        return broker;
    }
}
