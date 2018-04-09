package io.opensphere.controlpanels.styles.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javafx.scene.paint.Color;

import org.junit.Test;

import javafx.application.Platform;

import io.opensphere.controlpanels.styles.model.StyleOptions;
import io.opensphere.controlpanels.styles.model.Styles;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link StyleOptionsPanel}.
 */
public class StyleOptionsPanelTestDispaly
{
    /**
     * Tests the {@link StyleOptionsPanel}.
     */
    @Test
    public void test()
    {
        Platform.startup(() ->
        {
        });

        StyleOptions model = new StyleOptions();

        model.setColor(java.awt.Color.RED);
        model.setSize(8);
        model.setStyle(Styles.POINT);

        StyleOptionsView view = new StyleOptionsPanel(model);

        assertEquals(Color.RED, view.getColorPicker().getValue());
        assertEquals(8, view.getSize().getValue(), 0d);
        assertEquals(5, view.getSize().getMin(), 0);
        assertEquals(50, view.getSize().getMax(), 0);
        assertEquals(Styles.POINT, view.getStylePicker().getValue());
        assertEquals(New.list(Styles.values()), view.getStylePicker().getItems());

        view.getColorPicker().setValue(Color.BLUE);
        view.getSize().setValue(5);
        view.getStylePicker().setValue(Styles.ELLIPSE);

        assertEquals(java.awt.Color.BLUE, model.getColor());
        assertEquals(5, model.getSize());
        assertEquals(Styles.ELLIPSE, model.getStyle());

        assertNotNull(view.getColorPicker().getTooltip());
        assertNotNull(view.getStylePicker().getTooltip());
        assertNotNull(view.getSize().getTooltip());
    }
}
