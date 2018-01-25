package io.opensphere.controlpanels.styles.ui;

import static org.junit.Assert.assertEquals;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.Spinner;
import javafx.scene.paint.Color;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.sun.javafx.application.PlatformImpl;

import io.opensphere.controlpanels.styles.model.LabelOptions;

/**
 * Unit test for {@link LabelOptionsBinder}.
 */
public class LabelOptionsBinderTestDisplay
{
    /**
     * Unit test.
     */
    @Test
    public void test()
    {
        PlatformImpl.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        LabelOptionsView view = createView(support);

        support.replayAll();

        LabelOptions options = new LabelOptions();
        options.setColor(java.awt.Color.RED);
        options.setSize(12);

        LabelOptionsBinder binder = new LabelOptionsBinder(view, options);

        assertEquals(Color.RED, view.getColorPicker().valueProperty().get());
        assertEquals(Integer.valueOf(12), view.getSizePicker().getValueFactory().getValue());

        view.getColorPicker().valueProperty().set(Color.BLUE);
        view.getSizePicker().getValueFactory().setValue(Integer.valueOf(15));

        assertEquals(java.awt.Color.BLUE, options.getColor());
        assertEquals(15, options.getSize());

        options.setColor(java.awt.Color.WHITE);
        options.setSize(2);

        assertEquals(Color.WHITE, view.getColorPicker().valueProperty().get());
        assertEquals(Integer.valueOf(2), view.getSizePicker().getValueFactory().getValue());

        binder.close();

        view.getColorPicker().valueProperty().set(Color.BLUE);
        view.getSizePicker().getValueFactory().setValue(Integer.valueOf(7));

        options.setColor(java.awt.Color.YELLOW);
        options.setSize(8);

        assertEquals(Color.BLUE, view.getColorPicker().valueProperty().get());
        assertEquals(Integer.valueOf(7), view.getSizePicker().getValueFactory().getValue());

        assertEquals(java.awt.Color.YELLOW, options.getColor());
        assertEquals(8, options.getSize());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked view.
     *
     * @param support Used to create the view.
     * @return The easy mocked view.
     */
    private LabelOptionsView createView(EasyMockSupport support)
    {
        LabelOptionsView view = support.createMock(LabelOptionsView.class);

        ColorPicker colorPicker = new ColorPicker();
        EasyMock.expect(view.getColorPicker()).andReturn(colorPicker).atLeastOnce();

        Spinner<Integer> sizePicker = new Spinner<>(0, 24, 0);
        EasyMock.expect(view.getSizePicker()).andReturn(sizePicker).atLeastOnce();

        return view;
    }
}
