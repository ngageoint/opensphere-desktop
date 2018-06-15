package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import com.bric.swing.ColorPicker;

import io.opensphere.core.util.swing.ColorCircleIcon;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * The Class AbstractStyleParameterEditorPanel.
 */
public class ComboBoxAndColorChooserEditorPanel extends ComboBoxStyleParameterEditorPanel
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Second parameter. */
    private final String mySecondParameterKey;

    /** The color chooser button. */
    private final JButton myColorChooserButton;

    /** The Color. */
    private Color myColor;

    /**
     * Instantiates a new abstract style parameter editor panel.
     *
     * @param label the label
     * @param style the style
     * @param paramKey1 the param key1
     * @param previewable the previewable
     * @param options1 the options1
     * @param opt1Numeric the opt1 numeric
     * @param opt1ShowNone the opt1 show none
     * @param colorChooserParamKey the color chooser param key
     * @param colorChooserLabel the color chooser label
     */
    public ComboBoxAndColorChooserEditorPanel(PanelBuilder label, MutableVisualizationStyle style, String paramKey1,
            boolean previewable, Collection<? extends Object> options1, boolean opt1Numeric, boolean opt1ShowNone,
            String colorChooserParamKey, String colorChooserLabel)
    {
        super(label, style, paramKey1, previewable, opt1Numeric, opt1ShowNone, options1);

        mySecondParameterKey = colorChooserParamKey;

        myColor = getColorParameterValue();
        myColorChooserButton = new JButton(new ColorCircleIcon(myColor));
        myColorChooserButton.setMaximumSize(new Dimension(25, 20));
        myColorChooserButton.addActionListener(this);

        myComboBoxPanel.add(Box.createHorizontalStrut(5), 1);
        myComboBoxPanel.add(myColorChooserButton, 2);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        super.actionPerformed(e);
        if (e.getSource() == myColorChooserButton)
        {
            Color c = ColorPicker.showDialog(SwingUtilities.getWindowAncestor(this), "Select Color",
                    ((ColorCircleIcon)myColorChooserButton.getIcon()).getColor(), true);
            if (c != null)
            {
                myColor = c;
                myColorChooserButton.setIcon(new ColorCircleIcon(myColor));
                myStyle.setParameter(mySecondParameterKey, myColor, this);
            }
        }
    }

    @Override
    public final void update()
    {
        super.update();

        Color cValue = getColorParameterValue();
        if (!cValue.equals(myColor))
        {
            myColor = cValue;
            EventQueueUtilities.runOnEDT(() -> myColorChooserButton.setIcon(new ColorCircleIcon(myColor)));
        }
    }

    /**
     * Gets the parameter value.
     *
     * @return the parameter value
     */
    private Color getColorParameterValue()
    {
        Color val = Color.white;
        Object value = myStyle.getStyleParameter(mySecondParameterKey).getValue();
        if (value instanceof Color)
        {
            val = (Color)value;
        }
        return val;
    }
}
