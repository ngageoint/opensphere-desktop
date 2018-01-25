package io.opensphere.core.geometry;

import java.awt.Color;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.Utilities;

/**
 * A {@link Geometry} that models a string of text.
 */
public class LabelGeometry extends AbstractColorGeometry
{
    /**
     * The position of {@link #myPosition} relative to the label. <tt>0</tt> is
     * the beginning of the label; <tt>1</tt> is the end of the label.
     */
    // TODO this should be a mutable render property
    private final float myHorizontalAlignment;

    /**
     * When true, the text is displayed with either a light or dark outline
     * depending on the color of the text.
     */
    private final boolean myOutlined;

    /**
     * The anchor for the label.
     */
    private final Position myPosition;

    /**
     * The text string.
     */
    private final String myText;

    /**
     * The position of {@link #myPosition} relative to the label. <tt>0</tt> is
     * the bottom of the label; <tt>1</tt> is the top of the label.
     */
    // TODO this should be a mutable render property
    private final float myVerticalAlignment;

    /**
     * Construct me.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public LabelGeometry(LabelGeometry.Builder<?> builder, LabelRenderProperties renderProperties, Constraints constraints)
    {
        super(builder, renderProperties, constraints);
        myText = Utilities.checkNull(builder.getText(), "builder.getText()");
        if (builder.getFont() != null)
        {
            renderProperties.setFont(builder.getFont());
        }
        myPosition = Utilities.checkNull(builder.getPosition(), "builder.getPosition()");
        myHorizontalAlignment = builder.getHorizontalAlignment();
        myVerticalAlignment = builder.getVerticalAlignment();
        myOutlined = builder.isOutlined();
    }

    @Override
    public LabelGeometry clone()
    {
        return (LabelGeometry)super.clone();
    }

    @Override
    public Builder<? extends Position> createBuilder()
    {
        @SuppressWarnings("unchecked")
        Builder<Position> builder = (Builder<Position>)super.doCreateBuilder();
        builder.setFont(getRenderProperties().getFont());
        builder.setHorizontalAlignment(getHorizontalAlignment());
        builder.setPosition(getPosition());
        builder.setText(getText());
        builder.setVerticalAlignment(getVerticalAlignment());
        builder.setOutlined(myOutlined);
        return builder;
    }

    @Override
    public LabelGeometry derive(BaseRenderProperties renderProperties, Constraints constraints) throws ClassCastException
    {
        return new LabelGeometry(createBuilder(), (LabelRenderProperties)renderProperties, constraints);
    }

    @Override
    public GeometryOrderKey getGeometryOrderKey()
    {
        if (myOutlined)
        {
            return new LabelOutlineOrderKey();
        }
        return null;
    }

    /**
     * Get the location of the anchor point relative to the text of the label.
     * <tt>0</tt> is the beginning of the label; <tt>1</tt> is the end of the
     * label.
     *
     * @return The horizontal alignment.
     */
    public float getHorizontalAlignment()
    {
        return myHorizontalAlignment;
    }

    /**
     * Determine the outline color for this label based on the text color. This
     * will choose a light color for dark text and a dark color for light text.
     *
     * @return The outline color.
     */
    public Color getOutlineColor()
    {
        return ColorUtilities.getContrastingColor(getRenderProperties().getColor());
    }

    /**
     * Get the anchor point of the label. See {@link #getHorizontalAlignment()}
     * and {@link #getVerticalAlignment()} for more information on how this is
     * used.
     *
     * @return The anchor point.
     */
    public Position getPosition()
    {
        return myPosition;
    }

    @Override
    public Class<? extends Position> getPositionType()
    {
        return myPosition.getClass();
    }

    @Override
    public Position getReferencePoint()
    {
        return myPosition;
    }

    @Override
    public LabelRenderProperties getRenderProperties()
    {
        return (LabelRenderProperties)super.getRenderProperties();
    }

    /**
     * Get the text of the label.
     *
     * @return The text.
     */
    public String getText()
    {
        return myText;
    }

    /**
     * Get the location of the anchor point relative to the text of the label.
     * <tt>0</tt> is the bottom of the label; <tt>1</tt> is the top of the
     * label.
     *
     * @return The horizontal alignment.
     */
    public float getVerticalAlignment()
    {
        return myVerticalAlignment;
    }

    /**
     * Tell whether this label is outlined.
     *
     * @return true when this label is outlined.
     */
    public boolean isOutlined()
    {
        return myOutlined;
    }

