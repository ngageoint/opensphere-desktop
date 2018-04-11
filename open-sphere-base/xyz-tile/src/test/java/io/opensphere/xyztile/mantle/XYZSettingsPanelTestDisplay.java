package io.opensphere.xyztile.mantle;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.opensphere.xyztile.model.XYZSettings;
import javafx.application.Platform;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;

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
        try
        {
            Platform.startup(() ->
            {
            });
        }
        catch (IllegalStateException e)
        {
            // Platform already started; ignore
        }

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
