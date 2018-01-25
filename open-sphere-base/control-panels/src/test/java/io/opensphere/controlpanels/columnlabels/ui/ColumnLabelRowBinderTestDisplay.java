package io.opensphere.controlpanels.columnlabels.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.sun.javafx.application.PlatformImpl;

import io.opensphere.controlpanels.columnlabels.model.ColumnLabel;
import io.opensphere.controlpanels.columnlabels.model.ColumnLabels;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link ColumnLabelRowBinder}.
 */
public class ColumnLabelRowBinderTestDisplay
{
    /**
     * Tests the binding.
     */
    @Test
    public void test()
    {
        PlatformImpl.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        ColumnLabelRowView view = createView(support);

        support.replayAll();

        ColumnLabel model = new ColumnLabel();
        String column1 = "Column1";
        String column2 = "Column2";
        String column3 = "Column3";

        model.setColumn(column1);
        model.getAvailableColumns().addAll(column1, column2, column3);

        ColumnLabels mainModel = new ColumnLabels();
        mainModel.getColumnsInLabel().add(model);
        mainModel.getColumnsInLabel().add(new ColumnLabel());

        ColumnLabelRowBinder binder = new ColumnLabelRowBinder(view, mainModel, model);

        assertEquals(column1, view.getColumns().getValue());
        assertFalse(view.getShowColumnName().selectedProperty().get());
        assertEquals(New.list(column1, column2, column3), view.getColumns().getItems());

        view.getColumns().setValue(column2);
        view.getShowColumnName().selectedProperty().set(true);

        assertEquals(column2, model.getColumn());
        assertTrue(model.isShowColumnName());

        model.setColumn(column3);
        model.setShowColumnName(false);
        model.getAvailableColumns().add("Column4");

        assertEquals(column3, view.getColumns().getValue());
        assertFalse(view.getShowColumnName().selectedProperty().get());
        assertEquals(New.list(column1, column2, column3, "Column4"), view.getColumns().getItems());

        view.getMoveDownButton().fire();

        assertEquals(2, mainModel.getColumnsInLabel().size());
        assertEquals(model, mainModel.getColumnsInLabel().get(1));

        view.getMoveUpButton().fire();

        assertEquals(2, mainModel.getColumnsInLabel().size());
        assertEquals(model, mainModel.getColumnsInLabel().get(0));

        view.getRemoveButton().fire();
        assertFalse(mainModel.getColumnsInLabel().contains(model));

        binder.close();

        assertEquals(column3, model.getColumn());

        view.getColumns().setValue(column2);
        view.getShowColumnName().selectedProperty().set(true);

        model.setColumn(column1);
        model.setShowColumnName(false);
        model.getAvailableColumns().add("Column5");

        assertEquals(column2, view.getColumns().getValue());
        assertTrue(view.getShowColumnName().selectedProperty().get());
        assertTrue(view.getColumns().getItems().isEmpty());

        assertEquals(column1, model.getColumn());
        assertFalse(model.isShowColumnName());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked view.
     *
     * @param support Used to create the mock.
     * @return The mocked view.
     */
    private ColumnLabelRowView createView(EasyMockSupport support)
    {
        ColumnLabelRowView view = support.createMock(ColumnLabelRowView.class);

        ComboBox<String> columns = new ComboBox<>();
        EasyMock.expect(view.getColumns()).andReturn(columns).atLeastOnce();

        CheckBox showColumnName = new CheckBox();
        EasyMock.expect(view.getShowColumnName()).andReturn(showColumnName).atLeastOnce();

        Button removeButton = new Button();
        EasyMock.expect(view.getRemoveButton()).andReturn(removeButton).atLeastOnce();

        Button moveUpButton = new Button();
        EasyMock.expect(view.getMoveUpButton()).andReturn(moveUpButton).atLeastOnce();

        Button moveDownButton = new Button();
        EasyMock.expect(view.getMoveDownButton()).andReturn(moveDownButton).atLeastOnce();

        return view;
    }
}
