package io.opensphere.csvcommon.ui.columndefinition.model;

import java.util.List;
import java.util.Observable;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.controller.DetectedParameters;
import io.opensphere.csvcommon.detect.location.model.LocationResults;

/** The main model for the Column Definition panel. */
public class ColumnDefinitionModel extends Observable
{
    /** The available data types property name. */
    public static final String AVAILABLE_DATA_TYPES_PROPERTY = "availableDataTypes";

    /** The available formats property name. */
    public static final String AVAILABLE_FORMATS_PROPERTY = "availableFormats";

    /** The error message property name. */
    public static final String ERROR_MESSAGE_PROPERTY = "errorMessage";

    /** The selected column definition property name. */
    public static final String SELECTED_DEFINITION_PROPERTY = "selectedDefinition";

    /** The warning message property name. */
    public static final String WARNING_MESSAGE_PROPERTY = "warningMessage";

    /** The can add formats property name. */
    public static final String CAN_ADD_FORMATS_PROPERTY = "canAddFormats";

    /** List of available data types for the selectedcolumn definition row. */
    private List<String> myAvailableDataTypes = New.list();

    /**List of available formats for the selected column definition row. */
    private List<String> myAvailableFormats = New.list();

    /** Indicates if the user can add formats to the list. */
    private boolean myCanAddFormats;

    /** The table model that contains the before and after data. */
    private final BeforeAfterTableModel myBeforeAfterTableModel = new BeforeAfterTableModel();

    /** The table model that contains all column definitions. */
    private final ColumnDefinitionTableModel myDefinitionTableModel = new ColumnDefinitionTableModel();

    /** An error message to show the user, or null if there isn't one. */
    private String myErrorMessage;

    /** The currently selected column definition row. */
    private ColumnDefinitionRow mySelectedDefinition;

    /** A warning message to show the user, or null if there isn't one. */
    private String myWarningMessage;

    /** The sample data to display. */
    private List<? extends List<? extends String>> mySampleData;

    /** The selected parameters to populate the UI and edit. */
    private CSVParseParameters mySelectedParameters;

    /** The parameters detected by reading a sample of the file. */
    private DetectedParameters myDetectedParameters;

    /**
     * Gets the list of currently available data types for the currently
     * selected column definition row.
     *
     * @return The list of currently available data types for the currently
     *         selected column definition row.
     */
    public List<String> getAvailableDataTypes()
    {
        return myAvailableDataTypes;
    }

    /**
     * Gets the list of currently available formats for the currently selected
     * column definition row.
     *
     * @return The list of currently available formats for the currently
     *         selected column definition row.
     */
    public List<String> getAvailableFormats()
    {
        return myAvailableFormats;
    }

    /**
     * Gets the table model that contains the before and after data.
     *
     * @return The table model that contains the before and after data.
     */
    public BeforeAfterTableModel getBeforeAfterTableModel()
    {
        return myBeforeAfterTableModel;
    }

    /**
     * Gets the table model that contains all column definitions.
     *
     * @return The table model that contains all column definitions.
     */
    public ColumnDefinitionTableModel getDefinitionTableModel()
    {
        return myDefinitionTableModel;
    }

    /**
     * Gets An error message to show the user, or null if there isn't one.
     *
     * @return An error message to show the user, or null if there isn't one.
     */
    public String getErrorMessage()
    {
        return myErrorMessage;
    }

    /**
     * Gets the currently selected column definition row.
     *
     * @return The currently selected column definition row.
     */
    public ColumnDefinitionRow getSelectedDefinition()
    {
        return mySelectedDefinition;
    }

    /**
     * Gets a warning message to show the user, or null if there isn't one.
     *
     * @return A warning message to show the user, or null if there isn't one.
     */
    public String getWarningMessage()
    {
        return myWarningMessage;
    }

