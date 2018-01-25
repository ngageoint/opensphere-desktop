package io.opensphere.core.timeline;

import java.awt.Color;

import io.opensphere.core.model.time.TimeSpan;

/**
 * The TimelineDatum with style and will always draw as a dot (TimeInstant) or
 * rectangle (TimeSpan) no matter the chart type the user has selected.
 */
public class StyledTimelineDatum extends TimelineDatum
{
    /**
     * The time span color.
     */
    private final Color myColor;

    /**
     * Represents what type of shape the time span should be.
     */
    private final TimelineDrawType myDrawType;

    /**
     * The text to display in the timeline.
     */
    private final String myText;

    /**
     * The color the text should be.
     */
    private final Color myTextColor;

    /**
     * Constructs a {@link StyledTimelineDatum} that will always draw as a dot
     * (TimeInstant) or rectangle (TimeSpan) no matter the chart type the user
     * has selected.
     *
     * @param id The id of the datum.
     * @param timeSpan The time this datum should span.
     */
    public StyledTimelineDatum(long id, TimeSpan timeSpan)
    {
        this(id, timeSpan, null, TimelineDrawType.DEFAULT, null, null);
    }

    /**
     * Constructs a {@link StyledTimelineDatum} that will draw with its own
     * color, seperate from the layer color, and will have the specified text
     * displayed next to it.
     *
     * @param id The id of the datum.
     * @param timeSpan The time this datum should show up at.
     * @param color The color the datum should be drawn as.
     * @param text The text to display next to the datum.
     * @param textColor The color of the text.
     */
    public StyledTimelineDatum(long id, TimeSpan timeSpan, Color color, String text, Color textColor)
    {
        this(id, timeSpan, color, TimelineDrawType.DEFAULT, text, textColor);
    }

    /**
     * Constructs a {@link StyledTimelineDatum} that will draw as the specified
     * drawType.
     *
     * @param id The id of the datum.
     * @param timeSpan The time the datum should show up at.
     * @param color The color of the datum.
     * @param drawType Specifies if this datum should draw as default or a line
     *            or some other drawing shape.
     */
    public StyledTimelineDatum(long id, TimeSpan timeSpan, Color color, TimelineDrawType drawType)
    {
        this(id, timeSpan, color, drawType, null, null);
    }

    /**
     * Constructs a new {@link StyledTimelineDatum} with its own color, draw
     * type, and text.
     *
     * @param id The id of the datum.
     * @param timeSpan The time the datum should show up at.
     * @param color The color of the datum.
     * @param drawType Specifies if this datum should draw as default or a line
     *            or some other drawing shape.
     * @param text The text to display next to the datum.
     * @param textColor The color of the text.
     */
    public StyledTimelineDatum(long id, TimeSpan timeSpan, Color color, TimelineDrawType drawType, String text, Color textColor)
    {
        super(id, timeSpan);
        myColor = color;
        myDrawType = drawType;
        myText = text;
        myTextColor = textColor;
    }

    /**
     * Gets the color.
     *
     * @return the color
     */
    public Color getColor()
    {
        return myColor;
    }

    /**
     * Gets the draw type.
     *
     * @return the draw type
     */
    public TimelineDrawType getDrawType()
    {
        return myDrawType;
    }

    /**
     * The text of the datum.
     *
     * @return The text to draw next to the datum or null if no text.
     */
    public String getText()
    {
        return myText;
    }

    /**
     * The color of the text.
     *
     * @return The color of the text.
     */
    public Color getTextColor()
    {
        return myTextColor;
    }
}
