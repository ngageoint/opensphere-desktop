package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * The Class AbstractStyleParameterEditorPanel.
 */
@SuppressWarnings("serial")
public class CheckBoxFloatSliderStyleParameterEditorPanel extends FloatSliderStyleParameterEditorPanel
{
    /** The Constant POST_CHECKBOX_LABEL. */
    public static final String POST_CHECKBOX_LABEL = "POST_CHECKBOX_LABEL";

    /** The Slider. */
    private JCheckBox myCheckBox;

    /** The Check box parameter key. */
    private final String myCheckBoxParameterKey;

    /** The Link type. */
    private final SliderVisabilityLinkType myLinkType;

    /** The Post checkbox label. */
    private JLabel myPostCheckboxLabel;

    /**
     * Instantiates a new abstract style parameter editor panel.
     *
     * @param builder the label
     * @param style the style
     * @param paramKey the param key
     * @param previewable the previewable
     * @param textEntry the text entry
     * @param min the min
     * @param max the max
     * @param convertor the convertor
     * @param checkBoxParamKey the check box param key
     * @param cbSliderLinkType the cb slider link type
     */
    public CheckBoxFloatSliderStyleParameterEditorPanel(PanelBuilder builder, MutableVisualizationStyle style, String paramKey,
            boolean previewable, boolean textEntry, double min, double max, IntFloatConvertor convertor, String checkBoxParamKey,
            SliderVisabilityLinkType cbSliderLinkType)
    {
        super(builder, style, paramKey, previewable, textEntry, min, max, convertor);
        myCheckBoxParameterKey = checkBoxParamKey;
        myLinkType = cbSliderLinkType;
        myCheckBox.setSelected(isCheckBoxValue());
        adjustSliderVisibility();
        myCheckBox.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        super.actionPerformed(e);
        if (e.getSource() == myCheckBox)
        {
            setParamValue(myCheckBoxParameterKey, Boolean.valueOf(myCheckBox.isSelected()));
            adjustSliderVisibility();
        }
    }

    /**
     * Update.
     */
    @Override
    public final void update()
    {
        super.update();
        boolean val = isCheckBoxValue();
        if (val != myCheckBox.isSelected())
        {
            final boolean fVal = val;
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    myCheckBox.setSelected(fVal);
                    adjustSliderVisibility();
                }
            });
        }
    }

    @Override
    protected Component getPrefixComponent(PanelBuilder builder)
    {
        Box b = Box.createHorizontalBox();
        myCheckBox = new JCheckBox();
        b.add(myCheckBox);
        b.add(Box.createHorizontalStrut(4));
        Object postCBLabel = builder.getOtherParameter(POST_CHECKBOX_LABEL, null);
        if (postCBLabel != null)
        {
            myPostCheckboxLabel = new JLabel(postCBLabel.toString());
            b.add(myPostCheckboxLabel);
            b.add(Box.createHorizontalStrut(2));
        }
        return b;
    }

    /**
     * Adjust slider visibility.
     */
    private void adjustSliderVisibility()
    {
        if (myLinkType == SliderVisabilityLinkType.NOT_LINKED_TO_CHECKBOX)
        {
            getSliderPanel().setVisible(true);
            myPostCheckboxLabel.setVisible(true);
        }
        else if (myLinkType == SliderVisabilityLinkType.VISIBLE_WHEN_CHECKED)
        {
            getSliderPanel().setVisible(myCheckBox.isSelected());
            myPostCheckboxLabel.setVisible(myCheckBox.isSelected());
        }
        else
        // VISIBLE_WHEN_NOT_CHECKED
        {
            myPostCheckboxLabel.setVisible(!myCheckBox.isSelected());
            getSliderPanel().setVisible(!myCheckBox.isSelected());
        }
    }

    /**
     * Gets the parameter value.
     *
     * @return the parameter value
     */
    private boolean isCheckBoxValue()
    {
        boolean val = false;
        Object value = myStyle.getStyleParameterValue(myCheckBoxParameterKey);
        if (value instanceof Boolean)
        {
            val = ((Boolean)value).booleanValue();
        }
        return val;
    }

    /**
     * The Enum SliderVisabilityLinkType.
     */
    public enum SliderVisabilityLinkType
    {
        /** The NOT_LINKED_TO_CHECKBOX. */
        NOT_LINKED_TO_CHECKBOX,

        /** The VISIBLE_WHEN_CHECKED. */
        VISIBLE_WHEN_CHECKED,

        /** The VISIBLE_WHEN_NOT_CHECKED. */
        VISIBLE_WHEN_NOT_CHECKED
    }
}
