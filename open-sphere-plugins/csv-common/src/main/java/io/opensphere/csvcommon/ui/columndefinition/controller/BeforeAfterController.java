package io.opensphere.csvcommon.ui.columndefinition.controller;

import java.text.ParseException;
import java.util.List;
import java.util.Observable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.common.Constants;
import io.opensphere.csvcommon.format.CellFormatter;
import io.opensphere.csvcommon.format.factory.CellFormatterFactory;
import io.opensphere.csvcommon.ui.columndefinition.listener.BaseSelectedColumnObserver;
import io.opensphere.csvcommon.ui.columndefinition.model.BeforeAfterRow;
import io.opensphere.csvcommon.ui.columndefinition.model.BeforeAfterTableModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.importer.config.ColumnType;

/**
 * The controller for the Before After panel that populates the before and after
 * values within the table model, based on the selected row.
 *
 */
public class BeforeAfterController extends BaseSelectedColumnObserver
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(BeforeAfterController.class);

    /**
     * The column definition model.
     */
    private final ColumnDefinitionModel myModel;

    /**
     * The preferences registry used to get the list tool date format.
     */
    private final PreferencesRegistry myPreferencesRegistry;

    /**
     * Constructs a before after controller.
     *
     * @param preferencesRegistry The system toolbox.
     * @param model The column definition model.
     */
    public BeforeAfterController(PreferencesRegistry preferencesRegistry, ColumnDefinitionModel model)
    {
        super(model);
        myPreferencesRegistry = preferencesRegistry;
        myModel = model;
    }

    @Override
    public void update(Observable o, Object arg)
    {
        super.update(o, arg);
        if (ColumnDefinitionModel.SELECTED_DEFINITION_PROPERTY.equals(arg))
        {
            generateBeforeAfterData();
            setColumnNames();
        }
        else if (ColumnDefinitionRow.FORMAT_PROPERTY.equals(arg))
        {
            generateBeforeAfterData();
        }
    }

    /**
     * Generates the table data for the before and after columns in the before
     * and after table.
     */
    private void generateBeforeAfterData()
    {
        BeforeAfterTableModel table = myModel.getBeforeAfterTableModel();
        table.clear();

        if (getSelectedColumn() != null)
        {
            int columnIndex = getSelectedColumn().getColumnId();
            List<? extends List<? extends String>> sampleData = myModel.getSampleData();

            String dataType = getSelectedColumn().getDataType();

            CellFormatter cellFormatter = null;
            String systemFormat = null;

            if (StringUtils.isNotEmpty(dataType))
            {
                ColumnType columnType = ColumnType.fromString(dataType);
                cellFormatter = new CellFormatterFactory().getFormatter(columnType, myPreferencesRegistry);

                if (cellFormatter != null)
                {
                    systemFormat = cellFormatter.getSystemFormat();
                }
            }

            String beforeFormatString = getSelectedColumn().getFormat();

            List<BeforeAfterRow> beforeAfterRows = New.list();
            for (List<? extends String> row : sampleData)
            {
                BeforeAfterRow beforeAfter = new BeforeAfterRow();
                String sampleCell = row.get(columnIndex);

                beforeAfter.setBeforeValue(sampleCell);

                String afterValue = Constants.NON_FORMATTED_TEXT;
                if (cellFormatter != null)
                {
                    try
                    {
                        Object formattedValue = cellFormatter.formatCell(sampleCell, beforeFormatString);
                        afterValue = cellFormatter.fromObjectValue(formattedValue, systemFormat);
                    }
                    catch (ParseException e)
                    {
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug(e.getMessage(), e);
                        }

                        afterValue = Constants.ERROR_LABEL;
                    }
                }

                beforeAfter.setAfterValue(afterValue);

                beforeAfterRows.add(beforeAfter);
            }

            table.addRows(beforeAfterRows);
        }
    }

    /**
     * Sets the column names.
     */
    private void setColumnNames()
    {
        BeforeAfterTableModel table = myModel.getBeforeAfterTableModel();
        table.setColumnNamePrefix("");

        if (getSelectedColumn() != null)
        {
            table.setColumnNamePrefix(getSelectedColumn().getColumnName());
        }
    }
}
