package io.opensphere.mantle.data.geom.style.dialog;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.event.SourceableEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class ShowTypeVisualizationStyleEvent.
 */
public class ShowTypeVisualizationStyleEvent extends AbstractSingleStateEvent implements SourceableEvent
{
    /** The source. */
    private final Object mySource;

    /** The style action. */
    private final StyleAction myStyleAction;

    /** The type. */
    private final DataTypeInfo myType;

    /**
     * Instantiates a new show type visualization style event.
     *
     * @param dti the {@link DataTypeInfo}
     * @param source the source
     */
    public ShowTypeVisualizationStyleEvent(DataTypeInfo dti, Object source)
    {
        this(dti, StyleAction.SHOW_ONLY, source);
    }

    /**
     * Instantiates a new show type visualization style event.
     *
     * @param dti the {@link DataTypeInfo}
     * @param action the action
     * @param source the source
     */
    public ShowTypeVisualizationStyleEvent(DataTypeInfo dti, StyleAction action, Object source)
    {
        super();
        mySource = source;
        myStyleAction = action;
        myType = dti;
    }

    @Override
    public String getDescription()
    {
        return "Requests the " + VisualizationStyleControlDialog.TITLE
                + " dialog become visible and show the style for the selected data type.";
    }

    @Override
    public Object getSource()
    {
        return mySource;
    }

    /**
     * Gets the style action.
     *
     * @return the style action
     */
    public StyleAction getStyleAction()
    {
        return myStyleAction;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public DataTypeInfo getType()
    {
        return myType;
    }

    /**
     * The Enum StyleAction.
     */
    public enum StyleAction
    {
        /** The ACTIVATE_IF_INACTIVE. */
        ACTIVATE_IF_INACTIVE,

        /** The DEACTIVATE_IF_ACTIVE. */
        DEACTIVATE_IF_ACTIVE,

        /** The SHOW_ONLY. */
        SHOW_ONLY
    }
}
