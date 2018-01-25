package io.opensphere.analysis.binning.editor.model;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.opensphere.analysis.binning.criteria.BinCriteria;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;

/**
 * Unit test for {@link BinCriteriaModel}.
 */
public class BinCriteriaModelTest
{
    /**
     * Tests the {@link BinCriteriaModel}.
     */
    @Test
    public void test()
    {
        String dataTypeId = "layerid";
        List<Pair<String, Class<?>>> fields = New.list(new Pair<>("field1", String.class));
        BinCriteria criteria = new BinCriteria();

        BinCriteriaModel model = new BinCriteriaModel(dataTypeId, fields, criteria);
        assertEquals(dataTypeId, model.getDataTypeId());
        assertEquals(1, model.getLayersFields().size());
        assertEquals("field1", model.getLayersFields().get(0).getFirstObject());
        assertEquals(String.class, model.getLayersFields().get(0).getSecondObject());
        assertEquals(criteria, model.getCriteria());
    }
}