    @Override
    public <T extends Position> boolean overlaps(BoundingBox<T> boundingBox, double tolerance)
    {
        return boundingBox.contains(getPosition(), tolerance);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append(getClass().getSimpleName()).append(" [\"").append(getText()).append("\", ").append(getPosition()).append(']');
        return sb.toString();
    }

    @Override
    protected Builder<? extends Position> createRawBuilder()
    {
        return new Builder<Position>();
    }

    /**
     * Builder for the geometry.
     *
     * @param <T> The position type associated with the geometry.
     */
    public static class Builder<T extends Position> extends AbstractGeometry.Builder
    {
        /** Builder property. */
        private String myFont;

        /** Builder property. */
        private float myHorizontalAlignment;

        /** True when the label should be outlined. */
        private boolean myOutlined;

        /** Builder property. */
        private T myPosition;

        /** Builder property. */
        private String myText;

        /** Builder property. */
        private float myVerticalAlignment;

        /**
         * Accessor for the font.
         *
         * @return The font.
         */
        public String getFont()
        {
            return myFont;
        }

        /**
         * Accessor for the horizontalAlignment.
         *
         * @return The horizontalAlignment.
         */
        public float getHorizontalAlignment()
        {
            return myHorizontalAlignment;
        }

        /**
         * Accessor for the position.
         *
         * @return The position.
         */
        public T getPosition()
        {
            return myPosition;
        }

        /**
         * Accessor for the text.
         *
         * @return The text.
         */
        public String getText()
        {
            return myText;
        }

        /**
         * Accessor for the verticalAlignment.
         *
         * @return The verticalAlignment.
         */
        public float getVerticalAlignment()
        {
            return myVerticalAlignment;
        }

        /**
         * Get the outlined.
         *
         * @return the outlined
         */
        public boolean isOutlined()
        {
            return myOutlined;
        }

        /**
         * The font for the text.
         *
         * @param font The font to set.
         */
        public void setFont(String font)
        {
            myFont = font;
        }

        /**
         * The horizontal alignment of <tt>position</tt> relative to the label,
         * with 0 being the left side of the label and 1 being the right side.
         *
         * @param horizontalAlignment The horizontalAlignment to set.
         */
        public void setHorizontalAlignment(float horizontalAlignment)
        {
            myHorizontalAlignment = horizontalAlignment;
        }

        /**
         * Set the outlined.
         *
         * @param outlined the outlined to set
         */
        public void setOutlined(boolean outlined)
        {
            myOutlined = outlined;
        }

        /**
         * The location for the label.
         *
         * @param position The position to set.
         */
        public void setPosition(T position)
        {
            myPosition = position;
        }

        /**
         * The label text.
         *
         * @param text The text to set.
         */
        public void setText(String text)
        {
            myText = text;
        }

        /**
         * The vertical alignment of <tt>position</tt> relative to the label,
         * with 0 being the bottom and 1 being the top.
         *
         * @param verticalAlignment The verticalAlignment to set.
         */
        public void setVerticalAlignment(float verticalAlignment)
        {
            myVerticalAlignment = verticalAlignment;
        }
    }

    /**
     * A processor order key which determines order based on the outline color.
     */
    public class LabelOutlineOrderKey extends GeometryOrderKey
    {
        @Override
        public int compareTo(GeometryOrderKey other)
        {
            if (!(other instanceof LabelOutlineOrderKey))
            {
                return -1;
            }
            LabelOutlineOrderKey oLabelKey = (LabelOutlineOrderKey)other;

            if (myOutlined)
            {
                if (oLabelKey.isOutlined())
                {
                    int color = getOutlineColor().getRGB();
                    int otherColor = oLabelKey.getKeyOutlineColor().getRGB();
                    return Integer.compare(color, otherColor);
                }
                return 1;
            }

            return oLabelKey.isOutlined() ? -1 : 0;
        }

        @Override
        @SuppressWarnings("PMD.OverrideMerelyCallsSuper")
        public boolean equals(Object other)
        {
            return super.equals(other);
        }

        /**
         * Get the outline color for this key.
         *
         * @return The outline color.
         */
        public Color getKeyOutlineColor()
        {
            return getOutlineColor();
        }

        @Override
        @SuppressWarnings("PMD.OverrideMerelyCallsSuper")
        public int hashCode()
        {
            return super.hashCode();
        }

        /**
         * Tell whether this label is outlined.
         *
         * @return true when this label is outlined.
         */
        public boolean isOutlined()
        {
            return myOutlined;
        }
    }
}
