package io.opensphere.analysis.binning.editor.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.analysis.binning.criteria.BinCriteria;
import io.opensphere.analysis.binning.criteria.UniqueCriteria;
import io.opensphere.analysis.binning.editor.model.BinCriteriaModel;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;

/**
 * Unit test for {@link BinCriteriaController}.
 */
public class BinCriteriaControllerTest
{
    /**
     * The second column.
     */
    private static final String ourColumn2 = "acolumn";

    /**
     * The test layer id.
     */
    private static final String ourDtiKey = "iamkey";

    /**
     * Tests building the bin model without an existing criteria.
     */
    @Test
    public void testCreating()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupController typeController = createTypeController(support);

        support.replayAll();

        BinCriteriaController controller = new BinCriteriaController(typeController, ourDtiKey, null);
        BinCriteriaModel model = controller.getModel();

        assertNotNull(model.getCriteria());
        assertEquals(ourDtiKey, model.getCriteria().getDataTypeKey());
        assertEquals(1, model.getCriteria().getCriterias().size());
        assertEquals(ourColumn2, model.getCriteria().getCriterias().get(0).getField());
        assertEquals(UniqueCriteria.CRITERIA_TYPE, model.getCriteria().getCriterias().get(0).getCriteriaType().getCriteriaType());
        assertEquals(2, model.getLayersFields().size());
        assertEquals(ourColumn2, model.getLayersFields().get(0).getFirstObject());
        assertEquals(String.class, model.getLayersFields().get(0).getSecondObject());
        assertEquals("column1", model.getLayersFields().get(1).getFirstObject());
        assertEquals(Double.class, model.getLayersFields().get(1).getSecondObject());

        support.verifyAll();
    }

    /**
     * Tests building the bin model without an existing criteria.
     */
    @Test
    public void testCreatingEmpty()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupController typeController = createTypeController(support);

        support.replayAll();

        BinCriteriaController controller = new BinCriteriaController(typeController, ourDtiKey, new BinCriteria());
        BinCriteriaModel model = controller.getModel();

        assertNotNull(model.getCriteria());
        assertEquals(ourDtiKey, model.getCriteria().getDataTypeKey());
        assertEquals(1, model.getCriteria().getCriterias().size());
        assertEquals(ourColumn2, model.getCriteria().getCriterias().get(0).getField());
        assertEquals(UniqueCriteria.CRITERIA_TYPE, model.getCriteria().getCriterias().get(0).getCriteriaType().getCriteriaType());
        assertEquals(2, model.getLayersFields().size());
        assertEquals(ourColumn2, model.getLayersFields().get(0).getFirstObject());
        assertEquals(String.class, model.getLayersFields().get(0).getSecondObject());
        assertEquals("column1", model.getLayersFields().get(1).getFirstObject());
        assertEquals(Double.class, model.getLayersFields().get(1).getSecondObject());

        support.verifyAll();
    }

    /**
     * Tests building the bin model with existing criteria.
     */
    @Test
    public void testEditing()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupController typeController = createTypeController(support);

        support.replayAll();

        BinCriteria criteria = new BinCriteria();

        BinCriteriaController controller = new BinCriteriaController(typeController, ourDtiKey, criteria);
        BinCriteriaModel model = controller.getModel();

        assertEquals(criteria, model.getCriteria());
        assertEquals(2, model.getLayersFields().size());
        assertEquals(ourColumn2, model.getLayersFields().get(0).getFirstObject());
        assertEquals(String.class, model.getLayersFields().get(0).getSecondObject());
        assertEquals("column1", model.getLayersFields().get(1).getFirstObject());
        assertEquals(Double.class, model.getLayersFields().get(1).getSecondObject());

        support.verifyAll();
    }

    /**
     * Creates a mock data type controller.
     *
     * @param support Used to create the mock.
     * @return The mock data type controller.
     */
    private DataGroupController createTypeController(EasyMockSupport support)
    {
        Map<String, Class<?>> columnMap = New.map();
        columnMap.put("column1", Double.class);
        columnMap.put(ourColumn2, String.class);

        MetaDataInfo metadata = support.createMock(MetaDataInfo.class);
        EasyMock.expect(metadata.getKeyClassTypeMap()).andReturn(columnMap);

        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataType.getMetaDataInfo()).andReturn(metadata);

        DataGroupController groupController = support.createMock(DataGroupController.class);
        EasyMock.expect(groupController.findMemberById(EasyMock.cmpEq(ourDtiKey))).andReturn(dataType);

        return groupController;
    }
}
