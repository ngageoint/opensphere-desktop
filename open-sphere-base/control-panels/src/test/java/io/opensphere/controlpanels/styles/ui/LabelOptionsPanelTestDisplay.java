package io.opensphere.controlpanels.styles.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javafx.scene.paint.Color;

import org.junit.Test;

import com.sun.javafx.application.PlatformImpl;

import io.opensphere.controlpanels.columnlabels.model.ColumnLabel;
import io.opensphere.controlpanels.styles.controller.LabelOptionsController;
import io.opensphere.controlpanels.styles.model.LabelOptions;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link LabelOptionsPanel}.
 */
public class LabelOptionsPanelTestDisplay
{
    /**
     * The expected columns.
     */
    private static final List<String> expected = New.list("NAME", "DESCRIPTION",
            "LAT", "LON", "LAT_DMS",
            "LON_DMS", "MGRS", "SEMI_MAJOR",
            "SEMI_MINOR", "SEMI_MAJOR_UNITS",
            "SEMI_MINOR_UNITS", "ORIENTATION");

    /**
     * Tests the panel.
     */
    @Test
    public void test()
    {
        PlatformImpl.startup(() ->
        {
        });

        LabelOptions options = new LabelOptions();
        options.setColor(java.awt.Color.RED);
        options.setSize(12);

        LabelOptionsPanel view = new LabelOptionsPanel(options, new LabelOptionsController()
        {
            @Override
            public List<String> getColumns()
            {
                return expected;
            }
        }, true);

        assertEquals(Color.RED, view.getColorPicker().valueProperty().get());
        assertEquals(Integer.valueOf(12), view.getSizePicker().getValueFactory().getValue());

        view.getColorPicker().valueProperty().set(Color.BLUE);
        view.getSizePicker().getValueFactory().setValue(Integer.valueOf(15));

        assertEquals(java.awt.Color.BLUE, options.getColor());
        assertEquals(15, options.getSize());

        assertNotNull(view.getSizePicker().getTooltip());
        assertNotNull(view.getColorPicker().getTooltip());

        ColumnLabel label = new ColumnLabel();
        options.getColumnLabels().getColumnsInLabel().add(label);

        assertEquals(expected, label.getAvailableColumns());
    }
}
