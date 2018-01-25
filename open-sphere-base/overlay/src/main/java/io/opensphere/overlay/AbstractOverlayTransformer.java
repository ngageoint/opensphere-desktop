package io.opensphere.overlay;

import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.Showable;

/** Abstract overlay transformer. */
public abstract class AbstractOverlayTransformer extends DefaultTransformer implements Showable
{
    /** The preferences. */
    private final Preferences myPreferences;

    /**
     * Constructor.
     *
     * @param dataRegistry The data registry
     * @param preferences The preferences
     */
    public AbstractOverlayTransformer(DataRegistry dataRegistry, Preferences preferences)
    {
        super(dataRegistry);
        myPreferences = preferences;
    }

    @Override
    public void open()
    {
        super.open();
        if (isVisible())
        {
            setVisible(true);
        }
    }

    /**
     * Gets the preferences.
     *
     * @return the preferences
     */
    protected Preferences getPreferences()
    {
        return myPreferences;
    }

    /**
     * Gets the initial location.
     *
     * @param width the width
     * @param height the height
     * @param preferenceKey the location preference key
     * @return the location bounding box
     */
    protected ScreenBoundingBox getInitialLocation(int width, int height, String preferenceKey)
    {
        ScreenPosition scaleUpLeft = new ScreenPosition(0, 0);
        ScreenPosition scaleLowRight = new ScreenPosition(width, height);
        String pref = myPreferences.getString(preferenceKey, null);
        if (pref != null)
        {
            ScreenPosition pos = ScreenPosition.fromSimpleString(pref);
            scaleUpLeft = scaleUpLeft.add(pos);
            scaleLowRight = scaleLowRight.add(pos);
        }
        return new ScreenBoundingBox(scaleUpLeft, scaleLowRight);
    }
}
