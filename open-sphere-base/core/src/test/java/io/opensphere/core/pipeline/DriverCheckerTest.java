package io.opensphere.core.pipeline;

import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.collections.New;

/** Tests for {@link DriverChecker}. */
public class DriverCheckerTest
{
    /** The unsupported driver versions. */
    private static final Map<String, String> UNSUPPORTED_DRIVERS = Collections.singletonMap("ATI:14.", "disableTooltips");

    /** Test for {@link DriverChecker#getActions(String, String, Map)}. */
    @Test
    public void testGetActionsNvidia()
    {
        Assert.assertNull(DriverChecker.getActions("NVIDIA Corporation", "369.49", UNSUPPORTED_DRIVERS));
    }

    /** Test for {@link DriverChecker#getActions(String, String, Map)}. */
    @Test
    public void testGetActionsAtiGood()
    {
        Assert.assertNull(DriverChecker.getActions("ATI Technologies Inc.", "15.201.2201.0", UNSUPPORTED_DRIVERS));
    }

    /** Test for {@link DriverChecker#getActions(String, String, Map)}. */
    @Test
    public void testGetActionsAtiBad()
    {
        Assert.assertEquals(New.list("disableTooltips"),
                DriverChecker.getActions("ATI Technologies Inc.", "14.501.1003.0", UNSUPPORTED_DRIVERS));
    }
}
