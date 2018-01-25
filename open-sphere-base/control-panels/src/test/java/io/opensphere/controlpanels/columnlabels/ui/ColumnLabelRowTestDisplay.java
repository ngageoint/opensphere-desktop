package io.opensphere.controlpanels.columnlabels.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sun.javafx.application.PlatformImpl;

import io.opensphere.controlpanels.columnlabels.model.ColumnLabel;
import io.opensphere.controlpanels.columnlabels.model.ColumnLabels;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link ColumnLabelRowView}.
 */
public class ColumnLabelRowTestDisplay
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

        ColumnLabel model = new ColumnLabel();
        String column1 = "Column1";
        String column2 = "Column2";

        model.setColumn(column1);
        model.getAvailableColumns().addAll(column1, column2, "Column3");

        ColumnLabels mainModel = new ColumnLabels();
        mainModel.getColumnsInLabel().add(model);

        ColumnLabelRow view = new ColumnLabelRow(mainModel);
        view.updateItem(model, false);

        assertEquals(column1, view.getColumns().getValue());
        assertFalse(view.getShowColumnName().selectedProperty().get());
        assertEquals(New.list(column1, column2, "Column3"), view.getColumns().getItems());

        view.getColumns().setValue(column2);
        view.getShowColumnName().selectedProperty().set(true);

        assertEquals(column2, model.getColumn());
        assertTrue(model.isShowColumnName());

        assertNotNull(view.getMoveDownButton().getTooltip());
        assertNotNull(view.getMoveUpButton().getTooltip());
        assertNotNull(view.getRemoveButton().getTooltip());
        assertNotNull(view.getColumns().getTooltip());
        assertNotNull(view.getShowColumnName().getTooltip());
    }
}
