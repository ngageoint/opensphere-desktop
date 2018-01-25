package io.opensphere.core.api.adapter;

import java.awt.Point;

import javax.annotation.Nullable;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;

/**
 * Extension that allows saving and loading location in the preferences.
 */
public abstract class AbstractLocationSaveMenuItemPlugin extends AbstractMenuItemPlugin
{
    /** Preference key for the X location of the frame. */
    private static final String PREF_KEY_LOCATION_X = "LocationX";

    /** Preference key for the Y location of the frame. */
    private static final String PREF_KEY_LOCATION_Y = "LocationY";

    /** The preferences. */
    private Preferences myPreferences;

    /** The Remember location. */
    private boolean myRememberLocation;

    /** The title of the window. */
    private final String myTitle;

    /**
     * Constructor with remember visibility state flag.
     *
     * @param title The title of the window.
     * @param rememberVisibilityState Indicates if the visibility state should
     *            be persisted in the preferences.
     * @param rememberLocation Indicates if the location should be persisted in
     *            the preferences.
     */
    public AbstractLocationSaveMenuItemPlugin(String title, boolean rememberVisibilityState, boolean rememberLocation)
    {
        super(rememberVisibilityState);
        myTitle = title;
        myRememberLocation = rememberLocation;
    }

    @Override
    public void initialize(io.opensphere.core.PluginLoaderData plugindata, final Toolbox toolbox)
    {
        super.initialize(plugindata, toolbox);

        myPreferences = toolbox.getPreferencesRegistry().getPreferences("MenuItem." + getTitle());
    }

    /**
     * Get the current location of the widget.
     *
     * @return The current location, or {@code null} if it cannot be determined.
     */
    @Nullable
    protected abstract Point getLocation();

    /**
     * Get the title of the widget.
     *
     * @return The title.
     */
    protected final String getTitle()
    {
        return myTitle;
    }

    /**
     * Checks if is remember location.
     *
     * @return true, if is remember location
     */
    protected boolean isRememberLocation()
    {
        return myRememberLocation;
    }

    /**
     * Get the location of the widget.
     *
     * @param xLoc The x coordinate.
     * @param yLoc The y coordinate.
     */
    protected abstract void setLocation(int xLoc, int yLoc);

    /**
     * Set the location to the preferred values.
     */
    protected void setPreferredLocation()
    {
        if (isRememberLocation() && myPreferences != null)
        {
            int xLoc = myPreferences.getInt(PREF_KEY_LOCATION_X, -1);
            int yLoc = myPreferences.getInt(PREF_KEY_LOCATION_Y, -1);
            if (xLoc != -1 && yLoc != -1)
            {
                setLocation(xLoc, yLoc);
            }
        }
    }

    /**
     * Sets the remember location.
     *
     * @param rememberLocation the new remember location
     */
    protected void setRememberLocation(boolean rememberLocation)
    {
        myRememberLocation = rememberLocation;
    }

    /**
     * Update stored location preference.
     */
    protected void updateStoredLocationPreference()
    {
        if (isRememberLocation() && myPreferences != null)
        {
            Point p = getLocation();
            if (p != null)
            {
                myPreferences.putInt(PREF_KEY_LOCATION_X, p.x, this);
                myPreferences.putInt(PREF_KEY_LOCATION_Y, p.y, this);
            }
        }
    }

    /**
     * Determine whether the default location should be used.
     *
     * @return true when this item is not set to use a remembered location or no
     *         remembered location exists for it to use.
     */
    protected boolean useDefaultLocation()
    {
        int xLoc = myPreferences.getInt(PREF_KEY_LOCATION_X, -Integer.MAX_VALUE);
        return !myRememberLocation || xLoc == -Integer.MAX_VALUE;
    }
}
