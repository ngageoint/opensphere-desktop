package io.opensphere.controlpanels.animation.view;

import java.awt.Color;

/**
 * Animation constants.
 */
final class AnimationConstants
{
    /** The active window handle color. */
    public static final Color ACTIVE_HANDLE_COLOR = new Color(63, 88, 198);

    /** The active window handle hover color. */
    public static final Color ACTIVE_HANDLE_HOVER_COLOR = ACTIVE_HANDLE_COLOR.brighter();

    /** The animation span handle color. */
    public static final Color ANIMATION_SPAN_HANDLE_COLOR = Color.BLACK;

    /** The animation span handle hover color. */
    public static final Color ANIMATION_SPAN_HANDLE_HOVER_COLOR = Color.DARK_GRAY;

    /** The background color. */
    public static final Color BG_COLOR = new Color(221, 221, 221, 64);

    /** The foreground color. */
    public static final Color FG_COLOR = Color.WHITE;

    /** The time probe color. */
    public static final Color TIME_PROBE_COLOR = new Color(255, 255, 255, 50);

    /**
     * Private constructor.
     */
    private AnimationConstants()
    {
    }
}
