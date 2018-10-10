package io.opensphere.core.security.options;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.table.JTableUtilities;

/**
 * Superclass for options providers that use a table.
 */
public abstract class AbstractTableOptionsProvider extends AbstractSecurityOptionsProvider
{
    /** The delete button. */
    private JButton myDeleteButton;

    /** The details button. */
    private JButton myDetailsButton;

    /** The table. */
    private JTable myTable;

    /**
     * Constructor.
     *
     * @param securityManager The system security manager.
     * @param prefsRegistry The system preferences registry.
     * @param topic The options provider topic.
     */
    public AbstractTableOptionsProvider(SecurityManager securityManager, PreferencesRegistry prefsRegistry, String topic)
    {
        super(securityManager, prefsRegistry, topic);
    }

    @Override
    public JPanel getOptionsPanel()
    {
        myTable = new JXTable();
        myTable.setBackground(TRANSPARENT_COLOR);
        myTable.setForeground(Color.WHITE);
        myTable.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        myTable.addMouseListener(getTableMouseListener());

        myTable.setModel(buildTableModel());

        myTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        myTable.doLayout();
        TableColumn col;
        col = myTable.getColumnModel().getColumn(0);
        col.setPreferredWidth(col.getWidth());
        myTable.setCursor(Cursor.getDefaultCursor());

        JPanel buttonPanel = new JPanel();

        if (myDetailsButton == null)
        {
            createDetailsButton();
        }
        myDetailsButton.setEnabled(false);
        buttonPanel.add(myDetailsButton);

        if (myDeleteButton == null)
        {
            createDeleteButton();
        }
        myDeleteButton.setEnabled(false);
        buttonPanel.add(myDeleteButton);

        JScrollPane scrollPane = new JScrollPane(myTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(400, 400));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Component descriptionComponent = getDescriptionComponent();
        if (descriptionComponent != null)
        {
            panel.add(descriptionComponent, BorderLayout.NORTH);
        }
        panel.add(scrollPane);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        ListSelectionListener listSelectionListener = e ->
        {
            if (myTable.getSelectedRowCount() == 0)
            {
                myDetailsButton.setEnabled(false);
                myDeleteButton.setEnabled(false);
            }
            else
            {
                myDetailsButton.setEnabled(myTable.getSelectedRowCount() == 1);
                myDeleteButton.setEnabled(canDeleteRows(JTableUtilities.getSelectedModelRows(myTable)));
            }
        };
        myTable.getSelectionModel().addListSelectionListener(listSelectionListener);
        return panel;
    }

    /**
     * Build the table model.
     *
     * @return The table model.
     */
    protected abstract TableModel buildTableModel();

    /**
     * Hook method to allow subclasses to decide which rows can be deleted.
     *
     * @param selectedRows The selected rows.
     * @return {@code true} if all the rows can be deleted.
     */
    protected boolean canDeleteRows(int[] selectedRows)
    {
        return true;
    }

    /**
     * Actually delete a row.
     *
     * @param row The row to be deleted.
     */
    protected abstract void deleteRow(int row);

    /**
     * The tool tip text for the delete button.
     *
     * @return The tool tip text.
     */
    protected String getDeleteToolTipText()
    {
        return "Select a row to delete.";
    }

    /**
     * Get the description panel.
     *
     * @return The panel.
     */
    protected abstract Component getDescriptionComponent();

    /**
     * Accessor for the JTable.
     *
     * @return The JTable.
     */
    protected JTable getTable()
    {
        return myTable;
    }

    @Override
    protected void handlePreferenceChange()
    {
        assert EventQueue.isDispatchThread();

        JTable table = getTable();
        if (table != null)
        {
            table.setModel(buildTableModel());
        }
    }

    /**
     * Show a message dialog indicating that rows are about to be deleted.
     *
     * @return {@code true} if the user confirms the deletion.
     */
    protected abstract boolean showDeleteMessageDialog();

    /**
     * Show details for a row.
     *
     * @param row The row.
     */
    protected abstract void showDetails(int row);

    /**
     * Create the delete button.
     */
    private void createDeleteButton()
    {
        myDeleteButton = new JButton("Delete...");
        myDeleteButton.setMargin(ButtonPanel.INSETS_MEDIUM);
        myDeleteButton.setToolTipText(getDeleteToolTipText());
        myDeleteButton.addActionListener(e ->
        {
            if (myTable.getSelectedRowCount() == 0)
            {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(getTable()), "No row selected");
            }
            else
            {
                if (showDeleteMessageDialog())
                {
                    for (int index = 0; index < myTable.getSelectedRowCount(); ++index)
                    {
                        deleteRow(myTable.convertRowIndexToModel(myTable.getSelectedRow()));
                    }
                }
            }
        });
    }

    /**
     * Create the details button.
     */
    private void createDetailsButton()
    {
        myDetailsButton = new JButton("Details");
        myDetailsButton.setMargin(ButtonPanel.INSETS_MEDIUM);
        myDetailsButton.setToolTipText("Select a single row for details.");
        myDetailsButton.addActionListener(e ->
        {
            if (myTable.getSelectedRowCount() == 0)
            {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(getTable()), "No row selected");
            }
            else
            {
                showDetails(myTable.convertRowIndexToModel(myTable.getSelectedRow()));
            }
        });
    }

    /**
     * Get the mouse listener for the table.
     *
     * @return The mouse listener.
     */
    private MouseAdapter getTableMouseListener()
    {
        return new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    int row = myTable.rowAtPoint(e.getPoint());
                    if (row != -1)
                    {
                        showDetails(myTable.convertRowIndexToModel(row));
                    }
                }
            }
        };
    }
}
