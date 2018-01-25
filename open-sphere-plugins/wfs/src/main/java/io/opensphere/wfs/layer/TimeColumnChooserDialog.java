package io.opensphere.wfs.layer;

import java.awt.Component;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.swing.OptionDialog;

/**
 * The Class TimeColumnChooserDialog.
 */
public final class TimeColumnChooserDialog extends OptionDialog
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The time column chooser panel that holds the user's choices. */
    private final TimeColumnChooserPanel myChooserPanel;

    /**
     * Show dialog.
     *
     * @param columns the list of potential time columns
     * @param startColumn the start column
     * @param endColumn the end column
     * @param layerName the layer name
     * @param parent the parent component used to place dialog on-screen
     * @return the start/end time columns selected from this Dialog
     */
    public static TimeColumns showDialog(List<String> columns, String startColumn, String endColumn, String layerName,
            Component parent)
    {
        TimeColumnChooserDialog dialog = new TimeColumnChooserDialog(columns, startColumn, endColumn, layerName, parent);
        dialog.buildAndShow();

        List<String> resultList = dialog.getSelection() == JOptionPane.OK_OPTION ? dialog.getChooser().getSelectedColumns()
                : Collections.<String>emptyList();
        if (!resultList.isEmpty())
        {
            return new TimeColumns(resultList.get(0), resultList.size() > 1 ? resultList.get(1) : null);
        }
        return new TimeColumns(null, null);
    }

    /**
     * Disallow public instantiation. Use static "show" methods instead.
     *
     * @param columns the list of potential time columns
     * @param startColumn the start column
     * @param endColumn the end column
     * @param layerName the layer name
     * @param parent the parent component used to place dialog on-screen
     */
    private TimeColumnChooserDialog(List<String> columns, String startColumn, String endColumn, String layerName,
            Component parent)
    {
        super(parent);
        myChooserPanel = new TimeColumnChooserPanel(layerName);
        myChooserPanel.setComboboxOptions(columns);
        myChooserPanel.setStartColumn(startColumn);
        myChooserPanel.setEndColumn(endColumn);
        setComponent(myChooserPanel);
        setTitle("Choose Time Columns");
    }

    /**
     * Gets the chooser panel that holds the user's decisions.
     *
     * @return the chooser panel
     */
    private TimeColumnChooserPanel getChooser()
    {
        return myChooserPanel;
    }

    /**
     * The Class TimeColumns.
     */
    public static final class TimeColumns
    {
        /** The start time column name. */
        private final String myStartTimeColumn;

        /** The end time column name. */
        private final String myEndTimeColumn;

        /**
         * Time columns.
         *
         * @param startColumn the start-time column name.
         * @param endColumn the end-time column name.
         */
        public TimeColumns(String startColumn, String endColumn)
        {
            myStartTimeColumn = startColumn;
            myEndTimeColumn = endColumn;
        }

        /**
         * Gets the end-time column name.
         *
         * @return the end-time column name
         */
        public String getEndTimeColumn()
        {
            return myEndTimeColumn;
        }

        /**
         * Gets the start-time column name.
         *
         * @return the start-time column name
         */
        public String getStartTimeColumn()
        {
            return myStartTimeColumn;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder(getClass().getSimpleName());
            sb.append('[');
            boolean hadOne = false;
            if (StringUtils.isNotEmpty(myStartTimeColumn))
            {
                sb.append(" StartTimeColumn[").append(myStartTimeColumn).append(']');
                hadOne = true;
            }
            if (StringUtils.isNotEmpty(myEndTimeColumn))
            {
                sb.append(" EndTimeColumn[").append(myEndTimeColumn).append(']');
                hadOne = true;
            }
            if (!hadOne)
            {
                sb.append(" No Time Column Names Selected");
            }
            sb.append(" ]");
            return sb.toString();
        }
    }
}
