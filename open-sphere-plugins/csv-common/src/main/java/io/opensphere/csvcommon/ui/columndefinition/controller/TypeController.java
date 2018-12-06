package io.opensphere.csvcommon.ui.columndefinition.controller;

import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.detect.ValueWithConfidence;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.location.model.LatLonColumnResults;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionTableModel;
import io.opensphere.importer.config.ColumnType;

/**
 * The controller that populates the available data types drop down within the
 * column definition table. It will filter out types already selected and order
 * them in high probability order for given columns based on the detector
 * outputs.
 *
 */
public class TypeController implements Observer
{
    /**
     * The column definition model.
     */
    private final ColumnDefinitionModel myModel;

    /**
     * Constructs a new type controller.
     *
     * @param model The main column definition model.
     */
    public TypeController(ColumnDefinitionModel model)
    {
        myModel = model;
        myModel.addObserver(this);
    }

    /**
     * Removes itself from listening to model event.
     */
    public void close()
    {
        myModel.deleteObserver(this);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (ColumnDefinitionModel.SELECTED_DEFINITION_PROPERTY.equals(arg))
        {
            populateAvailableDataTypes();
        }
    }

    /**
     * Converts the list of column types to a list of string to display to the
     * user.
     *
     * @param columnTypes The list of column types to convert.
     * @return The list of data types.
     */
    private List<String> convertToDisplayStrings(List<ColumnType> columnTypes)
    {
        List<String> dataTypes = New.list();
        dataTypes.add("");

        for (ColumnType type : columnTypes)
        {
            dataTypes.add(type.toString());
        }

        return dataTypes;
    }

    /**
     * Gets all data types that we can possible display, ordered in logical
     * order.
     *
     * @return All possible data types a column can be.
     */
    private List<ColumnType> getAllDataTypes()
    {
        return New.list(ColumnType.values());
    }

    /**
     * Populates the available data types.
     */
    private void populateAvailableDataTypes()
    {
        List<ColumnType> allTypes = getAllDataTypes();
        removeSelectedTypes(allTypes);

        List<ColumnType> listOfTypes = New.list(allTypes);
        sortInProbabilityOrder(listOfTypes);
        List<String> dataTypes = convertToDisplayStrings(listOfTypes);

        myModel.setAvailableDataTypes(dataTypes);
    }

    /**
     * Removes the related types from the list.
     *
     * @param currentType The type whose related types should be removed.
     * @param removeFrom The list to remove the related types from.
     */
    private void removeRelatedTypes(ColumnType currentType, List<ColumnType> removeFrom)
    {
        if (currentType == ColumnType.TIMESTAMP)
        {
            removeFrom.remove(ColumnType.DATE);
            removeFrom.remove(ColumnType.TIME);
        }
        else if (currentType == ColumnType.DOWN_TIMESTAMP)
        {
            removeFrom.remove(ColumnType.DOWN_DATE);
            removeFrom.remove(ColumnType.DOWN_TIME);
        }
        else if (currentType == ColumnType.DATE || currentType == ColumnType.TIME)
        {
            removeFrom.remove(ColumnType.TIMESTAMP);
        }
        else if (currentType == ColumnType.DOWN_DATE || currentType == ColumnType.DOWN_TIME)
        {
            removeFrom.remove(ColumnType.DOWN_TIMESTAMP);
        }
    }

    /**
     * Removes any currently selected types from the list. For instance if
     * Timestamp is selected then Timestamp, Date, and Time are all removed from
     * the possible data type selections.
     *
     * @param removeFrom The collection to remove the types from.
     */
    private void removeSelectedTypes(List<ColumnType> removeFrom)
    {
        ColumnDefinitionTableModel table = myModel.getDefinitionTableModel();

        Set<ColumnType> currentTypes = New.set();

        for (int i = 0; i < table.getRowCount(); i++)
        {
            ColumnDefinitionRow row = table.getRow(i);

            if (myModel.getSelectedDefinition() == null || row.getColumnId() != myModel.getSelectedDefinition().getColumnId())
            {
                String dataType = row.getDataType();

                if (StringUtils.isNotEmpty(dataType))
                {
                    currentTypes.add(ColumnType.fromString(dataType));
                }
            }
        }

        boolean containsUpTime = currentTypes.contains(ColumnType.TIMESTAMP) || currentTypes.contains(ColumnType.DATE)
                || currentTypes.contains(ColumnType.TIME);

        if (!containsUpTime)
        {
            removeFrom.remove(ColumnType.DOWN_DATE);
            removeFrom.remove(ColumnType.DOWN_TIME);
            removeFrom.remove(ColumnType.DOWN_TIMESTAMP);
        }

        for (ColumnType currentType : currentTypes)
        {
            removeFrom.remove(currentType);
            removeRelatedTypes(currentType, removeFrom);
        }
    }

    /**
     * Sorts the data types in probability order where the highest probable data
     * types the selected column may be will be at the beginning of the list.
     * This function uses the data with the DetectedParameters class which as
     * already been populated by the detectors.
     *
     * @param dataTypes The data types to sort.
     */
    private void sortInProbabilityOrder(List<ColumnType> dataTypes)
    {
        ColumnDefinitionRow currentRow = myModel.getSelectedDefinition();
        if (currentRow == null)
        {
            return;
        }
        ValuesWithConfidence<LocationResults> loc = myModel.getLocationParameter();
        if (loc == null)
        {
            return;
        }

        Integer columnId = Integer.valueOf(currentRow.getColumnId());
        boolean isPotentialLocation = false;
        for (ValueWithConfidence<? extends LocationResults> potentialLocation : loc.getValues())
        {
            LocationResults result = potentialLocation.getValue();

            LatLonColumnResults latLonResult = result.getMostLikelyLatLonColumnPair();
            PotentialLocationColumn locationColumn = result.getMostLikelyLocationColumn();
            if (latLonResult != null && latLonResult.getLatColumn() != null
                    && latLonResult.getLatColumn().getColumnIndex() == columnId.intValue())
            {
                isPotentialLocation = true;
                break;
            }
            else if (latLonResult != null && latLonResult.getLonColumn() != null
                    && latLonResult.getLonColumn().getColumnIndex() == columnId.intValue())
            {
                isPotentialLocation = true;
                break;
            }
            else if (locationColumn != null && locationColumn.getColumnIndex() == columnId.intValue())
            {
                isPotentialLocation = true;
                break;
            }
        }

        if (isPotentialLocation)
        {
            Collections.sort(dataTypes, new ColumnTypeLocationComparator());
        }
    }
}
