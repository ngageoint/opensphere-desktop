package io.opensphere.merge.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Unit test for {@link MergeModel}.
 */
public class MergeModelTest
{
    /**
     * Tests the model.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo layer1 = support.createMock(DataTypeInfo.class);
        EasyMock.expect(layer1.getTypeKey()).andReturn("layer1").anyTimes();
        DataTypeInfo layer2 = support.createMock(DataTypeInfo.class);
        EasyMock.expect(layer2.getTypeKey()).andReturn("layer2").anyTimes();
        DataTypeInfo layer3 = support.createMock(DataTypeInfo.class);
        EasyMock.expect(layer3.getTypeKey()).andReturn("layer3").anyTimes();
        @SuppressWarnings("unchecked")
        ChangeListener<? super String> listener = support.createMock(ChangeListener.class);
        listener.changed(EasyMock.isA(StringProperty.class), EasyMock.isNull(), EasyMock.cmpEq("merged"));

        @SuppressWarnings("unchecked")
        ChangeListener<? super String> userMessageListener = support.createMock(ChangeListener.class);
        userMessageListener.changed(EasyMock.isA(StringProperty.class), EasyMock.isNull(), EasyMock.cmpEq("I have a message."));

        support.replayAll();

        MergeModel model = new MergeModel(New.list(layer1, layer2, layer3));
        model.getNewLayerName().addListener(listener);
        model.getUserMessage().addListener(userMessageListener);

        assertTrue(model.getLayers().containsAll(New.list(layer1, layer2, layer3)));
        model.getNewLayerName().set("merged");
        assertEquals("merged", model.getNewLayerName().get());

        model.getUserMessage().set("I have a message.");
        assertEquals("I have a message.", model.getUserMessage().get());

        support.verifyAll();
    }
}
