package io.opensphere.mantle.icon.impl.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;

import io.opensphere.core.util.AwesomeIcon;
import io.opensphere.core.util.FontIconEnum;

@SuppressWarnings({ "javadoc", "serial" })
public class IconBuilderChoiceDialog extends JOptionPane
{
    private FontIconEnum myIconValue;

    private final DefaultListModel<FontIconEnum> myListModel = new DefaultListModel<>();

    private JList<FontIconEnum> myList;

    public IconBuilderChoiceDialog()
    {
        setLayout(new BorderLayout());
        add(createInternalTree(), BorderLayout.WEST);
        add(createInternalPane(new Dimension(310, 300)), BorderLayout.CENTER);
    }

    private JTree createInternalTree()
    {
        Vector<String> treeValues = new Vector<>();
        treeValues.add("FontAwesome Icons");
        treeValues.add("Govicon Icons");
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
                default:
                    break;
            }
        });

        return tree;
    }

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

    @Override
    public Object getValue()
    {
        return myIconValue;
    }

    private final class IconCellRenderer extends JLabel implements ListCellRenderer<FontIconEnum>
    {
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
