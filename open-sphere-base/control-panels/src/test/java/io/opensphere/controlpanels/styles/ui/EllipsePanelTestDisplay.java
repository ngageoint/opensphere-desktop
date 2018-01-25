package io.opensphere.controlpanels.styles.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.sun.javafx.application.PlatformImpl;

import io.opensphere.controlpanels.styles.model.EllipseModel;
import io.opensphere.core.UnitsRegistry;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.units.length.NauticalMiles;
import io.opensphere.core.units.length.StatuteMiles;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link EllipsePanel}.
 */
public class EllipsePanelTestDisplay
{
    /**
     * Tests the UI.
     */
    @Test
    public void test()
    {
        PlatformImpl.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        List<String> availableUnits = New.list("nautical miles", "statute miles", "meters");

        UnitsRegistry units = support.createMock(UnitsRegistry.class);
        units.getAvailableUnits(Length.class, false);
        EasyMock.expectLastCall().andReturn(New.list(NauticalMiles.class, StatuteMiles.class, Meters.class));

        support.replayAll();

        EllipseModel model = new EllipseModel();
        model.setOrientation(10);
        model.setSemiMajor(12);
        model.setSemiMinor(11);
        model.setSemiMajorUnits(availableUnits.get(0));
        model.setSemiMinorUnits(availableUnits.get(0));

        EllipsePanel view = new EllipsePanel(units, model);

        assertEquals("10", view.getOrientationField().getText());
        assertEquals("12", view.getSemiMajorField().getText());
        assertEquals("11", view.getSemiMinorField().getText());
        assertEquals(availableUnits.get(0), view.getSemiMajorUnitsPicker().getValue());
        assertEquals(availableUnits.get(0), view.getSemiMinorUnitsPicker().getValue());
        assertEquals(availableUnits, view.getSemiMajorUnitsPicker().getItems());
        assertEquals(availableUnits, view.getSemiMinorUnitsPicker().getItems());

        view.getOrientationField().setText("13");
        view.getSemiMajorField().setText("14");
        view.getSemiMinorField().setText("9");
        view.getSemiMajorUnitsPicker().setValue(availableUnits.get(1));
        view.getSemiMinorUnitsPicker().setValue(availableUnits.get(2));

        assertEquals(13, model.getOrientation(), 0d);
        assertEquals(14, model.getSemiMajor(), 0d);
        assertEquals(9, model.getSemiMinor(), 0d);
        assertEquals(availableUnits.get(1), model.getSemiMajorUnits());
        assertEquals(availableUnits.get(2), model.getSemiMinorUnits());

        assertNotNull(view.getOrientationField().getTooltip());
        assertNotNull(view.getSemiMajorField().getTooltip());
        assertNotNull(view.getSemiMinorField().getTooltip());
        assertNotNull(view.getSemiMajorUnitsPicker().getTooltip());
        assertNotNull(view.getSemiMinorUnitsPicker().getTooltip());

        support.verifyAll();
    }
}
