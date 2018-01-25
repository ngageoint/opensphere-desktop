package io.opensphere.csvcommon.detect.datetime.algorithm.deciders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import io.opensphere.csvcommon.detect.datetime.algorithm.deciders.CompositeDateTimeDecider;
import io.opensphere.csvcommon.detect.datetime.algorithm.deciders.DateDecider;
import io.opensphere.csvcommon.detect.datetime.algorithm.deciders.DateTimeDecider;
import io.opensphere.csvcommon.detect.datetime.algorithm.deciders.Decider;
import io.opensphere.csvcommon.detect.datetime.algorithm.deciders.DeciderFactory;
import io.opensphere.csvcommon.detect.datetime.algorithm.deciders.OneDayMultipleTimesDecider;
import io.opensphere.csvcommon.detect.datetime.algorithm.deciders.TimeDecider;

/**
 * Tests the decider factory.
 *
 */
public class DeciderFactoryTest
{
    /**
     * Tests building the deciders.
     */
    @Test
    public void testBuildDeciders()
    {
        List<Decider> deciders = DeciderFactory.getInstance().buildDeciders();

        assertEquals(5, deciders.size());
        assertTrue(deciders.get(0) instanceof OneDayMultipleTimesDecider);
        assertTrue(deciders.get(1) instanceof CompositeDateTimeDecider);
        assertTrue(deciders.get(2) instanceof DateTimeDecider);
        assertTrue(deciders.get(3) instanceof DateDecider);
        assertTrue(deciders.get(4) instanceof TimeDecider);
    }
}
