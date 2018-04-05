package io.opensphere.controlpanels.styles.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import javafx.application.Platform;

import io.opensphere.controlpanels.styles.model.EllipseModel;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link EllipseBinder} class.
 */
public class EllipseBinderTestDisplay
{
    /**
     * Tests the binding.
     */
    @Test
    public void test()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        EllipseView view = createView(support);

        support.replayAll();

        List<String> availableColumns = New.list("nm", "mi", "m");

        EllipseModel model = new EllipseModel();
        model.getAvailableUnits().addAll(availableColumns);
        model.setOrientation(10);
        model.setSemiMajor(12);
        model.setSemiMinor(11);
        model.setSemiMajorUnits("nm");
        model.setSemiMinorUnits("nm");

        EllipseBinder binder = new EllipseBinder(view, model);

        assertEquals("10", view.getOrientationField().getText());
        assertEquals("12", view.getSemiMajorField().getText());
        assertEquals("11", view.getSemiMinorField().getText());
        assertEquals("nm", view.getSemiMajorUnitsPicker().getValue());
        assertEquals("nm", view.getSemiMinorUnitsPicker().getValue());
        assertEquals(availableColumns, view.getSemiMajorUnitsPicker().getItems());
        assertEquals(availableColumns, view.getSemiMinorUnitsPicker().getItems());

        view.getOrientationField().setText("13");
        view.getSemiMajorField().setText("14");
        view.getSemiMinorField().setText("9");
        view.getSemiMajorUnitsPicker().setValue("mi");
        view.getSemiMinorUnitsPicker().setValue("m");

        assertEquals(13, model.getOrientation(), 0d);
        assertEquals(14, model.getSemiMajor(), 0d);
        assertEquals(9, model.getSemiMinor(), 0d);
        assertEquals("mi", model.getSemiMajorUnits());
        assertEquals("m", model.getSemiMinorUnits());

        model.setOrientation(10);
        model.setSemiMajor(12);
        model.setSemiMinor(11);
        model.setSemiMajorUnits("nm");
        model.setSemiMinorUnits("nm");

        assertEquals("10", view.getOrientationField().getText());
        assertEquals("12", view.getSemiMajorField().getText());
        assertEquals("11", view.getSemiMinorField().getText());
        assertEquals("nm", view.getSemiMajorUnitsPicker().getValue());
        assertEquals("nm", view.getSemiMinorUnitsPicker().getValue());

        binder.close();

        view.getOrientationField().setText("13");
        view.getSemiMajorField().setText("14");
        view.getSemiMinorField().setText("9");
        view.getSemiMajorUnitsPicker().setValue("mi");
        view.getSemiMinorUnitsPicker().setValue("m");

        model.setOrientation(2);
        model.setSemiMajor(5);
        model.setSemiMinor(4);
        model.setSemiMajorUnits("m");
        model.setSemiMinorUnits("mi");

        assertEquals("13", view.getOrientationField().getText());
        assertEquals("14", view.getSemiMajorField().getText());
        assertEquals("9", view.getSemiMinorField().getText());
        assertEquals("mi", view.getSemiMajorUnitsPicker().getValue());
        assertEquals("m", view.getSemiMinorUnitsPicker().getValue());
        assertTrue(view.getSemiMajorUnitsPicker().getItems().isEmpty());
        assertTrue(view.getSemiMinorUnitsPicker().getItems().isEmpty());

        assertEquals(2, model.getOrientation(), 0d);
        assertEquals(5, model.getSemiMajor(), 0d);
        assertEquals(4, model.getSemiMinor(), 0d);
        assertEquals("m", model.getSemiMajorUnits());
        assertEquals("mi", model.getSemiMinorUnits());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link EllipseView}.
     *
     * @param support Used to create the mock.
     * @return The mocked view.
     */
    private EllipseView createView(EasyMockSupport support)
    {
        EllipseView view = support.createMock(EllipseView.class);

        TextField semiMajor = new TextField();
        EasyMock.expect(view.getSemiMajorField()).andReturn(semiMajor).atLeastOnce();

        TextField semiMinor = new TextField();
        EasyMock.expect(view.getSemiMinorField()).andReturn(semiMinor).atLeastOnce();

        TextField orientation = new TextField();
        EasyMock.expect(view.getOrientationField()).andReturn(orientation).atLeastOnce();

        ComboBox<String> semiMajorUnits = new ComboBox<>();
        EasyMock.expect(view.getSemiMajorUnitsPicker()).andReturn(semiMajorUnits).atLeastOnce();

        ComboBox<String> semiMinorUnits = new ComboBox<>();
        EasyMock.expect(view.getSemiMinorUnitsPicker()).andReturn(semiMinorUnits).atLeastOnce();

        return view;
    }
}
