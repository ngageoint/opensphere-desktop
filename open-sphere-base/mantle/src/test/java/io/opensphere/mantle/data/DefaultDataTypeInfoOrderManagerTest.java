package io.opensphere.mantle.data;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;

import io.opensphere.core.order.OrderCategory;
import io.opensphere.core.order.OrderChangeListener;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.test.core.matchers.EasyMockHelper;

/** Test for {@link DefaultDataTypeInfoOrderManager}. */
public class DefaultDataTypeInfoOrderManagerTest
{
    /** Test for {@link DefaultDataTypeInfoOrderManager}. */
    @Test
    public void test()
    {
        int order = 10101;

        DataTypeInfo type = EasyMock.createMock(DataTypeInfo.class);
        EasyMock.expect(type.getTypeKey()).andReturn("typekey");
        Capture<OrderParticipantKey> keyCapture = EasyMock.newCapture();
        type.setOrderKey(EasyMock.capture(keyCapture));
        MapVisualizationInfo mapVisInfo = EasyMock.createMock(MapVisualizationInfo.class);
        mapVisInfo.setZOrder(EasyMock.eq(order), EasyMock.anyObject());
        EasyMock.expectLastCall();
        EasyMock.expect(type.getMapVisualizationInfo()).andReturn(mapVisInfo).times(2);

        OrderManager orderManager = EasyMock.createMock(OrderManager.class);
        Capture<OrderChangeListener> listenerCapture = EasyMock.newCapture();
        orderManager.addParticipantChangeListener(EasyMock.capture(listenerCapture));
        EasyMock.expect(Integer.valueOf(orderManager.activateParticipant(EasyMockHelper.eq(keyCapture))))
                .andReturn(Integer.valueOf(order));
        EasyMock.expect(orderManager.getFamily()).andReturn("family");
        OrderCategory category = EasyMock.createMock(OrderCategory.class);
        EasyMock.expect(orderManager.getCategory()).andReturn(category);

        EasyMock.replay(mapVisInfo, orderManager, type, category);

        DefaultDataTypeInfoOrderManager dtOrderManager = new DefaultDataTypeInfoOrderManager(orderManager);
        dtOrderManager.open();
        dtOrderManager.activateParticipant(type);

        EasyMock.verify(mapVisInfo, orderManager, type, category);
        EasyMock.reset(mapVisInfo, orderManager, type, category);

        EasyMock.expect(type.getOrderKey()).andReturn(keyCapture.getValue()).times(2);
        EasyMock.expect(Integer.valueOf(orderManager.expungeParticipant(keyCapture.getValue())))
                .andReturn(Integer.valueOf(order));

        EasyMock.replay(mapVisInfo, orderManager, type, category);

        dtOrderManager.expungeDataType(type);

        EasyMock.verify(mapVisInfo, orderManager, type, category);
        EasyMock.reset(mapVisInfo, orderManager, type, category);

        EasyMock.expect(type.getTypeKey()).andReturn("typekey");
        type.setOrderKey(EasyMock.capture(keyCapture));
        mapVisInfo.setZOrder(EasyMock.eq(order), EasyMock.anyObject());
        EasyMock.expectLastCall();
        EasyMock.expect(type.getMapVisualizationInfo()).andReturn(mapVisInfo).times(2);

        EasyMock.expect(Integer.valueOf(orderManager.activateParticipant(EasyMockHelper.eq(keyCapture))))
                .andReturn(Integer.valueOf(order));
        EasyMock.expect(orderManager.getFamily()).andReturn("family");
        EasyMock.expect(orderManager.getCategory()).andReturn(category);

        EasyMock.replay(mapVisInfo, orderManager, type, category);

        dtOrderManager.activateParticipant(type);

        EasyMock.verify(mapVisInfo, orderManager, type, category);
        EasyMock.reset(mapVisInfo, orderManager, type, category);

        EasyMock.expect(Integer.valueOf(orderManager.deactivateParticipant(keyCapture.getValue())))
                .andReturn(Integer.valueOf(order));
        orderManager.removeParticipantChangeListener(listenerCapture.getValue());

        EasyMock.replay(mapVisInfo, orderManager, type, category);

        dtOrderManager.close();

        EasyMock.verify(mapVisInfo, orderManager, type, category);
    }
}
