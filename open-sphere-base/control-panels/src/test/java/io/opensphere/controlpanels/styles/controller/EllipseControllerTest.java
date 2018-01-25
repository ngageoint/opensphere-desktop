package io.opensphere.controlpanels.styles.controller;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.controlpanels.styles.model.EllipseModel;
import io.opensphere.core.UnitsRegistry;
import io.opensphere.core.units.length.AutoscaleImperial;
import io.opensphere.core.units.length.Feet;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.NauticalMiles;
import io.opensphere.core.units.length.StatuteMiles;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link EllipseController}.
 */
public class EllipseControllerTest
{
    /**
     * The units for testing.
     */
    private static final List<Class<? extends Length>> ourUnits = New.list(NauticalMiles.class, StatuteMiles.class, Feet.class);

    /**
     * Tests populating the units.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        UnitsRegistry unitRegistry = createRegistry(support, Feet.class);

        support.replayAll();

        EllipseModel model = new EllipseModel();
        EllipseController controller = new EllipseController(unitRegistry, model);
        controller.applyUnits();

        assertEquals(New.list("nautical miles", "statute miles", "feet"), model.getAvailableUnits());
        assertEquals("feet", model.getSemiMajorUnits());
        assertEquals("feet", model.getSemiMinorUnits());

        support.verifyAll();
    }

    /**
     * Tests populating the units and the default unit is an autoscale.
     */
    @Test
    public void testAutoDefault()
    {
        EasyMockSupport support = new EasyMockSupport();

        UnitsRegistry unitRegistry = createRegistry(support, AutoscaleImperial.class);

        support.replayAll();

        EllipseModel model = new EllipseModel();
        EllipseController controller = new EllipseController(unitRegistry, model);
        controller.applyUnits();

        assertEquals(New.list("nautical miles", "statute miles", "feet"), model.getAvailableUnits());
        assertEquals("nautical miles", model.getSemiMajorUnits());
        assertEquals("nautical miles", model.getSemiMinorUnits());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked units registry.
     *
     * @param support Used to create the mock.
     * @param defaultUnit The default unit to return.
     * @return The mocked units registry.
     */
    private UnitsRegistry createRegistry(EasyMockSupport support, Class<? extends Length> defaultUnit)
    {
        UnitsRegistry unit = support.createMock(UnitsRegistry.class);

        EasyMock.expect(unit.getAvailableUnits(EasyMock.eq(Length.class), EasyMock.eq(false))).andReturn(ourUnits);
        unit.getPreferredUnits(EasyMock.eq(Length.class));
        EasyMock.expectLastCall().andReturn(defaultUnit);

        return unit;
    }
}
