package io.opensphere.mantle.icon.impl.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Label;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;

import io.opensphere.core.util.AwesomeIcon;
import io.opensphere.core.util.FontIconEnum;
import io.opensphere.core.util.GovIcon;
import io.opensphere.core.util.MilitaryRankIcon;

/**
 * The IconBuilderChocieDialog class. Allows a user to select an icon from a
 * tree-list construction.
 */
public class IconBuilderChoiceDialog extends JPanel
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The list model containing icons available for selection. */
    private final DefaultListModel<FontIconEnum> myListModel = new DefaultListModel<>();

    /** The selected icon value. */
    private FontIconEnum myIconValue;

    /** The icon list. */
    private JList<FontIconEnum> myList;

    /** Constructs a new IconBuilderChoiceDialog. */
    public IconBuilderChoiceDialog()
    {
        setLayout(new BorderLayout());

        add(new Label("Select an icon type and graphic from the list below."));
        add(createInternalTree(), BorderLayout.WEST);
        add(createInternalPane(new Dimension(310, 300)), BorderLayout.CENTER);
    }

    /**
     * Constructs the tree of icon types.
     *
     * @return the type tree
     */
    private JTree createInternalTree()
    {
        Vector<String> treeValues = new Vector<>();
        // Temporarily hard-code icon values.
        treeValues.add("FontAwesome Icons");
        treeValues.add("Government Icons");
        treeValues.add("Military Rank Icons");

        JTree tree = new JTree(treeValues);
        tree.addTreeSelectionListener((evt) ->
        {
            String selected = evt.getPath().getLastPathComponent().toString();

            switch (selected)
            {
                case "FontAwesome Icons":
                    addElementsToList(8, AwesomeIcon.values());
                    break;
                case "Government Icons":
                    addElementsToList(8, GovIcon.values());
                    break;
                case "Military Rank Icons":
                    addElementsToList(12, MilitaryRankIcon.values());
                    break;
                default:
                    break;
            }
        });

        return tree;
    }

    /**
     * Constructs the {@link #myList} and containing scroll pane.
     *
     * @param preferredSize the preferred & maximum dimensions for the pane
     * @return the scroll pane
     */
    private JScrollPane createInternalPane(Dimension preferredSize)
    {
        JScrollPane scrollPane = new JScrollPane();

        myList = new JList<>();
        myList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        myList.setModel(myListModel);
        myList.setCellRenderer(new IconCellRenderer());
        myList.addListSelectionListener((evt) ->
        {
            myIconValue = myList.getSelectedValue();
        });

        scrollPane.setViewportView(myList);
        scrollPane.setMaximumSize(preferredSize);
        scrollPane.setPreferredSize(preferredSize);

        return scrollPane;
    }

    /**
     * Adds an arbitrary number of FontIconEnum elements to the
     * {@link #myListModel}. Additionally sets the number of icons visible
     * before the {@link #myList} row wraps.
     *
     * @param cellsPerRow the number of icons per wrapped row
     * @param values the icons to add
     */
    private void addElementsToList(int cellsPerRow, FontIconEnum... values)
    {
        myListModel.clear();

        for (FontIconEnum value : values)
        {
            myListModel.addElement(value);
        }

        myListModel.trimToSize();
        myList.setVisibleRowCount(myListModel.getSize() / cellsPerRow);
    }

    /**
     * Gets the selected icon.
     *
     * @return {@link #myIconValue}
     */
    public FontIconEnum getValue()
    {
        return myIconValue;
    }

    /** A cell renderer for FontIconEnum values inside {@link #myList}. */
    private final class IconCellRenderer extends JLabel implements ListCellRenderer<FontIconEnum>
    {
        /** serialVersionUID. */
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<? extends FontIconEnum> list, FontIconEnum value, int index,
                boolean isSelected, boolean cellHasFocus)
        {
            if (value != null)
            {
                setText(value.getFontCode());
                setFont(value.getFont().deriveFont(24f));
            }

            return this;
        }

    }
}
