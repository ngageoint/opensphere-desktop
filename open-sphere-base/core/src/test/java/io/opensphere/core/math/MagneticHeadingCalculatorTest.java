package io.opensphere.core.math;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.TimeManager;
import io.opensphere.core.TimeManager.ActiveTimeSpans;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;

/**
 * Unit test for the {@link MagneticHeadingCalculator}.
 */
public class MagneticHeadingCalculatorTest
{
    /**
     * Tests the calculator.
     *
     * @throws ParseException Bad date.
     */
    @Test
    public void test() throws ParseException
    {
        final EasyMockSupport support = new EasyMockSupport();

        final TimeManager timeManager = createTimeManager(support);

        support.replayAll();

        double magnetic = MagneticHeadingCalculator.getInstance().calculateMagneticHeading(45,
                LatLonAlt.createFromDegrees(25, -130), timeManager);
        assertEquals(34.194560901507046, magnetic, 0d);

        magnetic = MagneticHeadingCalculator.getInstance().calculateMagneticHeading(45, LatLonAlt.createFromDegrees(25, -130),
                null);
        // the expected value changes every year.
        // For 2018, the following commented out test result is expected:
        // assertEquals(34.39553047902176, magnetic, 0d);
        // For 2019, the following results are expected: 34.46300241643533
        // assertEquals(34.46300241643533, magnetic, 0d);
        // For 2020, the following results are expected: 34.530717252650824
        assertEquals(34.530717252650824, magnetic, 0d);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked time manager.
     *
     * @param support Used to create the mock.
     * @return The mocked time manager.
     * @throws ParseException bad date.
     */
    private TimeManager createTimeManager(final EasyMockSupport support) throws ParseException
    {
        final TimeSpan span = TimeSpan.fromISO8601String("2015-07-18T00:00:00Z/2015-07-19T00:00:00Z");
        final ActiveTimeSpans active = support.createMock(ActiveTimeSpans.class);
        EasyMock.expect(active.getPrimary()).andReturn(TimeSpanList.singleton(span));

        final TimeManager timeManager = support.createMock(TimeManager.class);
        EasyMock.expect(timeManager.getActiveTimeSpans()).andReturn(active);

        return timeManager;
    }
}
