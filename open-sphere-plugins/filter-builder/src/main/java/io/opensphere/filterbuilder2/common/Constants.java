package io.opensphere.filterbuilder2.common;

import java.awt.Color;
import java.awt.Dimension;

import io.opensphere.core.util.Colors;

/**
 * Constants.
 */
public final class Constants
{
    /** The inset for components. */
    public static final int INSET = 4;

    /** The double inset for components. */
    public static final int DOUBLE_INSET = 8;

    /** The term for "criterion" in the UI. */
    public static final String EXPRESSION = "Expression";

    /** The editor scroll pane size. */
    public static final Dimension EDITOR_SCROLLPANE_SIZE = new Dimension(580, 348);

    /** The hover color. */
    public static final Color HOVER_COLOR = new Color(.6f, .6f, .6f, .5f);

    /** The selection color. */
    public static final Color SELECTION_COLOR = Colors.LF_PRIMARY2;

    /** Transparent color. */
    public static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);

    /**
     * Private constructor.
     */
    private Constants()
    {
    }
}
