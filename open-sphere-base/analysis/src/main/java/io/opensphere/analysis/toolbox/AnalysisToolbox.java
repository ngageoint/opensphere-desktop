package io.opensphere.analysis.toolbox;

import io.opensphere.analysis.base.model.CommonSettingsModel;
import io.opensphere.analysis.table.functions.ColumnFunctionFactory;
import io.opensphere.analysis.table.functions.statusbar.StatusBarFunctionFactory;
import io.opensphere.core.PluginToolbox;
import io.opensphere.core.Toolbox;

/** The Analysis toolbox. */
public final class AnalysisToolbox implements PluginToolbox
{
    /** The settings model. */
    private volatile CommonSettingsModel mySettingsModel;

    /** The factory used to generate status bar functions. */
    private final StatusBarFunctionFactory myStatusBarFunctionFactory;

    /** The factory used to generate column functions. */
    private final ColumnFunctionFactory myColumnFunctionFactory;

    /**
     * Creates a new toolbox instance.
     *
     * @param toolbox the generic system toolbox through which application state
     *            is accessed.
     */
    public AnalysisToolbox(Toolbox toolbox)
    {
        myStatusBarFunctionFactory = new StatusBarFunctionFactory(toolbox);
        myColumnFunctionFactory = new ColumnFunctionFactory(toolbox);
    }

    @Override
    public String getDescription()
    {
        return "The Analysis toolbox";
    }

    /**
     * Gets the value of the columnFunctionFactory
     * ({@link #myColumnFunctionFactory}) field.
     *
     * @return the value stored in the {@link #myColumnFunctionFactory} field.
     */
    public ColumnFunctionFactory getColumnFunctionFactory()
    {
        return myColumnFunctionFactory;
    }

    /**
     * Gets the value of the statusBarFunctionFactory
     * ({@link #myStatusBarFunctionFactory}) field.
     *
     * @return the value stored in the {@link #myStatusBarFunctionFactory}
     *         field.
     */
    public StatusBarFunctionFactory getStatusBarFunctionFactory()
    {
        return myStatusBarFunctionFactory;
    }

    /**
     * Gets the settingsModel.
     *
     * @return the settingsModel
     */
    public CommonSettingsModel getSettingsModel()
    {
        return mySettingsModel;
    }

    /**
     * Sets the settingsModel.
     *
     * @param settingsModel the settingsModel
     */
    public void setSettingsModel(CommonSettingsModel settingsModel)
    {
        mySettingsModel = settingsModel;
    }
}
