package io.opensphere.mantle.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.ListComboBoxModel;

/**
 * The Class TableColumnMoverDialog.
 */
public class TableColumnMoverDialog extends JDialog implements ActionListener
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Available columns. */
    private List<ColumnSelectProxy> myAvailableColumns;

    /** The Cancel button. */
    private JButton myCancelButton;

    /** The Column to move. */
    private final TableColumn myColumnToMove;

    /** The Column to move combo box. */
    private JComboBox<ColumnSelectProxy> myColumnToMoveComboBox;

    /** The Column to move proxy. */
    private ColumnSelectProxy myColumnToMoveProxy;

    /** The Move type. */
    private static final MoveType MOVE_TYPE = MoveType.TO_FRONT;

    /** The Move type combo box. */
    private JComboBox<MoveType> myMoveTypeComboBox;

    /** The OK button. */
    private JButton myOKButton;

    /** The Table. */
    private final JTable myTable;

    /** The Target column combo box. */
    private JComboBox<ColumnSelectProxy> myTargetColumnComboBox;

    /** The Target panel. */
    private JPanel myTargetPanel;

    /**
     * Instantiates a new table column mover dialog.
     *
     * @param jt the {@link JTable}
     */
    public TableColumnMoverDialog(JTable jt)
    {
        this(jt, null);
    }

    /**
     * Instantiates a new table column mover dialog.
     *
     * @param jt the {@link JTable}
     * @param colToMove the column to move
     */
    public TableColumnMoverDialog(JTable jt, TableColumn colToMove)
    {
        super(SwingUtilities.getWindowAncestor(jt), "Table Column Mover", ModalityType.APPLICATION_MODAL);
        myTable = jt;
        myColumnToMove = colToMove;
        setSize(300, 300);
        setLocationRelativeTo(jt.getParent());
        setResizable(false);
        buildMainPanel();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (Utilities.sameInstance(e.getSource(), myColumnToMoveComboBox))
        {
            myColumnToMoveProxy = (ColumnSelectProxy)myColumnToMoveComboBox.getSelectedItem();
            rebuildTargetColumnComboBox();
        }
        else if (Utilities.sameInstance(e.getSource(), myMoveTypeComboBox))
        {
            MoveType mt = (MoveType)myMoveTypeComboBox.getSelectedItem();
            rebuildTargetColumnComboBox();
            myTargetPanel.setVisible(mt == MoveType.BEFORE_COLUMN || mt == MoveType.AFTER_COLUMN);
        }
        else if (Utilities.sameInstance(e.getSource(), myOKButton))
        {
            myColumnToMoveProxy = (ColumnSelectProxy)myColumnToMoveComboBox.getSelectedItem();
            MoveType mt = (MoveType)myMoveTypeComboBox.getSelectedItem();
            switch (mt)
            {
                case AFTER_COLUMN:
                case BEFORE_COLUMN:
                    ColumnSelectProxy targetColProxy = (ColumnSelectProxy)myTargetColumnComboBox.getSelectedItem();
                    int targetColIndex = targetColProxy.getIndex();
                    int sourceIndex = myColumnToMoveProxy.getIndex();
                    if (mt == MoveType.BEFORE_COLUMN)
                    {
                        if (sourceIndex < targetColIndex)
                        {
                            targetColIndex--;
                        }
                    }
                    else
                    {
                        if (sourceIndex > targetColIndex)
                        {
                            targetColIndex++;
                        }
                    }
                    myTable.moveColumn(sourceIndex, targetColIndex);
                    break;
                case TO_BACK:
                    myTable.moveColumn(myColumnToMoveProxy.getIndex(), myTable.getColumnCount() - 1);
                    break;
                case TO_FRONT:
                    myTable.moveColumn(myColumnToMoveProxy.getIndex(), 0);
                    break;
                default:
            }
            setVisible(false);
            dispose();
        }
        else if (Utilities.sameInstance(e.getSource(), myCancelButton))
        {
            setVisible(false);
            dispose();
        }
    }

    /**
     * Builds the button panel.
     *
     * @param d the d
     * @return the j panel
     */
    private JPanel buildButtonPanel(Dimension d)
    {
        myOKButton = new JButton("OK");
        myOKButton.addActionListener(this);
        myCancelButton = new JButton("Cancel");
        myCancelButton.addActionListener(this);
        JPanel btPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        btPanel.add(myOKButton);
        btPanel.add(myCancelButton);
        btPanel.setMaximumSize(d);
        btPanel.setMaximumSize(d);
        btPanel.setPreferredSize(d);
        btPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 5, 40));
        return btPanel;
    }

    /**
     * Builds the main panel.
     */
    private void buildMainPanel()
    {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        Dimension d = new Dimension(250, 50);

        JTextArea jta = buildTextArea(mainPanel, d);
        mainPanel.add(jta);

        myAvailableColumns = New.list();
        for (int i = 0; i < myTable.getColumnModel().getColumnCount(); i++)
        {
            TableColumn col = myTable.getColumnModel().getColumn(i);
            ColumnSelectProxy proxy = new ColumnSelectProxy(i, col);
            if (myColumnToMove != null && Utilities.sameInstance(col, myColumnToMove))
            {
                myColumnToMoveProxy = proxy;
            }
            myAvailableColumns.add(proxy);
        }

        List<ColumnSelectProxy> sourceList = New.list(myAvailableColumns);
        Collections.sort(sourceList, (o1, o2) -> o1.toString().compareTo(o2.toString()));
        myColumnToMoveComboBox = new JComboBox<>(new ListComboBoxModel<>(sourceList));
        if (myColumnToMoveProxy != null)
        {
            myColumnToMoveComboBox.setSelectedItem(myColumnToMoveProxy);
        }
        myColumnToMoveComboBox.addActionListener(this);
        JPanel mcPanel = new JPanel(new BorderLayout());
        mcPanel.setMaximumSize(d);
        mcPanel.setMaximumSize(d);
        mcPanel.setPreferredSize(d);
        mcPanel.add(myColumnToMoveComboBox, BorderLayout.CENTER);
        mainPanel.add(mcPanel);

        JPanel moveTypePanel = buildMoveTypePanel(d);
        mainPanel.add(moveTypePanel);

        buildTargetPanel(d);
        mainPanel.add(myTargetPanel);
        mainPanel.add(Box.createVerticalGlue());

        JPanel btPanel = buildButtonPanel(d);
        mainPanel.add(btPanel);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        setContentPane(mainPanel);
    }

    /**
     * Builds the move type panel.
     *
     * @param d the d
     * @return the j panel
     */
    private JPanel buildMoveTypePanel(Dimension d)
    {
        JPanel moveTypePanel = new JPanel(new BorderLayout());
        myMoveTypeComboBox = new JComboBox<>(MoveType.values());
        myMoveTypeComboBox.setSelectedItem(MOVE_TYPE);
        myMoveTypeComboBox.addActionListener(this);
        moveTypePanel.setMaximumSize(d);
        moveTypePanel.setMaximumSize(d);
        moveTypePanel.setPreferredSize(d);
        moveTypePanel.add(myMoveTypeComboBox, BorderLayout.CENTER);
        return moveTypePanel;
    }

    /**
     * Builds the target panel.
     *
     * @param d the d
     */
    private void buildTargetPanel(Dimension d)
    {
        myTargetPanel = new JPanel(new BorderLayout());
        myTargetColumnComboBox = new JComboBox<>(new ListComboBoxModel<>(myAvailableColumns));
        myTargetPanel.add(myTargetColumnComboBox, BorderLayout.CENTER);
        myTargetPanel.setVisible(false);
        myTargetPanel.setMaximumSize(d);
        myTargetPanel.setMaximumSize(d);
        myTargetPanel.setPreferredSize(d);
    }

    /**
     * Builds the text area.
     *
     * @param mainPanel the main panel
     * @param d the d
     * @return the j text area
     */
    private JTextArea buildTextArea(JPanel mainPanel, Dimension d)
    {
        JTextArea jta = new JTextArea("Select the column to move and the type of move to make.");
        jta.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        jta.setLineWrap(true);

        jta.setMaximumSize(d);
        jta.setMaximumSize(d);
        jta.setPreferredSize(d);
        jta.setFont(jta.getFont().deriveFont(Font.BOLD, jta.getFont().getSize() + 2));
        jta.setWrapStyleWord(true);
        jta.setBackground(mainPanel.getBackground());
        jta.setEditable(false);
        return jta;
    }

    /**
     * Rebuild target column combo box.
     */
    private void rebuildTargetColumnComboBox()
    {
        List<ColumnSelectProxy> list = New.list(myAvailableColumns);
        list.remove(myColumnToMoveProxy);
        myTargetColumnComboBox.setModel(new ListComboBoxModel<>(list));
    }

    /**
     * The Enum MoveType.
     */
    public enum MoveType
    {
        /** I like to. */
        AFTER_COLUMN("Move After Column"),

        /** Move it, move it. */
        BEFORE_COLUMN("Move Before Column"),

        /** I like to. */
        TO_BACK("Move To Back"),

        /** Move it, move it. */
        TO_FRONT("Move To Front"),

        ;

        /** The Label. */
        private String myLabel;

        /**
         * Instantiates a new move type.
         *
         * @param label the label
         */
        MoveType(String label)
        {
            myLabel = label;
        }

        /**
         * Gets the label.
         *
         * @return the label
         */
        public String getLabel()
        {
            return myLabel;
        }

        @Override
        public String toString()
        {
            return myLabel;
        }
    }

    /**
     * The Class ColumnSelectProxy.
     */
    private static class ColumnSelectProxy
    {
        /** The Index. */
        private final int myIndex;

        /** The Table column. */
        private final TableColumn myTableColumn;

        /**
         * Instantiates a new column select proxy.
         *
         * @param index the index
         * @param tc the tc
         */
        public ColumnSelectProxy(int index, TableColumn tc)
        {
            myTableColumn = tc;
            myIndex = index;
        }

        /**
         * Gets the index.
         *
         * @return the index
         */
        public int getIndex()
        {
            return myIndex;
        }

        @Override
        public String toString()
        {
            return myTableColumn.getHeaderValue().toString();
        }
    }
}
