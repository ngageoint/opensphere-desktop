package io.opensphere.controlpanels.styles.ui;

import static org.junit.Assert.assertEquals;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import javafx.application.Platform;

import io.opensphere.controlpanels.styles.model.StyleOptions;
import io.opensphere.controlpanels.styles.model.Styles;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link StyleOptionsBinder}.
 */
public class StyleOptionsBinderTestDisplay
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

        StyleOptionsView view = createView(support);

        support.replayAll();

        StyleOptions model = new StyleOptions();

        model.setColor(java.awt.Color.RED);
        model.setSize(12);
        model.setStyle(Styles.POINT);

        StyleOptionsBinder binder = new StyleOptionsBinder(view, model);

        assertEquals(Color.RED, view.getColorPicker().getValue());
        assertEquals(12, view.getSize().getValue(), 0d);
        assertEquals(Styles.POINT, view.getStylePicker().getValue());
        assertEquals(New.list(Styles.values()), view.getStylePicker().getItems());

        view.getColorPicker().setValue(Color.BLUE);
        view.getSize().setValue(14);
        view.getStylePicker().setValue(Styles.ELLIPSE);

        assertEquals(java.awt.Color.BLUE, model.getColor());
        assertEquals(14, model.getSize());
        assertEquals(Styles.ELLIPSE, model.getStyle());

        model.setColor(java.awt.Color.WHITE);
        model.setSize(15);
        model.setStyle(Styles.NONE);

        assertEquals(Color.WHITE, view.getColorPicker().valueProperty().get());
        assertEquals(15, view.getSize().getValue(), 0d);
        assertEquals(Styles.NONE, view.getStylePicker().getValue());

        binder.close();

        view.getColorPicker().setValue(Color.YELLOW);
        view.getSize().setValue(16);
        view.getStylePicker().setValue(Styles.ICON);

        model.setColor(java.awt.Color.BLUE);
        model.setSize(14);
        model.setStyle(Styles.ELLIPSE);

        assertEquals(java.awt.Color.BLUE, model.getColor());
        assertEquals(14, model.getSize());
        assertEquals(Styles.ELLIPSE, model.getStyle());

        assertEquals(Color.YELLOW, view.getColorPicker().valueProperty().get());
        assertEquals(16, view.getSize().getValue(), 0d);
        assertEquals(Styles.ICON, view.getStylePicker().getValue());

        support.verifyAll();
    }

    /**
     * Creates a mocked {@link StyleOptionsView}.
     *
     * @param support Used to create the mock.
     * @return The style options view.
     */
    private StyleOptionsView createView(EasyMockSupport support)
    {
        StyleOptionsView view = support.createMock(StyleOptionsView.class);

        ColorPicker colorPicker = new ColorPicker();
        EasyMock.expect(view.getColorPicker()).andReturn(colorPicker).atLeastOnce();

        Slider slider = new Slider();
        EasyMock.expect(view.getSize()).andReturn(slider).atLeastOnce();

        ComboBox<Styles> stylePicker = new ComboBox<>();
        EasyMock.expect(view.getStylePicker()).andReturn(stylePicker).atLeastOnce();

        return view;
    }
}
