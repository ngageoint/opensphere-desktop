package io.opensphere.analysis.base.model;

import javafx.beans.property.ObjectProperty;

import io.opensphere.core.dialog.alertviewer.event.Message;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;

/** Contains all the models for a tool. */
public class ToolModels
{
    /** The settings model. */
    private final SettingsModel mySettingsModel;

    /** The data model. */
    private final BinDataModel myDataModel;

    /** The action model. */
    private final ActionModel myActionModel;

    /** The user message. */
    private final ObjectProperty<Message> myUserMessage = new ConcurrentObjectProperty<>(this, "userMessage");

    /**
     * Constructor.
     *
     * @param settingsModel The settings model
     * @param dataModel The data model
     */
    public ToolModels(SettingsModel settingsModel, BinDataModel dataModel)
    {
        mySettingsModel = settingsModel;
        myDataModel = dataModel;
        myActionModel = new ActionModel();
    }

    /**
     * Gets the settingsModel.
     *
     * @return the settingsModel
     */
    public SettingsModel getSettingsModel()
    {
        return mySettingsModel;
    }

    /**
     * Gets the dataModel.
     *
     * @return the dataModel
     */
    public BinDataModel getDataModel()
    {
        return myDataModel;
    }

    /**
     * Gets the actionModel.
     *
     * @return the actionModel
     */
    public ActionModel getActionModel()
    {
        return myActionModel;
    }

    /**
     * Gets the userMessage property.
     *
     * @return the userMessage property
     */
    public ObjectProperty<Message> userMessageProperty()
    {
        return myUserMessage;
    }
}
