package io.opensphere.core.order.impl;

import java.util.Collection;

import org.junit.Test;

import io.opensphere.core.order.OrderCategory;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.util.collections.New;
import org.junit.Assert;

/** Test for {@link OrderManagerImpl}. */
public class OrderManagerImplTest
{
    /** A category for test managers. */
    private static final OrderCategory CATEGORY = DefaultOrderCategory.IMAGE_BASE_MAP_CATEGORY;

    /** A family name for test managers. */
    private static final String FAMILY = DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY;

    /**
     * Verify that the participants are in the expected positions in the
     * manager.
     *
     * @param manager The manager.
     * @param first The participant in the first position in the manager.
     * @param second The participant in the second position in the manager.
     * @param third The participant in the third position in the manager.
     * @param fourth The participant in the fourth position in the manager.
     */
    private static void verifyOrder(OrderManager manager, DefaultOrderParticipantKey first, DefaultOrderParticipantKey second,
            DefaultOrderParticipantKey third, DefaultOrderParticipantKey fourth)
    {
        int orderMin = CATEGORY.getOrderRange().getMinimumInteger();

        Assert.assertTrue(manager.getParticipantMap().get(first) == orderMin);
        Assert.assertTrue(manager.getParticipantMap().get(second) == orderMin + 1);
        Assert.assertTrue(manager.getParticipantMap().get(third) == orderMin + 2);
        Assert.assertTrue(manager.getParticipantMap().get(fourth) == orderMin + 3);
    }

    /** Test removal of elements and re-compression. */
    @Test
    public void testExpunge()
    {
        // This is created such that the forced gaping is 0, allowing us to
        // control the position of the participants.
        OrderManager manager = new OrderManagerImpl(FAMILY, CATEGORY, null);

        DefaultOrderParticipantKey key1 = new DefaultOrderParticipantKey(FAMILY, CATEGORY, "ImageKey1");
        DefaultOrderParticipantKey key2 = new DefaultOrderParticipantKey(FAMILY, CATEGORY, "ImageKey2");
        DefaultOrderParticipantKey key3 = new DefaultOrderParticipantKey(FAMILY, CATEGORY, "ImageKey3");
        DefaultOrderParticipantKey key4 = new DefaultOrderParticipantKey(FAMILY, CATEGORY, "ImageKey4");
        DefaultOrderParticipantKey key5 = new DefaultOrderParticipantKey(FAMILY, CATEGORY, "ImageKey5");
        DefaultOrderParticipantKey key6 = new DefaultOrderParticipantKey(FAMILY, CATEGORY, "ImageKey6");

        manager.activateParticipant(key1);
        manager.activateParticipant(key2);
        manager.activateParticipant(key3);
        manager.activateParticipant(key4);
        manager.activateParticipant(key5);

        // remove 2 (should give 1, 3, 4, 5)
        manager.expungeParticipant(key2);
        verifyOrder(manager, key1, key3, key4, key5);

        Collection<OrderParticipantKey> adds = New.collection(2);
        adds.add(key2);
        adds.add(key6);
        manager.activateParticipants(adds);

        // we should now have 1, 3, 4, 5, 2, 6

        // remove 1 and 4 (should give 3, 5, 2, 6)
        Collection<OrderParticipantKey> removes = New.collection(2);
        removes.add(key1);
        removes.add(key4);
        manager.expungeParticipants(removes);
        verifyOrder(manager, key3, key5, key2, key6);
    }

    /** Test moving a participant above other participants. */
    @Test
    public void testMoveAbove()
    {
        OrderManager manager = new OrderManagerImpl(FAMILY, CATEGORY, null);

        DefaultOrderParticipantKey key1 = new DefaultOrderParticipantKey(FAMILY, CATEGORY, "ImageKey1");
        DefaultOrderParticipantKey key2 = new DefaultOrderParticipantKey(FAMILY, CATEGORY, "ImageKey2");
        DefaultOrderParticipantKey key3 = new DefaultOrderParticipantKey(FAMILY, CATEGORY, "ImageKey3");
        DefaultOrderParticipantKey key4 = new DefaultOrderParticipantKey(FAMILY, CATEGORY, "ImageKey4");

        manager.activateParticipant(key1);
        manager.activateParticipant(key2);
        manager.activateParticipant(key3);
        manager.activateParticipant(key4);

        // move 2 above 3 (should give 1,3,2,4)
        manager.moveAbove(key2, key3);
        verifyOrder(manager, key1, key3, key2, key4);

        // move 2 above 4 (use moveToTop) (should give 1,3,4,2)
        manager.moveToTop(key2);
        verifyOrder(manager, key1, key3, key4, key2);

        // move 2 above 1 (should give 1,2,3,4)
        manager.moveAbove(key2, key1);
        verifyOrder(manager, key1, key2, key3, key4);
    }

    /** Test moving a participant below other participants. */
    @Test
    public void testMoveBelow()
    {
        // This is created such that the forced gaping is 0, allowing us to
        // control the position of the participants.
        OrderManager manager = new OrderManagerImpl(FAMILY, CATEGORY, null);

        DefaultOrderParticipantKey key1 = new DefaultOrderParticipantKey(FAMILY, CATEGORY, "ImageKey1");
        DefaultOrderParticipantKey key2 = new DefaultOrderParticipantKey(FAMILY, CATEGORY, "ImageKey2");
        DefaultOrderParticipantKey key3 = new DefaultOrderParticipantKey(FAMILY, CATEGORY, "ImageKey3");
        DefaultOrderParticipantKey key4 = new DefaultOrderParticipantKey(FAMILY, CATEGORY, "ImageKey4");

        manager.activateParticipant(key1);
        manager.activateParticipant(key2);
        manager.activateParticipant(key3);
        manager.activateParticipant(key4);

        // move 3 below 2 (should give 1,3,2,4)
        manager.moveBelow(key3, key2);
        verifyOrder(manager, key1, key3, key2, key4);

        // move 3 below 1 (use moveToBottom) (should give 3,1,2,4)
        manager.moveToBottom(key3);
        verifyOrder(manager, key3, key1, key2, key4);

        // move 3 below 4 (should give 1,2,3,4)
        manager.moveBelow(key3, key4);
        verifyOrder(manager, key1, key2, key3, key4);
    }
}
