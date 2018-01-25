package io.opensphere.core.control.action.context;

/** Utility class for defining action context identifiers. */
public final class ContextIdentifiers
{
    /** The context used for completed arcs. */
    public static final String ARC_CONTEXT = "ARC_CONTEXT";

    /**
     * The context used for providers who participate in the context for mouse
     * events. It is expected that the key will either be a MouseEvent or
     * contain a MouseEvent.
     */
    public static final String DEFAULT_MOUSE_CONTEXT = "DEFAULT_MOUSE_CONTEXT";

    /** The context used for providers who participate in deletion. */
    public static final String DELETE_CONTEXT = "DELETE_CONTEXT";

    /** The context used when a new geometry is created. */
    public static final String GEOMETRY_COMPLETED_CONTEXT = "GEOMETRY_COMPLETED_CONTEXT";

    /** The context used when an existing geometry is selected. */
    public static final String GEOMETRY_DOUBLE_CLICK_CONTEXT = "GEOMETRY_DOUBLE_CLICK_CONTEXT";

    /** The context used when an existing geometry is selected. */
    public static final String GEOMETRY_SELECTION_CONTEXT = "GEOMETRY_SELECTION_CONTEXT";

    /** The context used for providers who may import data. */
    public static final String IMPORT_CONTEXT = "IMPORT_CONTEXT";

    /** The context used for actions against a region of interest. */
    public static final String ROI_CONTEXT = "ROI_CONTEXT";

    /** The context used with only a screen position. */
    public static final String SCREEN_POSITION_CONTEXT = "SCREEN_POSITION_CONTEXT";

    /** The context used when a time span is selected. */
    public static final String TIMESPAN_CONTEXT = "TIMESPAN_CONTEXT";

    /** Disallow instantiation. */
    private ContextIdentifiers()
    {
    }
}
