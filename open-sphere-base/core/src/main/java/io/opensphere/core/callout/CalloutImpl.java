package io.opensphere.core.callout;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Colors;

/** The main callout class. */
public class CalloutImpl implements Callout
{
    /** The call out background color. */
    private Color myBackgroundColor;

    /** The border color. */
    private Color myBorderColor;

    /** The corner radius. */
    private int myCornerRadius;

    /** The call out text font. */
    private final Font myFont;

    /** Whether or not border is highlighted. */
    private boolean myIsHighlighted;

    /** The call out location. */
    private final LatLonAlt myLocation;

    /** The text lines. */
    private final List<? extends String> myTextLines;

    /** The call out text color. */
    private Color myTextColor;

    /** The Id. */
    private final long myId;

    /** The anchor offset in pixels. */
    private Vector2i myAnchorOffset;

    /** The border width. */
    private float myBorderWidth = 1f;

    /** If this callout is pickable (can be dragged). */
    private boolean myPickable = true;

    /**
     * The time of the callout, or null if there isn't one.
     */
    private TimeSpan myTime;

    /**
     * Constructor.
     *
     * @param id the id for the CallOut.
     * @param textlines The text to display (as a list of strings).
     * @param location The location we will be anchored to.
     * @param font The size of the text.
     */
    public CalloutImpl(long id, List<? extends String> textlines, LatLonAlt location, Font font)
    {
        myId = id;
        myBackgroundColor = Colors.TRANSPARENT_BLACK;
        myTextColor = Color.WHITE;
        myBorderColor = Color.WHITE;
        myCornerRadius = 0;
        myFont = font;
        myLocation = location;
        myTextLines = textlines;
    }

    @Override
    public Vector2i getAnchorOffset()
    {
        return myAnchorOffset;
    }

    @Override
    public Color getBackgroundColor()
    {
        return myBackgroundColor;
    }

    @Override
    public Color getBorderColor()
    {
        return myBorderColor;
    }

    @Override
    public float getBorderWidth()
    {
        return myBorderWidth;
    }

    @Override
    public int getCornerRadius()
    {
        return myCornerRadius;
    }

    @Override
    public Font getFont()
    {
        return myFont;
    }

    @Override
    public long getId()
    {
        return myId;
    }

    @Override
    public LatLonAlt getLocation()
    {
        return myLocation;
    }

    @Override
    public Color getTextColor()
    {
        return myTextColor;
    }

    @Override
    public List<? extends String> getTextLines()
    {
        return myTextLines;
    }

    @Override
    public boolean isBorderHighlighted()
    {
        return myIsHighlighted;
    }

    @Override
    public boolean isPickable()
    {
        return myPickable;
    }

    @Override
    public void setAnchorOffset(Vector2i anchorOffset)
    {
        myAnchorOffset = anchorOffset;
    }

    @Override
    public void setBackgroundColor(Color bgColor)
    {
        myBackgroundColor = bgColor;
    }

    @Override
    public void setBorderColor(Color borderColor)
    {
        myBorderColor = borderColor;
    }

    @Override
    public void setBorderWidth(float borderWidth)
    {
        myBorderWidth = borderWidth;
    }

    @Override
    public void setCornerRadius(int cornerRadius)
    {
        myCornerRadius = cornerRadius;
    }
    @Override
    public void setPickable(boolean pickable)
    {
        myPickable = pickable;
    }

    @Override
    public void setTextColor(Color textColor)
    {
        myTextColor = textColor;
    }

    @Override
    public void setTime(TimeSpan time)
    {
        myTime = time;
    }

    @Override
    public TimeSpan getTime()
    {
        return myTime;
    }
}
