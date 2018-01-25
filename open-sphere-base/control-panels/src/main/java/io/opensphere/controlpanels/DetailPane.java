package io.opensphere.controlpanels;

import javafx.scene.layout.BorderPane;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * A root class defining behaviors common to components used to render details
 * within the add data dialog.
 */
public abstract class DetailPane extends BorderPane
{
    /**
     * The toolbox through which system interactions occur.
     */
    private final Toolbox myToolbox;

    /**
     * Creates a new detail pane.
     *
     * @param pToolbox The toolbox through which system interactions occur.
     */
    public DetailPane(Toolbox pToolbox)
    {
        myToolbox = pToolbox;
        setPrefWidth(300);
    }

    /**
     * Gets the value of the {@link #myToolbox} field.
     *
     * @return the value stored in the {@link #myToolbox} field.
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Populates data into the detail pane.
     *
     * @param pDataGroupInfo the data to populate into the pane.
     */
    public abstract void populate(DataGroupInfo pDataGroupInfo);
}
