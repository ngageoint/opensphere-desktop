package io.opensphere.analysis.binning.editor.controller;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javafx.collections.ListChangeListener;

import org.easymock.EasyMockSupport;
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

/**
 * Unit test for {@link CriterionController}.
 */
public class CriterionControllerTest
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
     * Tests initial values with a number field initially selected.
     */
    @Test
    public void testInitialValuesNumber()
    {
        BinCriteriaModel mainModel = createMainModel();

        BinCriteriaElement element = new BinCriteriaElement();
        element.setCriteriaType(new UniqueCriteria());
        element.setField(ourNumberField);
        CriterionModel model = new CriterionModel(element);

        CriterionController controller = new CriterionController(mainModel, model);

        assertEquals(2, model.getBinTypes().size());
        assertEquals(UniqueCriteria.CRITERIA_TYPE, model.getBinTypes().get(0));
        assertEquals(RangeCriteria.CRITERIA_TYPE, model.getBinTypes().get(1));

        controller.close();
    }

    /**
     * Tests initial values with a string field initially selected.
     */
    @Test
    public void testInitialValuesString()
    {
        BinCriteriaModel mainModel = createMainModel();

        BinCriteriaElement element = new BinCriteriaElement();
        element.setCriteriaType(new UniqueCriteria());
        element.setField(ourStringField);
        CriterionModel model = new CriterionModel(element);

        CriterionController controller = new CriterionController(mainModel, model);

        assertEquals(1, model.getBinTypes().size());
        assertEquals(UniqueCriteria.CRITERIA_TYPE, model.getBinTypes().get(0));

        controller.close();
    }

    /**
     * Test selected the number type.
     */
    @Test
    public void testNumberType()
    {
        BinCriteriaModel mainModel = createMainModel();

        BinCriteriaElement element = new BinCriteriaElement();
        CriterionModel model = new CriterionModel(element);

        CriterionController controller = new CriterionController(mainModel, model);

        element.setField(ourNumberField);

        assertEquals(2, model.getBinTypes().size());
        assertEquals(UniqueCriteria.CRITERIA_TYPE, element.getCriteriaType().getCriteriaType());
        assertEquals(UniqueCriteria.CRITERIA_TYPE, model.getBinTypes().get(0));
        assertEquals(RangeCriteria.CRITERIA_TYPE, model.getBinTypes().get(1));

        element.setCriteriaType(CriteriaTypeFactory.getInstance().newCriteriaType(model.getBinTypes().get(1)));
        assertEquals(RangeCriteria.CRITERIA_TYPE, element.getCriteriaType().getCriteriaType());
        assertEquals(10, ((RangeCriteria)element.getCriteriaType()).getBinWidth(), 0);

        controller.close();

        element.setField(ourStringField);

        assertEquals(2, model.getBinTypes().size());
        assertEquals(UniqueCriteria.CRITERIA_TYPE, model.getBinTypes().get(0));
        assertEquals(RangeCriteria.CRITERIA_TYPE, model.getBinTypes().get(1));
    }

    /**
     * Tests selecting a string field type and then selected it again.
     */
    @Test
    public void testStringFieldChangedToStringField()
    {
        EasyMockSupport support = new EasyMockSupport();

        @SuppressWarnings("unchecked")
        ListChangeListener<String> listener = support.createMock(ListChangeListener.class);

        support.replayAll();

        BinCriteriaModel mainModel = createMainModel();

        BinCriteriaElement element = new BinCriteriaElement();
        CriterionModel model = new CriterionModel(element);

        CriterionController controller = new CriterionController(mainModel, model);

        element.setField(ourStringField);

        assertEquals(1, model.getBinTypes().size());
        assertEquals(UniqueCriteria.CRITERIA_TYPE, model.getBinTypes().get(0));
        model.getBinTypes().addListener(listener);

        element.setField(ourStringField2);

        controller.close();
    }

    /**
     * Tests selected a string field type.
     */
    @Test
    public void testStringType()
    {
        BinCriteriaModel mainModel = createMainModel();

        BinCriteriaElement element = new BinCriteriaElement();
        CriterionModel model = new CriterionModel(element);

        CriterionController controller = new CriterionController(mainModel, model);

        element.setField(ourStringField);

        assertEquals(1, model.getBinTypes().size());
        assertEquals(UniqueCriteria.CRITERIA_TYPE, model.getBinTypes().get(0));
        assertEquals(UniqueCriteria.CRITERIA_TYPE, model.getElement().getCriteriaType().getCriteriaType());

        controller.close();
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
