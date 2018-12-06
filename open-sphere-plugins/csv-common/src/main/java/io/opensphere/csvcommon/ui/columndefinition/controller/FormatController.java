package io.opensphere.csvcommon.ui.columndefinition.controller;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.stream.Collectors;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.csvcommon.common.Constants;
import io.opensphere.csvcommon.format.CellFormatSaver;
import io.opensphere.csvcommon.format.CellFormatter;
import io.opensphere.csvcommon.format.factory.CellFormatterFactory;
import io.opensphere.csvcommon.ui.columndefinition.listener.BaseSelectedColumnObserver;
import io.opensphere.csvcommon.ui.columndefinition.model.BeforeAfterTableModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.importer.config.ColumnType;

/**
 * The controller used to control the format drop down within the table. It
 * changes the available formats depending on the selected rows data type. It
 * will also add a format to the system the user requests to.
 *
 */
public class FormatController extends BaseSelectedColumnObserver implements TableModelListener
{
    /**
     * The column definition model.
     */
    private final ColumnDefinitionModel myModel;

    /**
     * The preferences registry.
     */
    private final PreferencesRegistry myPreferencesRegistry;

    /**
     * The previous data type so we can tell if we need to refresh the selected
     * format or not.
     */
    private String myPreviousDataType;

    /**
     * Constructs a new format controller.
     *
     * @param preferencesRegistry The preferences registry.
     * @param model The column definition model.
     */
    public FormatController(PreferencesRegistry preferencesRegistry, ColumnDefinitionModel model)
    {
        super(model);
        myPreferencesRegistry = preferencesRegistry;
        myModel = model;
        myModel.getBeforeAfterTableModel().addTableModelListener(this);
    }

    @Override
    public void close()
    {
        myModel.getBeforeAfterTableModel().removeTableModelListener(this);
        super.close();
    }

    @Override
    public void tableChanged(TableModelEvent e)
    {
        if (e.getType() != TableModelEvent.INSERT)
        {
            return;
        }
        ColumnDefinitionRow row = getSelectedColumn();
        if (row == null || myModel.getAvailableFormats().contains(row.getFormat()))
        {
            return;
        }
        BeforeAfterTableModel tbl = myModel.getBeforeAfterTableModel();
        int n = tbl.getRowCount();
        for (int i = 0; i < n; i++)
        {
            String val = tbl.getRow(i).getAfterValue();
            if (val != null && !val.isEmpty() && !Constants.NO_SAVE_SET.contains(val))
            {
                saveCurrentFormat();
                break;
            }
        }
    }

    @Override
    public void update(Observable o, Object arg)
    {
        super.update(o, arg);
        if (ColumnDefinitionModel.SELECTED_DEFINITION_PROPERTY.equals(arg))
        {
            setPreviousDataType();
            myModel.setAvailableFormats(populateFormats());
        }
        else if (ColumnDefinitionRow.DATA_TYPE_PROPERTY.equals(arg))
        {
            resetFormatIfNeeded();
            setPreviousDataType();
            myModel.setAvailableFormats(populateFormats());
        }
    }

    /**
     * Sets the format to null if the data type has changed.
     */
    private void resetFormatIfNeeded()
    {
        ColumnDefinitionRow selCol = getSelectedColumn();
        if (selCol == null)
        {
            return;
        }
        String type = selCol.getDataType();
        if (!Objects.equals(type, myPreviousDataType))
        {
            selCol.setFormat(detectFormat(type));
        }
    }

    /**
     * Based on the sample data and the current data type, auto detects the most
     * appropriate format.
     *
     * @param dataType The data type to get a format for.
     * @return the format, which may be null
     */
    private String detectFormat(String dataType)
    {
        if (dataType == null)
        {
            return null;
        }
        CellFormatter formatter = new CellFormatterFactory().getFormatter(ColumnType.fromString(dataType), myPreferencesRegistry);
        if (formatter == null)
        {
            return null;
        }
        List<String> fmtList = myModel.getSampleData().stream().map(r -> r.get(getSelectedColumn().getColumnId()))
                .collect(Collectors.toList());
        return formatter.getFormat(fmtList);
    }

    /**
     * Populates the available formats for the selected data type.
     *
     * @return the list of format Strings
     */
    private List<String> populateFormats()
    {
        ColumnDefinitionRow selCol = getSelectedColumn();
        if (selCol == null)
        {
            return new LinkedList<>();
        }

        myModel.getDefinitionTableModel().setFormatEditable(false);
        String dataType = selCol.getDataType();
        if (dataType == null || dataType.isEmpty())
        {
            return new LinkedList<>();
        }

        ColumnType columnType = ColumnType.fromString(dataType);
        CellFormatter formatter = new CellFormatterFactory().getFormatter(columnType, myPreferencesRegistry);
        if (formatter == null)
        {
            return new LinkedList<>();
        }

        myModel.setCanAddFormats(formatter instanceof CellFormatSaver);

        List<String> fmtList = new LinkedList<>(formatter.getKnownPossibleFormats());
        boolean hasFmt = !fmtList.isEmpty();
        if (!hasFmt)
        {
            fmtList.add(selCol.getFormat());
        }
        myModel.getDefinitionTableModel().setFormatEditable(hasFmt);
        Collections.sort(fmtList);
        return fmtList;
    }

    /**
     * Saves the current format for the selected column.
     */
    private void saveCurrentFormat()
    {
        ColumnDefinitionRow selCol = getSelectedColumn();
        if (selCol == null)
        {
            return;
        }
        String type = selCol.getDataType();
        String fmt = selCol.getFormat();
        if (type == null || type.isEmpty() || fmt == null || fmt.isEmpty())
        {
            return;
        }
        CellFormatter formatter = new CellFormatterFactory().getFormatter(ColumnType.fromString(type), myPreferencesRegistry);
        if (formatter instanceof CellFormatSaver)
        {
            ((CellFormatSaver)formatter).saveNewFormat(fmt);
        }
    }

    /**
     * Sets the previous data type based on what the current selected column
     * currently has set for its data type.
     */
    private void setPreviousDataType()
    {
        if (getSelectedColumn() != null)
        {
            myPreviousDataType = getSelectedColumn().getDataType();
        }
        else
        {
            myPreviousDataType = null;
        }
    }
}