    /**
     * Sets the list of currently available data types for the currently
     * selected column definition row.
     *
     * @param availableDataTypes The list of currently available data types for
     *            the currently selected column definition row.
     */
    public void setAvailableDataTypes(List<String> availableDataTypes)
    {
        myAvailableDataTypes = availableDataTypes;
        setChanged();
        notifyObservers(AVAILABLE_DATA_TYPES_PROPERTY);
    }

    /**
     * Sets the list of currently available formats for the currently selected
     * column definition row.
     *
     * @param availableFormats The list of currently available formats for the
     *            currently selected column definition row.
     */
    public void setAvailableFormats(List<String> availableFormats)
    {
        myAvailableFormats = availableFormats;
        setChanged();
        notifyObservers(AVAILABLE_FORMATS_PROPERTY);
    }

    /**
     * Sets an error message to show the user, or null if there isn't one.
     *
     * @param errorMessage An error message to show the user, or null if there
     *            isn't one.
     */
    public void setErrorMessage(String errorMessage)
    {
        myErrorMessage = errorMessage;
        setChanged();
        notifyObservers(ERROR_MESSAGE_PROPERTY);
    }

    /**
     * Sets the currently selected column definition row.
     *
     * @param selectedDefinition The currently selected column definition row.
     */
    public void setSelectedDefinition(ColumnDefinitionRow selectedDefinition)
    {
        mySelectedDefinition = selectedDefinition;
        setChanged();
        notifyObservers(SELECTED_DEFINITION_PROPERTY);
    }

    /**
     * Sets a warning message to show the user, or null if there isn't one.
     *
     * @param warningMessage A warning message to show the user, or null if
     *            there isn't one.
     */
    public void setWarningMessage(String warningMessage)
    {
        myWarningMessage = warningMessage;
        setChanged();
        notifyObservers(WARNING_MESSAGE_PROPERTY);
    }

    /**
     * Gets the sample data.
     *
     * @return The sample data to display to the user.
     */
    public List<? extends List<? extends String>> getSampleData()
    {
        return mySampleData;
    }

    /**
     * Sets the sample data.
     *
     * @param sampleData The sample data to display to the user.
     */
    public void setSampleData(List<? extends List<? extends String>> sampleData)
    {
        mySampleData = sampleData;
    }

    /**
     * Gets the selected parameters to populate the UI with as well as make
     * changes to.
     *
     * @return The selected parameters to populate the UI with as well as make
     *         changes to.
     */
    public CSVParseParameters getSelectedParameters()
    {
        return mySelectedParameters;
    }

    /**
     * Sets the selected parameters to populate the UI with as well as make
     * changes to.
     *
     * @param selectedParameters The selected parameters to populate the UI with
     *            as well as make changes to.
     */
    public void setSelectedParameters(CSVParseParameters selectedParameters)
    {
        mySelectedParameters = selectedParameters;
    }

    /**
     * Indicates if the user can add formats to the list of available formats.
     *
     * @return True if the user can add, false otherwise.
     */
    public boolean canAddFormats()
    {
        return myCanAddFormats;
    }

    /**
     * Sets if the user can add formats.
     *
     * @param canAddFormats True if the user can add formats, false otherwise.
     */
    public void setCanAddFormats(boolean canAddFormats)
    {
        myCanAddFormats = canAddFormats;
        setChanged();
        notifyObservers(CAN_ADD_FORMATS_PROPERTY);
    }

    /**
     * Sets the detected parameters that were generated by reading a sample of
     * the file.
     *
     * @param detectedParameters the detected parameters.
     */
    public void setDetectedParameters(DetectedParameters detectedParameters)
    {
        myDetectedParameters = detectedParameters;
    }

    /**
     * Obtain the location results by some means.
     * @return location results
     */
    public ValuesWithConfidence<LocationResults> getLocationParameter()
    {
        if (myDetectedParameters == null)
        {
            return null;
        }
        return myDetectedParameters.getLocationParameter();
    }
}
