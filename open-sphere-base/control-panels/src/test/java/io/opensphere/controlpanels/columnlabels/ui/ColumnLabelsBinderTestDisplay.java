package io.opensphere.controlpanels.columnlabels.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import javafx.application.Platform;

import io.opensphere.controlpanels.columnlabels.model.ColumnLabel;
import io.opensphere.controlpanels.columnlabels.model.ColumnLabels;

/**
 * Unit test for {@link ColumnLabelsBinder}.
 */
public class ColumnLabelsBinderTestDisplay
{
    /**
     * Tests when the user clicks add.
     */
    @Test
    public void testCreate()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        ColumnLabelsView view = createView(support);

        support.replayAll();

        ColumnLabels model = new ColumnLabels();
        model.setAlwaysShowLabels(true);
        ColumnLabelsBinder binder = new ColumnLabelsBinder(view, model);

        assertTrue(view.getAlwaysShowLabels().selectedProperty().get());

        view.getAlwaysShowLabels().selectedProperty().set(false);
        view.getAddButton().fire();

        assertFalse(model.isAlwaysShowLabels());
        assertEquals(1, model.getColumnsInLabel().size());
        assertEquals(1, view.getColumnLabels().getItems().size());
        assertEquals(model.getColumnsInLabel().get(0), view.getColumnLabels().getItems().get(0));

        model.setAlwaysShowLabels(true);
        assertTrue(view.getAlwaysShowLabels().selectedProperty().get());

        binder.close();

        view.getAlwaysShowLabels().selectedProperty().set(false);
        view.getAddButton().fire();

        assertTrue(model.isAlwaysShowLabels());
        assertEquals(1, model.getColumnsInLabel().size());
        assertEquals(0, view.getColumnLabels().getItems().size());

        support.verifyAll();
    }

    /**
     * Tests when the user deletes a label.
     */
    @Test
    public void testDelete()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        ColumnLabelsView view = createView(support);

        support.replayAll();

        ColumnLabels model = new ColumnLabels();
        ColumnLabel label = new ColumnLabel();
        label.setColumn("column1");
        model.getColumnsInLabel().add(label);

        ColumnLabelsBinder binder = new ColumnLabelsBinder(view, model);

        assertEquals(1, view.getColumnLabels().getItems().size());
        assertEquals(label, view.getColumnLabels().getItems().get(0));

        model.getColumnsInLabel().remove(0);

        assertEquals(0, view.getColumnLabels().getItems().size());

        binder.close();

        model.getColumnsInLabel().add(label);

        assertEquals(0, view.getColumnLabels().getItems().size());

        support.verifyAll();
    }

    /**
     * Tests when the user moves a label up or down.
     */
    @Test
    public void testMove()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        ColumnLabelsView view = createView(support);

        support.replayAll();

        ColumnLabels model = new ColumnLabels();
        ColumnLabel label = new ColumnLabel();
        label.setColumn("column1");
        model.getColumnsInLabel().add(label);

        ColumnLabel label2 = new ColumnLabel();
        label2.setColumn("column2");
        model.getColumnsInLabel().add(label2);

        ColumnLabel label3 = new ColumnLabel();
        label3.setColumn("column3");
        model.getColumnsInLabel().add(label3);

        ColumnLabelsBinder binder = new ColumnLabelsBinder(view, model);

        assertEquals(3, view.getColumnLabels().getItems().size());
        assertEquals(label, view.getColumnLabels().getItems().get(0));
        assertEquals(label2, view.getColumnLabels().getItems().get(1));
        assertEquals(label3, view.getColumnLabels().getItems().get(2));

        model.getColumnsInLabel().remove(0);
        model.getColumnsInLabel().add(1, label);

        assertEquals(3, view.getColumnLabels().getItems().size());
        assertEquals(label2, view.getColumnLabels().getItems().get(0));
        assertEquals(label, view.getColumnLabels().getItems().get(1));
        assertEquals(label3, view.getColumnLabels().getItems().get(2));

        binder.close();

        support.verifyAll();
    }

    /**
     * Creates an easy mocked view.
     *
     * @param support Used to create the mock.
     * @return The mocked view.
     */
    private ColumnLabelsView createView(EasyMockSupport support)
    {
        ColumnLabelsView view = support.createMock(ColumnLabelsView.class);

        Button addButton = new Button();
        EasyMock.expect(view.getAddButton()).andReturn(addButton).atLeastOnce();

        ListView<ColumnLabel> listView = new ListView<>();
        EasyMock.expect(view.getColumnLabels()).andReturn(listView).atLeastOnce();

        CheckBox alwaysShow = new CheckBox();
        EasyMock.expect(view.getAlwaysShowLabels()).andReturn(alwaysShow).atLeastOnce();

        return view;
    }
}
