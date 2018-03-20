package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import io.opensphere.core.util.swing.ComponentUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * The Class AbstractStyleParameterEditorPanel.
 */
public class CheckBoxStyleParameterEditorPanel extends AbstractStyleParameterEditorPanel implements ActionListener
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Slider. */
    private final JCheckBox myCheckBox;

    /**
     * Instantiates a new abstract style parameter editor panel.
     *
     * @param label the {@link PanelBuilder}
     * @param style the style
     * @param paramKey the param key
     * @param previewable the previewable
     */
    public CheckBoxStyleParameterEditorPanel(PanelBuilder label, MutableVisualizationStyle style, String paramKey,
            boolean previewable)
    {
        super(label, style, paramKey);
        myCheckBox = new JCheckBox();
        myCheckBox.setSelected(isParameterValue());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
//        panel.add(Box.createHorizontalStrut(5));
        panel.add(myCheckBox);
        myControlPanel.setLayout(new BorderLayout());
        myControlPanel.add(panel, BorderLayout.CENTER);
        myCheckBox.addActionListener(this);

        int panelHeight = getPanelHeightFromBuilder();
        ComponentUtilities.setPreferredHeight(myControlPanel, panelHeight);
        showMessage(Boolean.valueOf(isParameterValue()));
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == myCheckBox && myCheckBox.isSelected() != isParameterValue())
        {
            Boolean val = Boolean.valueOf(myCheckBox.isSelected());
            setParamValue(val);
            showMessage(val);
        }
    }

    /**
     * Update.
     */
    @Override
    public final void update()
    {
        boolean val = isParameterValue();
        if (val != myCheckBox.isSelected())
        {
            final boolean fVal = val;
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    myCheckBox.setSelected(fVal);
                    showMessage(Boolean.valueOf(fVal));
                }
            });
        }
    }

    /**
     * Gets the parameter value.
     *
     * @return the parameter value
     */
    private boolean isParameterValue()
    {
        boolean val = false;
        Object value = getParamValue();
        if (value instanceof Boolean)
        {
            val = ((Boolean)value).booleanValue();
        }
        return val;
    }
}
