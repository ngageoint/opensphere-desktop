package io.opensphere.analysis.binning.editor.model;

import static org.junit.Assert.assertEquals;

import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.analysis.binning.criteria.BinCriteriaElement;

/**
 * Unit test for {@link CriterionModel}.
 */
public class CriterionModelTest
{
    /**
     * Tests the model.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        ListChangeListener<String> listener = support.createMock(ListChangeListener.class);
        listener.onChanged(EasyMock.isA(Change.class));
        EasyMock.expectLastCall().times(2);

        support.replayAll();

        BinCriteriaElement element = new BinCriteriaElement();

        CriterionModel model = new CriterionModel(element);

        model.getBinTypes().add("Unique");
        model.getBinTypes().add("Range");

        assertEquals(element, model.getElement());

        model.getBinTypes().addListener(listener);

        model.getBinTypes().remove("Range");
        model.getBinTypes().add("Range");

        support.verifyAll();
    }
}
