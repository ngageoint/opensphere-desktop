package io.opensphere.core.pipeline.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collection;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.TimeManager;
import io.opensphere.core.geometry.ConstrainableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.constraint.MutableConstraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.util.collections.New;

/**
 * Tests the {@link MostRecentGeometryFilter} class.
 */
public class MostRecentGeometryFilterTest
{
    /** Now. */
    private static final long NOW = System.currentTimeMillis();

    /** A second ago. */
    private static final long A_SECOND_AGO = NOW - 1000;

    /** A minute ago. */
    private static final long A_MINUTE_AGO = NOW - 60000;

    /** A minute from now. */
    private static final long A_MINUTE_FROM_NOW = NOW + 60000;

    /**
     * Tests the {@link MostRecentGeometryFilter} with non constrained
     * geometries, null constrained geometries, time constrained geometries, and
     * most recent constrained geometries.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        Geometry nonConstrained = support.createMock(Geometry.class);
        Geometry nullConstrained = support.createNiceMock(ConstrainableGeometry.class);
        Geometry timeConstrained = createConstrainableGeometry(support, TimeConstraint.getTimeConstraint(TimeSpan.get(NOW)));
        Geometry oldMostRecent = createConstrainableGeometry(support,
                TimeConstraint.getMostRecentTimeConstraint("id", A_SECOND_AGO));
        Geometry newMostRecent = createConstrainableGeometry(support, TimeConstraint.getMostRecentTimeConstraint("id", NOW));
        Geometry futureMostRecent = createConstrainableGeometry(support,
                TimeConstraint.getMostRecentTimeConstraint("id", A_MINUTE_FROM_NOW));

        List<Geometry> geometries = New.list(nonConstrained, nullConstrained, timeConstrained, oldMostRecent, newMostRecent,
                futureMostRecent);

        TimeManager timeManager = createTimeManager(support);

        support.replayAll();

        MostRecentGeometryFilter filter = new MostRecentGeometryFilter();
        Collection<Geometry> filtered = filter.filterMostRecent(geometries, timeManager, true);

        assertEquals(geometries.size() - 2, filtered.size());
        assertFalse(filtered.contains(oldMostRecent));
        assertFalse(filtered.contains(futureMostRecent));

        support.verifyAll();
    }

    /**
     * Tests the {@link MostRecentGeometryFilter} with non constrained
     * geometries, null constrained geometries, time constrained geometries, and
     * most recent constrained geometries.
     */
    @Test
    public void testNoConstraints()
    {
        EasyMockSupport support = new EasyMockSupport();

        Geometry nonConstrained = support.createMock(Geometry.class);
        Geometry nullConstrained = support.createNiceMock(ConstrainableGeometry.class);
        Geometry timeConstrained = createConstrainableGeometry(support, TimeConstraint.getTimeConstraint(TimeSpan.get(NOW)));
        Geometry oldMostRecent = createConstrainableGeometry(support,
                TimeConstraint.getMostRecentTimeConstraint("id", A_SECOND_AGO));
        Geometry newMostRecent = createConstrainableGeometry(support, TimeConstraint.getMostRecentTimeConstraint("id", NOW));
        Geometry futureMostRecent = createConstrainableGeometry(support,
                TimeConstraint.getMostRecentTimeConstraint("id", A_MINUTE_FROM_NOW));

        List<Geometry> geometries = New.list(nonConstrained, nullConstrained, timeConstrained, oldMostRecent, newMostRecent,
                futureMostRecent);

        TimeManager timeManager = createTimeManager(support);

        support.replayAll();

        MostRecentGeometryFilter filter = new MostRecentGeometryFilter();
        Collection<Geometry> filtered = filter.filterMostRecent(geometries, timeManager, false);

        assertEquals(geometries.size() - 2, filtered.size());
        assertFalse(filtered.contains(oldMostRecent));
        assertFalse(filtered.contains(newMostRecent));

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link ConstrainableGeometry}.
     *
     * @param support Used to create the mock.
     * @param constraint The time constraint for the geometry.
     * @return The {@link ConstrainableGeometry}.
     */
    private ConstrainableGeometry createConstrainableGeometry(EasyMockSupport support, TimeConstraint constraint)
    {
        ConstrainableGeometry geometry = support.createMock(ConstrainableGeometry.class);

        EasyMock.expect(geometry.getConstraints()).andReturn(new MutableConstraints(constraint)).anyTimes();

        return geometry;
    }

    /**
     * Creates an easy mocked time manager.
     *
     * @param support Used to create the mock.
     * @return The time manager.
     */
    private TimeManager createTimeManager(EasyMockSupport support)
    {
        TimeManager timeManager = support.createMock(TimeManager.class);

        EasyMock.expect(timeManager.getPrimaryActiveTimeSpans())
                .andReturn(TimeSpanList.singleton(TimeSpan.get(A_MINUTE_AGO, NOW + 1))).anyTimes();

        return timeManager;
    }
}
