package io.opensphere.controlpanels.layers.base;

import java.util.List;

import io.opensphere.core.util.swing.tree.CustomTreeTableModelButtonBuilder;

/**
 * The Interface DataTreeButtonBuilders.
 */
@FunctionalInterface
public interface DataTreeButtonProvisioner
{
    /**
     * The constant DELETE_BUTTON.
     */
    String DELETE_BUTTON = "DeleteButton";

    /** The Constant FILTER_BUTTON. */
    String FILTER_BUTTON = "FilterButton";

    /** The Constant GEAR_BUTTON. */
    String GEAR_BUTTON = "GearButton";

    /** The constant representing the pause button. */
    String PAUSE_BUTTON = "PauseButton";

    /** The constant representing the play button. */
    String PLAY_BUTTON = "PlayButton";

    /** The constant representing the playclock button. */
    String PLAYCLOCK_BUTTON = "PlayClockButton";

    /**
     * The constant popout button.
     */
    String POPOUT_BUTTON = "PopoutButton";

    /**
     * The constant refresh button.
     */
    String REFRESH_BUTTON = "RefreshButton";

    /** The Constant REMOVE_BUTTON. */
    String REMOVE_BUTTON = "RemoveButton";

    /** The constant representing the stop button. */
    String STOP_BUTTON = "StopButton";

    /**
     * Gets the button builders.
     *
     * @return the button builders
     */
    List<CustomTreeTableModelButtonBuilder> getButtonBuilders();
}
