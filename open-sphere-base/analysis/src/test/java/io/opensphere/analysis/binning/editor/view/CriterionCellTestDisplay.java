package io.opensphere.analysis.binning.editor.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import javafx.application.Platform;

import io.opensphere.analysis.binning.criteria.BinCriteria;
import io.opensphere.analysis.binning.criteria.BinCriteriaElement;
import io.opensphere.analysis.binning.criteria.RangeCriteria;
import io.opensphere.analysis.binning.criteria.UniqueCriteria;
import io.opensphere.analysis.binning.editor.model.BinCriteriaModel;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.lang.Pair;

/**
 * Unit test for {@link CriterionCell}.
 */
public class CriterionCellTestDisplay
{
    /**
     * The number field name.
     */
    private static final String ourNumberField = "numberField";

    /**
     * The string field name.
     */
    private static final String ourStringField = "stringField";

    /**
     * The second string field.
     */
    private static final String ourStringField2 = "stringField2";

    /**
     * Tests editing an existing criteria.
     */
    @Test
    public void testEdit()
    {
        Platform.startup(() ->
        {
        });

        BinCriteriaModel model = createMainModel();

        BinCriteriaElement element = new BinCriteriaElement();
        element.setField(ourStringField);
        element.setCriteriaType(new UniqueCriteria());

        CriterionCell view = new CriterionCell(model);
        view.updateItem(element, true);

        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });
        assertEquals(1, view.getBinTypeBox().getItems().size());
        assertEquals("Unique", view.getBinTypeBox().getItems().get(0));

        assertEquals(ourStringField, view.getFieldBox().getValue());
        assertEquals("Unique", view.getBinTypeBox().getValue());

        assertTrue(view.getFieldBox().getItems().contains(ourNumberField));
        assertTrue(view.getFieldBox().getItems().contains(ourStringField));
        assertTrue(view.getFieldBox().getItems().contains(ourStringField2));

        assertFalse(view.getTolerance().isVisible());

        view.getFieldBox().setValue(ourNumberField);
        assertTrue(view.getBinTypeBox().getItems().contains("Unique"));
        assertTrue(view.getBinTypeBox().getItems().contains("Range"));

        view.getBinTypeBox().setValue("Range");
        assertTrue(view.getTolerance().isVisible());
        view.getTolerance().setText("text");
        assertEquals(10d, ((RangeCriteria)element.getCriteriaType()).getBinWidth(), 0d);
        view.getTolerance().setText("17.1");

        assertEquals(ourNumberField, element.getField());
        assertEquals("Range", element.getCriteriaType().getCriteriaType());
        assertEquals(17.1, ((RangeCriteria)element.getCriteriaType()).getBinWidth(), 0d);
    }

    /**
     * Tests creating a new criteria.
     */
    @Test
    public void testNew()
    {
        Platform.startup(() ->
        {
        });

        BinCriteriaModel model = createMainModel();

        BinCriteriaElement element = new BinCriteriaElement();

        CriterionCell view = new CriterionCell(model);
        view.updateItem(element, true);

        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });
        view.getFieldBox().setValue(ourStringField);
        assertEquals(1, view.getBinTypeBox().getItems().size());
        assertEquals("Unique", view.getBinTypeBox().getItems().get(0));

        assertEquals(ourStringField, element.getField());
        assertEquals("Unique", element.getCriteriaType().getCriteriaType());

        assertTrue(view.getFieldBox().getItems().contains(ourNumberField));
        assertTrue(view.getFieldBox().getItems().contains(ourStringField));
        assertTrue(view.getFieldBox().getItems().contains(ourStringField2));

        assertFalse(view.getTolerance().isVisible());
    }

    /**
     * Tests removing the criteria.
     */
    @Test
    public void testRemove()
    {
        Platform.startup(() ->
        {
        });

        BinCriteriaModel model = createMainModel();

        BinCriteriaElement element = new BinCriteriaElement();
        element.setField(ourStringField);
        element.setCriteriaType(new UniqueCriteria());

        model.getCriteria().getCriterias().add(element);

        CriterionCell view = new CriterionCell(model);
        view.updateItem(element, true);
        view.getRemoveButton().fire();

        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });
        assertEquals(1, model.getCriteria().getCriterias().size());
    }

    /**
     * Creates the main model.
     *
     * @return The main test model.
     */
    private BinCriteriaModel createMainModel()
    {
        List<Pair<String, Class<?>>> fields = New.list();
        fields.add(new Pair<>(ourStringField, String.class));
        fields.add(new Pair<>(ourNumberField, Double.class));
        fields.add(new Pair<>(ourStringField2, String.class));

        BinCriteriaModel model = new BinCriteriaModel("iamid", fields, new BinCriteria());

        return model;
    }
}
