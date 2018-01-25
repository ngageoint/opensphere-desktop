package io.opensphere.wfs.envoy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.wfs.envoy.WFSDownloadMonitor.DownloadMonitorChangeEvent;
import io.opensphere.wfs.envoy.WFSDownloadMonitor.DownloadMonitorObject;

/**
 * The Class LayerManager.
 */
public class WFSDownloadMonitorDisplay extends AbstractInternalFrame
{
    /** The Constant Column Name COL_NUMFEATURES. */
    private static final String COL_NUMFEATURES = "Features";

    /** The Constant Column Name COL_REQ_NUMBER. */
    private static final String COL_REQ_NUMBER = "Index";

    /** The Constant Column Name COL_STARTTIME. */
    private static final String COL_STARTTIME = "Query Time";

    /** The Constant Column Name COL_TYPENAME. */
    private static final String COL_TYPENAME = "Layer";

    /** The default message displayed when no row is selected. */
    private static final String DEFAULT_MESSAGE = "Select a row to display details about the request.";

    /** The Constant DEFAULT_WINDOW_SPLIT. */
    private static final float DEFAULT_WINDOW_SPLIT = 0.6f;

    /** Comparator used to sort the list of requests. */
    private static final Comparator<DownloadMonitorObject> LIST_SORTER = new Comparator<DownloadMonitorObject>()
    {
        @Override
        public int compare(DownloadMonitorObject o1, DownloadMonitorObject o2)
        {
            return o2.getRequestNumber() - o1.getRequestNumber();
        }
    };

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(WFSDownloadMonitorDisplay.class);

    /** Default Serial Version UID. */
    private static final long serialVersionUID = 1L;

    /** My column names. */
    private final List<String> myColumnNames = New.list();

    /** My content pane. */
    private final AbstractHUDPanel myContentPane;

    /** My DownloadMonitorChangeEvent listener. */
    private final transient EventListener<DownloadMonitorChangeEvent> myEventListener = new EventListener<DownloadMonitorChangeEvent>()
    {
        @Override
        public void notify(DownloadMonitorChangeEvent event)
        {
            loadRequests();
        }
    };

    /** My monitor controller. */
    private final transient WFSDownloadMonitor myMonitorController;

    /** My request detail text area. */
    private JTextArea myRequestDetailTextArea;

    /** My request table. */
    private JXTable myRequestTable;

    /** My table contents. */
    private transient List<DownloadMonitorObject> myTableData;

