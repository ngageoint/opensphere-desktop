package io.opensphere.core.hud.widget;

import java.awt.Color;
import java.awt.Font;
import java.util.Collections;
import java.util.Set;

import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultLabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/** A component that just renders some text. */
public class TextLabel extends Renderable
{
    /** Text font. */
    private final String myFont;

    /** LabelGeometry which renders for this label. */
    private LabelGeometry myLabelGeometry;

    /** Text which is currently being displayed. */
    private String myText;

    /**
     * Construct a TextLabel.
     *
     * @param parent parent component.
     * @param builder Builder for the label.
     */
    public TextLabel(Component parent, Builder builder)
    {
        super(parent);
        myText = builder.getText();
        myFont = builder.getFont();
        setBaseColor(builder.getColor());
    }

    /**
     * Get the text.
     *
     * @return the text
     */
    public String getText()
    {
        return myText;
    }

    @Override
    public void init()
    {
        myLabelGeometry = myText == null ? null : createLabelGeometry(myText);
        if (myLabelGeometry != null)
        {
            getGeometries().add(myLabelGeometry);
        }
    }

    /**
     * Replace the text of this label with new text.
     *
     * @param text Text to render with this label.
     */
    public void replaceText(String text)
    {
        myText = text;
        Set<LabelGeometry> removes;
        if (myLabelGeometry == null)
        {
            removes = Collections.emptySet();
        }
        else
        {
            removes = Collections.singleton(myLabelGeometry);
            getGeometries().remove(myLabelGeometry);
        }

        myLabelGeometry = text == null ? null : createLabelGeometry(text);
        Set<LabelGeometry> adds;
        if (myLabelGeometry == null)
        {
            adds = Collections.emptySet();
        }
        else
        {
            adds = Collections.singleton(myLabelGeometry);
            getGeometries().add(myLabelGeometry);
        }

        updateGeometries(adds, removes);
    }

    /**
     * Create a LabelGeometry with the given text.
     *
     * @param text Text for the geometry.
     * @return newly created LabelGeometry.
     */
    private LabelGeometry createLabelGeometry(String text)
    {
        ScreenBoundingBox drawBox = getDrawBounds();

        LabelGeometry.Builder<ScreenPosition> labelBuilder = new LabelGeometry.Builder<ScreenPosition>();
        LabelRenderProperties props = new DefaultLabelRenderProperties(getBaseZOrder() + 1, true, false);
        props.setColor(getBaseColor());
        labelBuilder.setText(text);
        labelBuilder.setFont(myFont);
        labelBuilder.setRapidUpdate(true);

        int xLoc = (int)(drawBox.getUpperLeft().getX() + drawBox.getWidth() * getHorizontalAlignment());
        int yLoc = (int)(drawBox.getLowerRight().getY() - drawBox.getHeight() * getVerticalAlignment());
        labelBuilder.setHorizontalAlignment(getHorizontalAlignment());
        labelBuilder.setVerticalAlignment(getVerticalAlignment());
        labelBuilder.setPosition(new ScreenPosition(xLoc, yLoc));

        return new LabelGeometry(labelBuilder, props, null);
    }

    /** Builder class for text labels. */
    public static class Builder
    {
        /** Color of the text. */
        private Color myColor = Color.WHITE;

        /** Text font. */
        private String myFont = Font.SANS_SERIF + " PLAIN 13";

        /** The text of the label. */
        private String myText;

        /**
         * Get the color.
         *
         * @return the color
         */
        public Color getColor()
        {
            return myColor;
        }

        /**
         * Get the font.
         *
         * @return the font
         */
        public String getFont()
        {
            return myFont;
        }

        /**
         * Get the text.
         *
         * @return the text
         */
        public String getText()
        {
            return myText;
        }

        /**
         * Set the color.
         *
         * @param color the color to set
         */
        public void setColor(Color color)
        {
            myColor = color;
        }

        /**
         * Set the font.
         *
         * @param font the font to set
         */
        public void setFont(String font)
        {
            myFont = font;
        }

        /**
         * Set the text.
         *
         * @param text the text to set
         */
        public void setText(String text)
        {
            myText = text;
        }
    }
}
