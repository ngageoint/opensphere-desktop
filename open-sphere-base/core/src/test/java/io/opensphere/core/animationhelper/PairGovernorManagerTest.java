package io.opensphere.core.animationhelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;

/**
 * Unit test for {@link PairGovernorManager}.
 */
public class PairGovernorManagerTest
{
    /**
     * Test id.
     */
    private static final String ourStringId = "i am string";

    /**
     * Tests clearing the data.
     */
    @Test
    public void testClearData()
    {
        EasyMockSupport support = new EasyMockSupport();

        TimeSpan span = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        TimeSpanGovernor governor = createGovernor(support, span);

        governor.clearData();
        EasyMock.expectLastCall().times(4);

        support.replayAll();

        PairGovernorManager<String, Integer> manager = new PairGovernorManager<>(p -> governor);
        Pair<String, Integer> context = new Pair<>(ourStringId, Integer.valueOf(7));

        manager.requestData(context, New.list(span));
        manager.clearData(context);
        assertTrue(manager.findGovernors(p -> true).isEmpty());

        manager.requestData(context, New.list(span));
        manager.clearData(new Pair<>(null, Integer.valueOf(7)));
        assertTrue(manager.findGovernors(p -> true).isEmpty());

        manager.requestData(context, New.list(span));
        manager.clearData(new Pair<>(ourStringId, null));
        assertTrue(manager.findGovernors(p -> true).isEmpty());

        manager.requestData(context, New.list(span));
        manager.clearData(new Pair<>(null, null));
        assertTrue(manager.findGovernors(p -> true).isEmpty());

        support.verifyAll();
    }

    /**
     * Tests clearing the data using time spans.
     */
    @Test
    public void testClearDataSpans()
    {
        EasyMockSupport support = new EasyMockSupport();

        TimeSpan span = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        List<TimeSpan> deleteSpan = New.list(TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis() - 5000));
        TimeSpanGovernor governor = createGovernor(support, span);
        governor.clearData(deleteSpan);
        EasyMock.expectLastCall().times(4);

        support.replayAll();

        PairGovernorManager<String, Integer> manager = new PairGovernorManager<>(p -> governor);
        Pair<String, Integer> context = new Pair<>(ourStringId, Integer.valueOf(7));

        manager.requestData(context, New.list(span));
        manager.clearData(context, deleteSpan);
        assertEquals(1, manager.findGovernors(p -> true).size());

        manager.requestData(context, New.list(span));
        manager.clearData(new Pair<>(null, Integer.valueOf(7)), deleteSpan);
        assertEquals(1, manager.findGovernors(p -> true).size());

        manager.requestData(context, New.list(span));
        manager.clearData(new Pair<>(ourStringId, null), deleteSpan);
        assertEquals(1, manager.findGovernors(p -> true).size());

        manager.requestData(context, New.list(span));
        manager.clearData(new Pair<>(null, null), deleteSpan);
        assertEquals(1, manager.findGovernors(p -> true).size());

        support.verifyAll();
    }

    /**
     * Tests requesting data.
     */
    @Test
    public void testRequestData()
    {
        EasyMockSupport support = new EasyMockSupport();

        TimeSpan span = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        TimeSpanGovernor governor = createGovernor(support, span);

        support.replayAll();

        PairGovernorManager<String, Integer> manager = new PairGovernorManager<>(p -> governor);
        Pair<String, Integer> context = new Pair<>(ourStringId, Integer.valueOf(7));

        manager.requestData(context, New.list(span));
        assertEquals(1, manager.findGovernors(p -> true).size());

        manager.requestData(new Pair<>(null, Integer.valueOf(7)), New.list(span));
        assertEquals(1, manager.findGovernors(p -> true).size());

        manager.requestData(new Pair<>(ourStringId, null), New.list(span));
        assertEquals(1, manager.findGovernors(p -> true).size());

        manager.requestData(new Pair<>(null, null), New.list(span));
        assertEquals(1, manager.findGovernors(p -> true).size());

        support.verifyAll();
    }

    /**
     * Creates a mocked {@link TimeSpanGovernor}.
     *
     * @param support Used to create the mock.
     * @param span The expected time span.
     * @return The mocked governor.
     */
    private TimeSpanGovernor createGovernor(EasyMockSupport support, TimeSpan span)
    {
        TimeSpanGovernor governor = support.createMock(TimeSpanGovernor.class);

        governor.requestData(span);
        EasyMock.expectLastCall().times(4);

        return governor;
    }
}
