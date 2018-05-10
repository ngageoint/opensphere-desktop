package io.opensphere.csv.ui.columndefinition.controller;

import java.util.List;
import java.util.Observable;

import org.apache.commons.lang3.StringUtils;

import gnu.trove.map.hash.TIntObjectHashMap;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.csv.ui.columndefinition.validator.ColumnDefinitionValidator;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.common.Utilities;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.ui.columndefinition.controller.BeforeAfterController;
import io.opensphere.csvcommon.ui.columndefinition.controller.FormatController;
import io.opensphere.csvcommon.ui.columndefinition.controller.TypeController;
import io.opensphere.csvcommon.ui.columndefinition.listener.BaseSelectedColumnObserver;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionTableModel;
import io.opensphere.importer.config.ColumnType;
import io.opensphere.importer.config.SpecialColumn;

/**
 * The main controller for the column definition panel. It populates the column
 * definition table and applies the changes made by the user to the overall
 * wizard model.
 *
 */
public class ColumnDefinitionController extends BaseSelectedColumnObserver
{
    /**
     * The controller used to control the before after panel.
     */
    private final BeforeAfterController myBeforeAfterController;

    /**
     * The controller used to control the available formats.
     */
    private final FormatController myFormatController;

    /**
     * The main column definition model.
     */
    private final ColumnDefinitionModel myModel;

    /**
     * The controller used to control the available types.
     */
    private final TypeController myTypeController;

    /**
     * The validator used to validate the inputs.
     */
    private final ColumnDefinitionValidator myValidator;

    /**
     * Constructs a new Column definition controller.
     *
     * @param preferencesRegistry The system toolbox.
     * @param model The column definition model.
     * @param sampler The cell sampler.
     */
    public ColumnDefinitionController(PreferencesRegistry preferencesRegistry, ColumnDefinitionModel model, CellSampler sampler)
    {
        super(model);
        myModel = model;
        myModel.setSampleData(sampler.getBeginningSampleCells());
        myBeforeAfterController = new BeforeAfterController(preferencesRegistry, model);
        myFormatController = new FormatController(preferencesRegistry, model);
        myTypeController = new TypeController(model);
        myValidator = new ColumnDefinitionValidator(model);
        populateColumnTable();
    }

    /**
     * Unsubscribes from listening to model events.
     */
    @Override
    public void close()
    {
        myBeforeAfterController.close();
        myFormatController.close();
        myTypeController.close();
        myValidator.close();
    }

    /**
     * Set the isImport flag on all columns, only applying changes once.
     *
     * @param imp If all columns should be set to import.
     */
    public void setAllColumnsImport(boolean imp)
    {
        ColumnDefinitionRow selectedColumn = getSelectedColumn();
        if (selectedColumn != null)
        {
            selectedColumn.deleteObserver(this);
        }
        for (int row = 0; row < myModel.getDefinitionTableModel().getRowCount(); row++)
        {
            myModel.getDefinitionTableModel().getRow(row).setIsImport(imp);
        }
        if (selectedColumn != null)
        {
            selectedColumn.addObserver(this);
        }
        myValidator.validate();
        applyChanges();
    }

    @Override
    public void update(Observable o, Object arg)
    {
        super.update(o, arg);
        if (o instanceof ColumnDefinitionRow)
        {
            applyChanges();
        }
    }

    /**
     * Applies the changes made to the CSV wizard model.
     */
    private void applyChanges()
    {
        ColumnDefinitionTableModel columnTable = myModel.getDefinitionTableModel();
        CSVParseParameters parameters = myModel.getSelectedParameters();

        List<String> columnNames = New.list();
        List<SpecialColumn> specialColumns = New.list();
        List<Integer> ignoreColumns = New.list();

        for (int i = 0; i < columnTable.getRowCount(); i++)
        {
            ColumnDefinitionRow row = columnTable.getRow(i);

            columnNames.add(row.getColumnName());

            String dataType = row.getDataType();

            if (StringUtils.isNotEmpty(dataType))
            {
                SpecialColumn specialColumn = new SpecialColumn();
                specialColumn.setColumnIndex(row.getColumnId());
                specialColumn.setColumnType(ColumnType.fromString(dataType));
                specialColumn.setFormat(row.getFormat());

                specialColumns.add(specialColumn);
            }

            if (!row.isImport())
            {
                ignoreColumns.add(Integer.valueOf(row.getColumnId()));
            }
        }

        parameters.setColumnNames(columnNames);
        parameters.getSpecialColumns().clear();
        parameters.getSpecialColumns().addAll(specialColumns);
        parameters.setColumnsToIgnore(ignoreColumns);
    }

    /**
     * Associates specific data types to the columns.
     *
     * @param rows The rows defining each column.
     */
    private void associateDataTypes(List<ColumnDefinitionRow> rows)
    {
        CSVParseParameters parameters = myModel.getSelectedParameters();

        TIntObjectHashMap<SpecialColumn> specialColumnMap = Utilities.createSpecialColumnMap(parameters.getSpecialColumns());

        for (ColumnDefinitionRow row : rows)
        {
            SpecialColumn special = specialColumnMap.get(row.getColumnId());

            if (special != null)
            {
                if (special.getColumnType() != null)
                {
                    row.setDataType(special.getColumnType().toString());
                }

                row.setFormat(special.getFormat());
            }
        }
    }

    /**
     * Creates the columns based on the cell sampler.
     *
     * @return The list of columns with their column id's and column names
     *         filled.
     */
    private List<ColumnDefinitionRow> createColumnRows()
    {
        List<ColumnDefinitionRow> columnRows = New.list();

        int index = 0;
        for (String columnName : myModel.getSelectedParameters().getColumnNames())
        {
            ColumnDefinitionRow columnRow = new ColumnDefinitionRow();
            columnRow.setColumnId(index);
            columnRow.setColumnName(columnName);

            if (myModel.getSelectedParameters().getColumnsToIgnore().contains(Integer.valueOf(index)))
            {
                columnRow.setIsImport(false);
            }
            else
            {
                columnRow.setIsImport(true);
            }

            columnRows.add(columnRow);

            index++;
        }

        return columnRows;
    }

    /**
     * Populates the column table with the columns and their types from the csv.
     */
    private void populateColumnTable()
    {
        List<ColumnDefinitionRow> rows = createColumnRows();
        associateDataTypes(rows);

        myModel.getDefinitionTableModel().addRows(rows);

        if (!rows.isEmpty())
        {
            myModel.setSelectedDefinition(rows.get(0));
        }
    }
}
