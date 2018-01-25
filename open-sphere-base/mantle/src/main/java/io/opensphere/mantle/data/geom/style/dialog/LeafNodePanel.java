package io.opensphere.mantle.data.geom.style.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * The Class LeafNodePanel.
 *
 * A panel for the node in the tree that has the checkbox and selectable label. Also holds a reference to our node user object
 * data ( i.e. the data type information and node type ).
 */
public class LeafNodePanel extends JPanel
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Check box. */
    private final JCheckBox myCheckBox;

    /** The Data type node. */
    private final transient DataTypeNodeUserObject myDataTypeNode;

    /** The Label. */
    private final SelectableLabel myLabel;

    /**
     * Instantiates a new leaf node panel.
     *
     * @param lbGrp the SelectableLabelGroup for the node.
     * @param obj the {@link DataTypeNodeUserObject} for the node
     * @param showCheckBox true to show the checkbox for the node, false to hide it.
     */
    public LeafNodePanel(SelectableLabelGroup lbGrp, DataTypeNodeUserObject obj, boolean showCheckBox)
    {
        super(new BorderLayout());
        setMaximumSize(new Dimension(1000, VisualizationStyleDataTypeTreePanel.NODE_HEIGHT_PIXELS));
        myDataTypeNode = obj;

        myCheckBox = new JCheckBox();
        myCheckBox.setToolTipText("SELECT checkbox to ACTIVATE individual style for \"" + obj.getDisplayName() + "\"");
        myCheckBox.setMargin(new Insets(0, 0, 0, 0));
        myCheckBox.setSelected(myDataTypeNode.isChecked());
        myCheckBox.setVisible(showCheckBox);
        myLabel = new SelectableLabel(obj.getDisplayName(), myDataTypeNode.isSelected());

        if ("Default Styles".equals(obj.getDisplayName()))
        {
            myLabel.setToolTipText("SELECT name to EDIT default style settings for all types that are NOT CHECKED.");
        }
        else
        {
            myLabel.setToolTipText(
                    "SELECT name to edit individual style settings for \"" + obj.getDisplayName() + "\" that apply if ACTIVE");
        }
        lbGrp.addLabel(myLabel);
        JPanel subPanel = new JPanel();
        subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.X_AXIS));

        subPanel.add(myCheckBox);
        subPanel.add(Box.createHorizontalStrut(2));
        subPanel.add(myLabel);
        add(subPanel, BorderLayout.WEST);

        myLabel.addActionListener(e -> myDataTypeNode.setSelected(myLabel.isSelected()));
        myCheckBox.addActionListener(e -> styleActivated());
    }

    /**
     * An event handler used to update the checked / selected state of the data type node and label. The values of each are
     * bound to the checked state of the {@link #myCheckBox} field.
     */
    private void styleActivated()
    {
        myDataTypeNode.setChecked(myCheckBox.isSelected(), true);
        myLabel.setSelected(myCheckBox.isSelected(), true);
    }

    /**
     * Gets the check box.
     *
     * @return the check box
     */
    public JCheckBox getCheckBox()
    {
        return myCheckBox;
    }

    /**
     * Gets the selectable label.
     *
     * @return the selectable label
     */
    public SelectableLabel getSelectableLabel()
    {
        return myLabel;
    }
}
