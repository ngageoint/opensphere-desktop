package io.opensphere.mantle.iconproject.model;

import io.opensphere.mantle.iconproject.panels.MainPanel;

/** The model of the UI Display Panels. */
public class ViewModel
{
    /**
     * The SplitPane containing the Tree hierarchy, Add, Generate, and Customize
     * Icon components on the Left and the Icon Display Grid on the Right.
     */
    private MainPanel myMainPanel;

    /** The option to disable or enable the user to select multiple icons. */
    private boolean myMultiSelectEnabled;

    /**
     * Sets the main display panel.
     *
     * @param mainPanel the main split panel display.
     */
    public void setMainPanel(MainPanel mainPanel)
    {
        myMainPanel = mainPanel;
    }

    /**
     * Gets the main panel.
     *
     * @return the current main panel.
     */
    public MainPanel getMainPanel()
    {
        return myMainPanel;
    }

    /**
     * Gets the multiple icon selection choice.
     *
     * @return the toggle.
     */
    public boolean getMultiSelectEnabled()
    {
        return myMultiSelectEnabled;
    }

    /**
     * Sets the ability to enable or disable multiple icon selection.
     *
     * @param multiSelectEnabled the inputted choice.
     */
    public void setMultiSelectEnabled(boolean multiSelectEnabled)
    {
        myMultiSelectEnabled = multiSelectEnabled;
    }
}
