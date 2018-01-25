package io.opensphere.shapefile;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.jdesktop.swingx.JXTable;

import io.opensphere.core.util.swing.ListComboBoxModel;
import io.opensphere.shapefile.config.v1.ShapeFileSource;

/**
 * Class to do table name resolution.
 */
class ColumnNameResolver extends JPanel implements ActionListener
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The import config. */
    private final ShapeFileSource myImportConfig;

    /** The change bt. */
    private final JButton myChangeBT;

    /** The model. */
    private final CSVDataPreviewTableModel myModel;

    /** The name chooser combo box. */
    private final JComboBox<String> myNameChooserComboBox;

    /** The preview table. */
    private final JXTable myPreviewTable;

    /**
     * Instantiates a new column name resolver.
     *
     * @param source the source
     * @param valuesSet the values set
     */
    public ColumnNameResolver(ShapeFileSource source, List<List<String>> valuesSet)
    {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        myImportConfig = source;

        myPreviewTable = new JXTable();
        myPreviewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        myPreviewTable.setSortable(false);

        JScrollPane jsp = new JScrollPane(myPreviewTable);
        jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(jsp, BorderLayout.CENTER);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        tablePanel.add(new JLabel("Data Preview ( not all data is shown )"), BorderLayout.NORTH);
        tablePanel.setMaximumSize(new Dimension(1000, 400));
        tablePanel.setPreferredSize(new Dimension(1000, 400));

        JPanel workPanel = new JPanel(new BorderLayout());

        JTextArea jta = new JTextArea();
        jta.setEditable(false);
        jta.setBackground(workPanel.getBackground());
        Font itemFont = new Font(jta.getFont().getName(), Font.BOLD, jta.getFont().getSize() + 2);
        jta.setFont(itemFont);
        jta.setBorder(BorderFactory.createEmptyBorder());
        jta.setText("Please change or assign any desired column names before proceeding,\n"
                + "or if you are satisfied with the existing names click \"next\"");
        workPanel.add(jta, BorderLayout.NORTH);

        myNameChooserComboBox = new JComboBox<>(new ListComboBoxModel<>(myImportConfig.getColumnNames()));
        myChangeBT = new JButton("Change");
        myChangeBT.addActionListener(this);

        JPanel cbPanel = new JPanel(new BorderLayout());
        cbPanel.add(new JLabel("Column Name "), BorderLayout.WEST);
        cbPanel.add(myNameChooserComboBox, BorderLayout.CENTER);
        cbPanel.add(myChangeBT, BorderLayout.EAST);
        cbPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 35, 40));

        workPanel.add(cbPanel, BorderLayout.CENTER);

        add(workPanel, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.SOUTH);

        myModel = new CSVDataPreviewTableModel(myImportConfig, valuesSet);
        myPreviewTable.setModel(myModel);
        myPreviewTable.packAll();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == myChangeBT)
        {
            String currentName = myNameChooserComboBox.getSelectedItem().toString();
            int currentIndex = myNameChooserComboBox.getSelectedIndex();

            String choice = JOptionPane.showInputDialog(this, "Please choose a new name for the column.", currentName);

            boolean done = false;
            while (!done)
            {
                if (choice == null)
                {
                    done = true;
                    // User cancelled
                }
                else if (choice.isEmpty())
                {
                    choice = JOptionPane.showInputDialog(this,
                            "The name cannot be blank!\nPlease choose a new name for the column.",
                            myNameChooserComboBox.getSelectedItem());
                }
                else
                {
                    ArrayList<String> tNames = new ArrayList<>(myImportConfig.getColumnNames());
                    tNames.remove(currentIndex);
                    if (tNames.contains(choice))
                    {
                        choice = JOptionPane.showInputDialog(this, "The name\"" + choice + "\" is already in use.\n"
                                + "Please choose a another name for the column.", currentName);
                    }
                    else
                    {
                        done = true;
                    }
                }
            }

            if (choice != null && !choice.equals(currentName))
            {
                myImportConfig.getColumnNames().remove(currentIndex);
                myImportConfig.getColumnNames().add(currentIndex, choice);
                myModel.fireTableStructureChanged();
                myPreviewTable.packAll();
                myNameChooserComboBox.setModel(new ListComboBoxModel<>(myImportConfig.getColumnNames()));
                myNameChooserComboBox.setSelectedItem(Integer.valueOf(currentIndex));
            }
        }
    }
}