    /**
     * Instantiates a new Download Monitor GUI.
     *
     * @param toolbox The toolbox.
     * @param monitor the download monitor that this class gets all its data
     *            from
     */
    public WFSDownloadMonitorDisplay(Toolbox toolbox, WFSDownloadMonitor monitor)
    {
        super();
        myMonitorController = monitor;

        myColumnNames.add(COL_REQ_NUMBER);
        myColumnNames.add(COL_TYPENAME);
        myColumnNames.add(COL_STARTTIME);
        myColumnNames.add(COL_NUMFEATURES);

        setSize(520, 321);
        setPreferredSize(getSize());
        setMinimumSize(new Dimension(400, 247));
        setTitle("Features Download Monitor");
        setOpaque(false);
        setIconifiable(false);
        setClosable(true);
        setResizable(true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        myContentPane = new AbstractHUDPanel(toolbox.getPreferencesRegistry());
        myContentPane.setLayout(new BorderLayout());
        setContentPane(myContentPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getTable(), getDetailsArea());
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(DEFAULT_WINDOW_SPLIT);
        myContentPane.add(splitPane, BorderLayout.CENTER);

        toolbox.getEventManager().subscribe(DownloadMonitorChangeEvent.class, myEventListener);
        loadRequests();
    }

    /**
     * Gets the component that displays the details for a selected request.
     *
     * @return the details area
     */
    private Component getDetailsArea()
    {
        myRequestDetailTextArea = new JTextArea(DEFAULT_MESSAGE);
        myRequestDetailTextArea.setLineWrap(true);
        JScrollPane textSP = AbstractHUDPanel.getJScrollPane(myRequestDetailTextArea);
        textSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        textSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        return textSP;
    }

    /**
     * Gets the component that contains the table of requests.
     *
     * @return the table component
     */
    private Component getTable()
    {
        myRequestTable = new JXTable();
        myRequestTable.setSortable(false);

        JScrollPane tableSP = AbstractHUDPanel.getJScrollPane(myRequestTable);
        tableSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        DownloadMonitorTableModel model = new DownloadMonitorTableModel();
        myRequestTable.setModel(model);
        myRequestTable.packAll();
        myRequestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        myRequestTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                EventQueueUtilities.runOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        int row = myRequestTable.getSelectedRow();
                        if (row == -1)
                        {
                            // No row was selected, so display default
                            // instruction.
                            myRequestDetailTextArea.setText(DEFAULT_MESSAGE);
                        }
                        else
                        {
                            myRequestDetailTextArea.setText(myTableData.get(myRequestTable.getSelectedRow()).toString());
                            // setCaretPosition() is used to reposition the
                            // scrollBar.
                            myRequestDetailTextArea.setCaretPosition(0);
                        }
                    }
                });
            }
        });

        return tableSP;
    }

    /**
     * Load the request lists from the Download Monitor.
     */
    private void loadRequests()
    {
        DownloadMonitorObject currentSelection = null;
        if (myTableData != null && myRequestTable.getSelectedRow() > -1)
        {
            currentSelection = myTableData.get(myRequestTable.getSelectedRow());
        }
        myTableData = Collections.synchronizedList(myMonitorController.getActiveDownloads());
        Collections.sort(myTableData, LIST_SORTER);
        ((DefaultTableModel)myRequestTable.getModel()).fireTableDataChanged();

        // Set the selected row to what it was before the new data was loaded
        if (currentSelection != null && myTableData != null)
        {
            int row = myTableData.indexOf(currentSelection);
            if (row > -1 && row < myRequestTable.getRowCount())
            {
                myRequestTable.setRowSelectionInterval(row, row);
            }
        }
    }

    /**
     * The Class DownloadMonitorTableModel.
     */
    protected class DownloadMonitorTableModel extends DefaultTableModel
    {
        /** Default Serial Version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new Download Monitor table model.
         */
        public DownloadMonitorTableModel()
        {
            super();
        }

        @Override
        public int getColumnCount()
        {
            return myColumnNames.size();
        }

        @Override
        public String getColumnName(int columnIndex)
        {
            return myColumnNames.get(columnIndex);
        }

        @Override
        public int getRowCount()
        {
            if (myTableData != null)
            {
                if (myTableData.size() > 100)
                {
                    return 100;
                }
                return myTableData.size();
            }
            return 0;
        }

        @Override
        public Object getValueAt(int row, int col)
        {
            try
            {
                if (myTableData != null)
                {
                    DownloadMonitorObject rowData = myTableData.get(row);
                    String columnName = myColumnNames.get(col);
                    if (columnName.equals(COL_REQ_NUMBER))
                    {
                        return Integer.valueOf(rowData.getRequestNumber());
                    }
                    else if (columnName.equals(COL_TYPENAME))
                    {
                        return rowData.getType().getDisplayName();
                    }
                    else if (columnName.equals(COL_STARTTIME))
                    {
                        return new SimpleDateFormat().format(rowData.getQueryTime());
                    }
                    else if (columnName.equals(COL_NUMFEATURES))
                    {
                        return rowData.getNumFeatures() == -1 ? "Pending" : Integer.toString(rowData.getNumFeatures());
                    }
                }
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                LOGGER.error("Out of bounds access.", e);
            }
            return "";
        }

        @Override
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }

        @Override
        public void setValueAt(Object aValue, int row, int column)
        {
            // Not implemented - Cells are not editable.
        }
    }
}
