package io.opensphere.core.util.swing.input;

import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import io.opensphere.core.util.swing.GridBagPanel;
import javafx.beans.property.ReadOnlyProperty;

/**
 * A panel that can be used for most views.
 */
public class ViewPanel extends GridBagPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The label style. */
    public static final String LABEL = "label";

    /** The component style. */
    public static final String COMPONENT = "comp";

    /** The double component style. */
    public static final String DOUBLE_COMPONENT = "double";

    /** The double component indented style. */
    public static final String DOUBLE_COMPONENT_IND = "doubleInd";

    /**
     * Constructor.
     */
    public ViewPanel()
    {
        super();
        defineStyles();
    }

    /**
     * Adds a component row to the panel.
     *
     * @param comp the component
     */
    public void addComponent(Component comp)
    {
        style(comp instanceof JCheckBox ? DOUBLE_COMPONENT : DOUBLE_COMPONENT_IND).addRow(comp);
    }

    /**
     * Adds a label and component row to the panel.
     *
     * @param label the label text
     * @param comp the component
     */
    public void addLabelComponent(String label, Component comp)
    {
        addLabelComponent(new JLabel(label), comp);
    }

    /**
     * Adds a label and component row to the panel.
     *
     * @param property the property
     * @param comp the component
     */
    public void addLabelComponent(ReadOnlyProperty<?> property, Component comp)
    {
        addLabelComponent(new JLabel(property.getName() + ":"), comp);
    }

    /**
     * Adds a label and component row to the panel.
     *
     * @param labelAndComponent the label and component
     */
    public void addLabelComponent(JComponent[] labelAndComponent)
    {
        addLabelComponent((JLabel)labelAndComponent[0], labelAndComponent[1]);
    }

    /**
     * Adds a label and component row to the panel.
     *
     * @param label the label text
     * @param comp the component
     */
    public void addLabelComponent(JLabel label, Component comp)
    {
        style(LABEL, COMPONENT).addRow(label, comp);
    }

    /**
     * Adds a heading label.
     *
     * @param text the text
     */
    public void addHeading(String text)
    {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        if (getGBC().gridy > 0)
        {
            addRow(Box.createVerticalStrut(8));
        }
        style(DOUBLE_COMPONENT).addRow(label);
    }

    /**
     * Defines styles.
     */
    private void defineStyles()
    {
        style(LABEL).anchorWest().setInsets(0, 6, 4, 4);
        style(COMPONENT).anchorWest().fillHorizontal().setInsets(0, 0, 4, 0);
        style(DOUBLE_COMPONENT).anchorWest().setGridwidth(2).setInsets(0, 0, 4, 0);
        style(DOUBLE_COMPONENT_IND).anchorWest().setGridwidth(2).setInsets(0, 6, 4, 0);
        style();
    }
}
