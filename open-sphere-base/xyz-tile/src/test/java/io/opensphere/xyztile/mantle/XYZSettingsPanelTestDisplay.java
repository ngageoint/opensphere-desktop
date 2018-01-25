package io.opensphere.xyztile.mantle;

import static org.junit.Assert.assertEquals;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;

import org.junit.Test;

import com.sun.javafx.application.PlatformImpl;

import io.opensphere.xyztile.model.XYZSettings;

/**
 * Unit test for {@link XYZSettingsPanel}.
 */
public class XYZSettingsPanelTestDisplay
{
    /**
     * Tests the panel.
     */
    @Test
    public void test()
    {
        PlatformImpl.startup(() ->
        {
        });

        XYZSettings settings = new XYZSettings();
        settings.setMaxZoomLevelDefault(19);
        settings.setMaxZoomLevelCurrent(17);
        settings.setMinZoomLevel(2);

        XYZSettingsPanel panel = new XYZSettingsPanel(settings);
        Spinner<Integer> spinner = panel.getMaxZoomSpinner();

        assertEquals(17, spinner.getValue().intValue());
        assertEquals(2, ((IntegerSpinnerValueFactory)spinner.getValueFactory()).getMin());
        assertEquals(19, ((IntegerSpinnerValueFactory)spinner.getValueFactory()).getMax());

        spinner.getValueFactory().setValue(Integer.valueOf(14));

        assertEquals(14, settings.getMaxZoomLevelCurrent());
    }
}
