package io.opensphere.controlpanels.columnlabels.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import io.opensphere.controlpanels.columnlabels.model.ColumnLabel;
import io.opensphere.controlpanels.columnlabels.model.ColumnLabels;
import io.opensphere.core.util.collections.New;

/**
 * Unit tests the {@link ColumnsController}.
 */
public class ColumnsControllerTest
{
    /**
     * Tests the columns controller class.
     */
    @Test
    public void test()
    {
        List<String> columns = New.list("Name", "Description");
        ColumnLabels model = new ColumnLabels();

        ColumnsController controller = new ColumnsController(model, columns);

        ColumnLabel label1 = new ColumnLabel();
        model.getColumnsInLabel().add(label1);

        assertEquals(columns, label1.getAvailableColumns());
        assertEquals(columns.get(0), label1.getColumn());

        List<String> otherColumns = New.list("some other");
        ColumnLabel prePop = new ColumnLabel();
        prePop.getAvailableColumns().addAll(otherColumns);
        prePop.setColumn(otherColumns.get(0));
        model.getColumnsInLabel().add(prePop);

        assertEquals(otherColumns, prePop.getAvailableColumns());
        assertEquals(otherColumns.get(0), prePop.getColumn());
        model.getColumnsInLabel().remove(prePop);

        ColumnLabel label2 = new ColumnLabel();
        model.getColumnsInLabel().add(label2);

        assertEquals(columns, label2.getAvailableColumns());
        assertEquals(columns.get(1), label2.getColumn());

        ColumnLabel label3 = new ColumnLabel();
        model.getColumnsInLabel().add(label3);

        assertEquals(columns, label2.getAvailableColumns());
        assertEquals(columns.get(1), label2.getColumn());

        controller.close();

        ColumnLabel label4 = new ColumnLabel();
        model.getColumnsInLabel().add(label4);

        assertTrue(label4.getAvailableColumns().isEmpty());
        assertNull(label4.getColumn());
    }

    /**
     * Tests the columns controller class.
     */
    @Test
    public void testExisting()
    {
        List<String> columns = New.list("Name", "Description");
        ColumnLabels model = new ColumnLabels();
        ColumnLabel label1 = new ColumnLabel();
        label1.setColumn(columns.get(1));
        model.getColumnsInLabel().add(label1);

        ColumnsController controller = new ColumnsController(model, columns);
        controller.close();

        assertEquals(columns, label1.getAvailableColumns());
    }

    /**
     * Tests added a third column label and verifies the default is correctly
     * selected.
     */
    @Test
    public void testDefaultColumns()
    {
        List<String> columns = New.list("Name", "Description", "Lat", "Lon");
        ColumnLabels model = new ColumnLabels();

        ColumnLabel name = new ColumnLabel();
        name.setColumn(columns.get(0));
        model.getColumnsInLabel().add(name);

        ColumnLabel description = new ColumnLabel();
        description.setColumn(columns.get(1));
        model.getColumnsInLabel().add(description);

        ColumnsController controller = new ColumnsController(model, columns);

        ColumnLabel added = new ColumnLabel();
        model.getColumnsInLabel().add(added);

        assertEquals(columns.get(2), added.getColumn());

        controller.close();
    }
}
