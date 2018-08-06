package io.opensphere.mantle.iconproject.model;

import io.opensphere.mantle.iconproject.panels.MainPanel;
import io.opensphere.mantle.iconproject.panels.TopMenuBar;

/** The model of the UI Display Panels. */
public class ViewModel
{
    /**
     * The HBox containing the top UI bar components for icon resizing, display
     * view, and icon resizing.
     */
    private TopMenuBar myTopMenuBar;

    /**
     * the SplitPane containing the Tree hierarchy, Add,Generate,and Customize
     * Icon components on the Left and the Icon Display Grid on the Right.
     */
    private MainPanel myMainPanel;

    /**
     * Sets the top menu bar.
     * @param theTopMenuBar the menu bar.
     */
    public void setTopMenuBar(TopMenuBar theTopMenuBar)
    {
        myTopMenuBar = theTopMenuBar;
    }

    /**
     * Gets the top menu bar.
     * @return myTopMenuBar the menu bar.
     */
    public TopMenuBar getTopMenuBar()
    {
        return myTopMenuBar;
    }

    /**
     * Sets the main display panel.
     * @param theMainPanel the main split panel display.
     */
    public void setMainPanel(MainPanel theMainPanel)
    {
        myMainPanel = theMainPanel;
    }

    /**
     * Gets the main panel.
     * @return myMainPanel the curent main panel.
     */
    public MainPanel getMainPanel()
    {
        return myMainPanel;
    }
}

