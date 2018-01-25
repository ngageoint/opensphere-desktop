package io.opensphere.core.animationhelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link TimeSpanGovernor}.
 */
public class TimeSpanGovernorTest
{
    /**
     * Tests making some queries at some given times. Then removing one of the
     * queries. Then making another query that is a subset of the removed query.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testRequestClearRequest() throws InterruptedException
    {
        MockTimeSpanGovernor governor = new MockTimeSpanGovernor();
        TimeSpan query1 = TimeSpan.get(System.currentTimeMillis() - 100000, System.currentTimeMillis() - 80000);
        TimeSpan query2 = TimeSpan.get(System.currentTimeMillis() - 5000, System.currentTimeMillis());

        CountDownLatch latch = new CountDownLatch(2);

        governor.requestData(query1, () ->
        {
            latch.countDown();
        });

        governor.requestData(query1, () ->
        {
            latch.countDown();
        });

        governor.requestData(query2, () ->
        {
            latch.countDown();
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));

        assertEquals(2, governor.getRequestedTimes().size());
        assertTrue(governor.getRequestedTimes().contains(query1));
        assertTrue(governor.getRequestedTimes().contains(query2));

        governor.clearData(New.list(query1));

        CountDownLatch latch1 = new CountDownLatch(1);
        TimeSpan query3 = TimeSpan.get(System.currentTimeMillis() - 95000, System.currentTimeMillis() - 85000);
        governor.requestData(query3, () ->
        {
            latch1.countDown();
        });

        assertTrue(latch1.await(1, TimeUnit.SECONDS));
        assertEquals(3, governor.getRequestedTimes().size());
        assertTrue(governor.getRequestedTimes().contains(query3));
    }
}
