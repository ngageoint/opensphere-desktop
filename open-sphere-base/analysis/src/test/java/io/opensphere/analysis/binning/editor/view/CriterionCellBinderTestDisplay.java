package io.opensphere.analysis.binning.editor.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import io.opensphere.analysis.binning.criteria.BinCriteria;
import io.opensphere.analysis.binning.criteria.BinCriteriaElement;
import io.opensphere.analysis.binning.criteria.CriteriaTypeFactory;
import io.opensphere.analysis.binning.criteria.RangeCriteria;
import io.opensphere.analysis.binning.criteria.UniqueCriteria;
import io.opensphere.analysis.binning.editor.model.BinCriteriaModel;
import io.opensphere.analysis.binning.editor.model.CriterionModel;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Unit test for {@link CriterionCellBinder}.
 */
public class CriterionCellBinderTestDisplay
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

    /** Initializes the JavaFX platform. */
    @Before
    public void initialize()
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
    }

    /**
     * Tests when binder is closed.
     */
    @Test
    public void testClose()
    {
        EasyMockSupport support = new EasyMockSupport();

        CriterionCellView view = createView(support);
        BinCriteriaModel model = createMainModel();

        BinCriteriaElement element = new BinCriteriaElement();

        model.getCriteria().getCriterias().add(element);

        CriterionModel criterionModel = new CriterionModel(element);
        criterionModel.getBinTypes().addAll(CriteriaTypeFactory.getInstance().getAvailableTypes());

        support.replayAll();

        CriterionCellBinder binder = new CriterionCellBinder(view, criterionModel, model);

        view.getFieldBox().setValue(ourNumberField);
        view.getBinTypeBox().setValue(RangeCriteria.CRITERIA_TYPE);
        assertTrue(view.getTolerance().isVisible());
        assertTrue(view.getToleranceLabel().isVisible());
        view.getTolerance().setText("17.3");

        binder.close();

        view.getFieldBox().setValue(ourStringField);
        view.getBinTypeBox().setValue(UniqueCriteria.CRITERIA_TYPE);
        view.getRemoveButton().fire();
        model.getCriteria().getCriterias().add(new BinCriteriaElement());

        assertEquals(ourNumberField, element.getField());
        assertEquals(RangeCriteria.CRITERIA_TYPE, element.getCriteriaType().getCriteriaType());
        assertEquals(17.3, ((RangeCriteria)element.getCriteriaType()).getBinWidth(), 0d);
        assertFalse(view.getRemoveButton().isVisible());

        support.verifyAll();
    }

    /**
     * Tests the initial binding for a range bin type.
     */
    @Test
    public void testInitialDouble()
    {
        EasyMockSupport support = new EasyMockSupport();

        CriterionCellView view = createView(support);
        BinCriteriaModel model = createMainModel();

        BinCriteriaElement element = new BinCriteriaElement();
        element.setField(ourNumberField);
        RangeCriteria rangeCriteria = new RangeCriteria();
        rangeCriteria.setBinWidth(17);
        element.setCriteriaType(rangeCriteria);

        model.getCriteria().getCriterias().add(element);

        CriterionModel criterionModel = new CriterionModel(element);
        criterionModel.getBinTypes().addAll(CriteriaTypeFactory.getInstance().getAvailableTypes());

        support.replayAll();

        CriterionCellBinder binder = new CriterionCellBinder(view, criterionModel, model);

        assertEquals(ourNumberField, view.getFieldBox().getValue());
        assertEquals(RangeCriteria.CRITERIA_TYPE, view.getBinTypeBox().getValue());

        assertTrue(view.getFieldBox().getItems().contains(ourNumberField));
        assertTrue(view.getFieldBox().getItems().contains(ourStringField));
        assertTrue(view.getFieldBox().getItems().contains(ourStringField2));

        assertTrue(view.getBinTypeBox().getItems().contains(UniqueCriteria.CRITERIA_TYPE));
        assertTrue(view.getBinTypeBox().getItems().contains(RangeCriteria.CRITERIA_TYPE));

        assertTrue(view.getTolerance().isVisible());
        assertTrue(view.getToleranceLabel().isVisible());
        assertEquals("17", view.getTolerance().getText());

        binder.close();

        support.verifyAll();
    }

    /**
     * Tests the initial binding for a range bin type and a unique bin type.
     */
    @Test
    public void testInitialDoubleAndString()
    {
        EasyMockSupport support = new EasyMockSupport();

        CriterionCellView view = createView(support);
        CriterionCellView view2 = createView(support);
        BinCriteriaModel model = createMainModel();

        BinCriteriaElement element = new BinCriteriaElement();
        element.setField(ourNumberField);
        RangeCriteria rangeCriteria = new RangeCriteria();
        rangeCriteria.setBinWidth(17);
        element.setCriteriaType(rangeCriteria);

        model.getCriteria().getCriterias().add(element);

        BinCriteriaElement stringElement = new BinCriteriaElement();
        element.setField(ourStringField);
        element.setCriteriaType(new UniqueCriteria());

        model.getCriteria().getCriterias().add(stringElement);

        CriterionModel criterionModel = new CriterionModel(element);
        criterionModel.getBinTypes().addAll(CriteriaTypeFactory.getInstance().getAvailableTypes());

        CriterionModel stringCriterionModel = new CriterionModel(stringElement);
        stringCriterionModel.getBinTypes().addAll(CriteriaTypeFactory.getInstance().getAvailableTypes());

        support.replayAll();

        CriterionCellBinder binder = new CriterionCellBinder(view, criterionModel, model);
        CriterionCellBinder binder2 = new CriterionCellBinder(view2, stringCriterionModel, model);

        assertTrue(view.getRemoveButton().isVisible());
        assertTrue(view2.getRemoveButton().isVisible());

        binder2.close();

        model.getCriteria().getCriterias().remove(1);

        assertFalse(view.getRemoveButton().isVisible());
        binder.close();

        support.verifyAll();
    }

    /**
     * Tests the initial binding for a Unique bin type.
     */
    @Test
    public void testInitialString()
    {
        EasyMockSupport support = new EasyMockSupport();

        CriterionCellView view = createView(support);
        BinCriteriaModel model = createMainModel();

        BinCriteriaElement element = new BinCriteriaElement();
        element.setField(ourStringField);
        element.setCriteriaType(new UniqueCriteria());

        model.getCriteria().getCriterias().add(element);

        CriterionModel criterionModel = new CriterionModel(element);
        criterionModel.getBinTypes().addAll(CriteriaTypeFactory.getInstance().getAvailableTypes());

        support.replayAll();

        CriterionCellBinder binder = new CriterionCellBinder(view, criterionModel, model);

        assertEquals("0", view.getTolerance().getText());
        assertEquals(ourStringField, view.getFieldBox().getValue());
        assertEquals(UniqueCriteria.CRITERIA_TYPE, view.getBinTypeBox().getValue());

        assertFalse(view.getRemoveButton().isVisible());
        assertTrue(view.getFieldBox().getItems().contains(ourNumberField));
        assertTrue(view.getFieldBox().getItems().contains(ourStringField));
        assertTrue(view.getFieldBox().getItems().contains(ourStringField2));

        assertTrue(view.getBinTypeBox().getItems().contains(UniqueCriteria.CRITERIA_TYPE));
        assertTrue(view.getBinTypeBox().getItems().contains(RangeCriteria.CRITERIA_TYPE));

        assertFalse(view.getTolerance().isVisible());
        assertFalse(view.getToleranceLabel().isVisible());

        binder.close();

        support.verifyAll();
    }

    /**
     * Tests when the user clicks the delete button.
     */
    @Test
    public void testRemove()
    {
        EasyMockSupport support = new EasyMockSupport();

        CriterionCellView view = createView(support);
        BinCriteriaModel model = createMainModel();

        BinCriteriaElement element = new BinCriteriaElement();
        element.setField(ourStringField);
        element.setCriteriaType(new UniqueCriteria());

        model.getCriteria().getCriterias().add(element);

        CriterionModel criterionModel = new CriterionModel(element);
        criterionModel.getBinTypes().addAll(CriteriaTypeFactory.getInstance().getAvailableTypes());

        support.replayAll();

        CriterionCellBinder binder = new CriterionCellBinder(view, criterionModel, model);

        assertEquals(ourStringField, view.getFieldBox().getValue());
        assertEquals(UniqueCriteria.CRITERIA_TYPE, view.getBinTypeBox().getValue());

        assertTrue(view.getFieldBox().getItems().contains(ourNumberField));
        assertTrue(view.getFieldBox().getItems().contains(ourStringField));
        assertTrue(view.getFieldBox().getItems().contains(ourStringField2));

        assertTrue(view.getBinTypeBox().getItems().contains(UniqueCriteria.CRITERIA_TYPE));
        assertTrue(view.getBinTypeBox().getItems().contains(RangeCriteria.CRITERIA_TYPE));

        assertFalse(view.getTolerance().isVisible());
        assertFalse(view.getToleranceLabel().isVisible());

        view.getRemoveButton().fire();

        assertTrue(model.getCriteria().getCriterias().isEmpty());

        binder.close();

        support.verifyAll();
    }

    /**
     * Tests when the user changes the selected field to a double type.
     */
    @Test
    public void testViewChangeToDouble()
    {
        EasyMockSupport support = new EasyMockSupport();

        CriterionCellView view = createView(support);
        BinCriteriaModel model = createMainModel();

        BinCriteriaElement element = new BinCriteriaElement();

        model.getCriteria().getCriterias().add(element);

        CriterionModel criterionModel = new CriterionModel(element);
        criterionModel.getBinTypes().addAll(CriteriaTypeFactory.getInstance().getAvailableTypes());

        support.replayAll();

        CriterionCellBinder binder = new CriterionCellBinder(view, criterionModel, model);

        view.getFieldBox().setValue(ourNumberField);
        view.getBinTypeBox().setValue(RangeCriteria.CRITERIA_TYPE);
        assertTrue(view.getTolerance().isVisible());
        assertTrue(view.getToleranceLabel().isVisible());
        assertEquals("10", view.getTolerance().getText());
        view.getTolerance().setText("17.3");

        assertEquals(ourNumberField, element.getField());
        assertEquals(RangeCriteria.CRITERIA_TYPE, element.getCriteriaType().getCriteriaType());
        assertEquals(17.3, ((RangeCriteria)element.getCriteriaType()).getBinWidth(), 0d);

        assertTrue(view.getFieldBox().getItems().contains(ourNumberField));
        assertTrue(view.getFieldBox().getItems().contains(ourStringField));
        assertTrue(view.getFieldBox().getItems().contains(ourStringField2));

        assertTrue(view.getBinTypeBox().getItems().contains(UniqueCriteria.CRITERIA_TYPE));
        assertTrue(view.getBinTypeBox().getItems().contains(RangeCriteria.CRITERIA_TYPE));

        binder.close();

        support.verifyAll();
    }

    /**
     * Tests when the user changes the selected field to a string type.
     */
    @Test
    public void testViewChangeToString()
    {
        EasyMockSupport support = new EasyMockSupport();

        CriterionCellView view = createView(support);
        BinCriteriaModel model = createMainModel();

        BinCriteriaElement element = new BinCriteriaElement();

        model.getCriteria().getCriterias().add(element);

        CriterionModel criterionModel = new CriterionModel(element);
        criterionModel.getBinTypes().addAll(CriteriaTypeFactory.getInstance().getAvailableTypes());

        support.replayAll();

        CriterionCellBinder binder = new CriterionCellBinder(view, criterionModel, model);
        view.getFieldBox().setValue(ourStringField);
        view.getBinTypeBox().setValue(UniqueCriteria.CRITERIA_TYPE);

        assertEquals(ourStringField, element.getField());
        assertEquals(UniqueCriteria.CRITERIA_TYPE, element.getCriteriaType().getCriteriaType());

        assertTrue(view.getFieldBox().getItems().contains(ourNumberField));
        assertTrue(view.getFieldBox().getItems().contains(ourStringField));
        assertTrue(view.getFieldBox().getItems().contains(ourStringField2));

        assertTrue(view.getBinTypeBox().getItems().contains(UniqueCriteria.CRITERIA_TYPE));
        assertTrue(view.getBinTypeBox().getItems().contains(RangeCriteria.CRITERIA_TYPE));

        assertFalse(view.getTolerance().isVisible());
        assertFalse(view.getToleranceLabel().isVisible());

        binder.close();

        support.verifyAll();
    }

    /**
     * Tests when the user changes the bin type from range to unique.
     */
    @Test
    public void testViewChangeToUnique()
    {
        EasyMockSupport support = new EasyMockSupport();

        CriterionCellView view = createView(support);
        BinCriteriaModel model = createMainModel();

        BinCriteriaElement element = new BinCriteriaElement();

        model.getCriteria().getCriterias().add(element);

        CriterionModel criterionModel = new CriterionModel(element);
        criterionModel.getBinTypes().addAll(CriteriaTypeFactory.getInstance().getAvailableTypes());

        support.replayAll();

        CriterionCellBinder binder = new CriterionCellBinder(view, criterionModel, model);

        view.getFieldBox().setValue(ourNumberField);
        view.getBinTypeBox().setValue(RangeCriteria.CRITERIA_TYPE);
        assertTrue(view.getTolerance().isVisible());
        assertTrue(view.getToleranceLabel().isVisible());
        view.getTolerance().setText("17.3");

        assertEquals(ourNumberField, element.getField());
        assertEquals(RangeCriteria.CRITERIA_TYPE, element.getCriteriaType().getCriteriaType());
        assertEquals(17.3, ((RangeCriteria)element.getCriteriaType()).getBinWidth(), 0d);

        assertTrue(view.getFieldBox().getItems().contains(ourNumberField));
        assertTrue(view.getFieldBox().getItems().contains(ourStringField));
        assertTrue(view.getFieldBox().getItems().contains(ourStringField2));

        assertTrue(view.getBinTypeBox().getItems().contains(UniqueCriteria.CRITERIA_TYPE));
        assertTrue(view.getBinTypeBox().getItems().contains(RangeCriteria.CRITERIA_TYPE));

        view.getBinTypeBox().setValue(UniqueCriteria.CRITERIA_TYPE);
        assertEquals(ourNumberField, element.getField());
        assertEquals(UniqueCriteria.CRITERIA_TYPE, element.getCriteriaType().getCriteriaType());
        assertFalse(view.getTolerance().isVisible());
        assertFalse(view.getToleranceLabel().isVisible());

        binder.close();

        support.verifyAll();
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

    /**
     * Creates an easy mocked view.
     *
     * @param support Used to create the mock.
     * @return The mocked view.
     */
    private CriterionCellView createView(EasyMockSupport support)
    {
        CriterionCellView view = support.createMock(CriterionCellView.class);

        ComboBox<String> binTypeBox = new ComboBox<>();
        ComboBox<String> fieldBox = new ComboBox<>();
        Button removeButton = new Button();
        TextField tolerance = new TextField();
        Label toleranceLable = new Label();

        EasyMock.expect(view.getBinTypeBox()).andReturn(binTypeBox).atLeastOnce();
        EasyMock.expect(view.getFieldBox()).andReturn(fieldBox).atLeastOnce();
        EasyMock.expect(view.getRemoveButton()).andReturn(removeButton).atLeastOnce();
        EasyMock.expect(view.getTolerance()).andReturn(tolerance).atLeastOnce();
        EasyMock.expect(view.getToleranceLabel()).andReturn(toleranceLable).atLeastOnce();

        return view;
    }
}
