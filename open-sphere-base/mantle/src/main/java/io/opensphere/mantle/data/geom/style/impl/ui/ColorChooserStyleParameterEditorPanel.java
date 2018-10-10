package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.bric.swing.ColorPicker;

import io.opensphere.core.util.swing.ColorCircleIcon;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * The Class AbstractStyleParameterEditorPanel.
 */
public class ColorChooserStyleParameterEditorPanel extends AbstractStyleParameterEditorPanel
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Color. */
    private Color myColor;

    /** The Slider. */
    private final JButton myColorChooserBT;

    /**
     * Instantiates a new abstract style parameter editor panel.
     *
     * @param label the label
     * @param style the style
     * @param paramKey the param key
     * @param previewable the previewable
     * @param includeOpacity when true a slider for opacity will be included on
     *            the color picker.
     */
    public ColorChooserStyleParameterEditorPanel(PanelBuilder label, MutableVisualizationStyle style, String paramKey,
            boolean previewable, final boolean includeOpacity)
    {
        super(label, style, paramKey);
        myColor = getParameterValue();
        myColorChooserBT = new JButton(new ColorCircleIcon(myColor));
        myColorChooserBT.setMaximumSize(new Dimension(25, 20));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(10));
        panel.add(myColorChooserBT);
        panel.add(Box.createHorizontalGlue());
        myControlPanel.setLayout(new BorderLayout());
        myControlPanel.add(panel, BorderLayout.CENTER);
        myColorChooserBT.addActionListener(e ->
        {
            if (e.getSource() == myColorChooserBT)
            {
                Color c = ColorPicker.showDialog(SwingUtilities.getWindowAncestor(ColorChooserStyleParameterEditorPanel.this),
                        "Select Color", ((ColorCircleIcon)myColorChooserBT.getIcon()).getColor(), includeOpacity);
                if (c != null)
                {
                    myColor = c;
                    myColorChooserBT.setIcon(new ColorCircleIcon(myColor));
                    setParamValue(myColor);
                }
            }
        });
        setMaximumSize(new Dimension(1000, 35));
        setPreferredSize(new Dimension(100, 35));
    }

    @Override
    public final void update()
    {
        Color cValue = getParameterValue();
        if (!cValue.equals(myColor))
        {
            myColor = cValue;
            EventQueueUtilities.runOnEDT(() -> myColorChooserBT.setIcon(new ColorCircleIcon(myColor)));
        }
    }

    /**
     * Gets the parameter value.
     *
     * @return the parameter value
     */
    private Color getParameterValue()
    {
        Color val = Color.white;
        Object value = getParamValue();
        if (value instanceof Color)
        {
            val = (Color)value;
        }
        return val;
    }
}
