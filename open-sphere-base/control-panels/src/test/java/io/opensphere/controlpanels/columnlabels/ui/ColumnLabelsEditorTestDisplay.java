package io.opensphere.controlpanels.columnlabels.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sun.javafx.application.PlatformImpl;

import io.opensphere.controlpanels.columnlabels.model.ColumnLabels;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link ColumnLabelsEditor}.
 */
public class ColumnLabelsEditorTestDisplay
{
    /**
     * Tests the UI.
     */
    @Test
    public void test()
    {
        PlatformImpl.startup(() ->
        {
        });

        ColumnLabels model = new ColumnLabels();
        model.setAlwaysShowLabels(true);

        ColumnLabelsEditor view = new ColumnLabelsEditor(model, New.list("test"));

        assertTrue(view.getAlwaysShowLabels().selectedProperty().get());

        view.getAlwaysShowLabels().selectedProperty().set(false);
        view.getAddButton().fire();

        assertFalse(model.isAlwaysShowLabels());
        assertEquals(1, model.getColumnsInLabel().size());
        assertEquals(1, view.getColumnLabels().getItems().size());
        assertEquals(model.getColumnsInLabel().get(0), view.getColumnLabels().getItems().get(0));

        assertNotNull(view.getAlwaysShowLabels().getTooltip());
    }
}
