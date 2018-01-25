package io.opensphere.core.callout;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;

/** A model for a callout, or cartoon bubble. */
public interface Callout
{
    /**
     * Gets the anchor offset in pixels, or null if none.
     *
     * @return the anchorOffset {@link Vector2i}.
     */
    Vector2i getAnchorOffset();

    /**
     * Standard getter.
     *
     * @return The background color.
     */
    Color getBackgroundColor();

    /**
     * Standard getter.
     *
     * @return The border color.
     */
    Color getBorderColor();

    /**
     * Get the width of the border.
     *
     * @return The width of the border.
     */
    float getBorderWidth();

    /**
     * Standard getter.
     *
     * @return The corner radius.
     */
    int getCornerRadius();

    /**
     * Standard getter.
     *
     * @return The font.
     */
    Font getFont();

    /**
     * Gets the id assigned to this CallOut.
     *
     * @return the id.
     */
    long getId();

    /**
     * Standard getter.
     *
     * @return The call out location.
     */
    LatLonAlt getLocation();

    /**
     * Standard getter.
     *
     * @return The text color.
     */
    Color getTextColor();

    /**
     * Standard getter.
     *
     * @return The list of strings that describe our text to display.
     */
    List<? extends String> getTextLines();

    /**
     * Sets the time of the callout if there is one.
     *
     * @param time The time of the callout or null if there isn't one.
     */
    void setTime(TimeSpan time);

    /**
     * Gets the time of the callout if there is one.
     *
     * @return The time of the callout or null.
     */
    TimeSpan getTime();

    /**
     * Check if border is highlighted.
     *
     * @return True if border is currently highlighted, false otherwise.
     */
    boolean isBorderHighlighted();

    /**
     * Set if this callout is pickable.
     *
     * @return If this callout is pickable.
     */
    boolean isPickable();

    /**
     * Sets the anchor offset in pixels.
     *
     * @param anchorOffset the {@link Vector2i} anchor offset.
     */
    void setAnchorOffset(Vector2i anchorOffset);

    /**
     * Standard setter.
     *
     * @param bgColor The background color.
     */
    void setBackgroundColor(Color bgColor);

    /**
     * Standard setter.
     *
     * @param borderColor The border color.
     */
    void setBorderColor(Color borderColor);

    /**
     * Set the border width.
     *
     * @param borderWidth The border width.
     */
    void setBorderWidth(float borderWidth);

    /**
     * Standard setter.
     *
     * @param cornerRadius The corner radius.
     */
    void setCornerRadius(int cornerRadius);

    /**
     * Get if this callout is pickable.
     *
     * @param pickable If this callout is pickable.
     */
    void setPickable(boolean pickable);

    /**
     * Standard setter.
     *
     * @param textColor The text color.
     */
    void setTextColor(Color textColor);
}
