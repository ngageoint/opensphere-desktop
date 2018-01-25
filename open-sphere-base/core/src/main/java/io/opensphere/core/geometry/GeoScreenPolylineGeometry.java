package io.opensphere.core.geometry;

import java.util.List;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.Utilities;

/**
 * A polyline geometry which is in screen coordinates, but is anchored to a
 * geographic position. The screen coordinates are translated so that the origin
 * is at the geographic anchor. So, as the anchor moves, the polyline will move
 * with it.
 */
public class GeoScreenPolylineGeometry extends PolylineGeometry
{
    /** Geographic position to which this polyline is attached. */
    private final GeographicPosition myAttachment;

    /**
     * Constructor.
     *
     * @param builder the builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public GeoScreenPolylineGeometry(Builder builder, PolylineRenderProperties renderProperties, Constraints constraints)
    {
        super(builder, renderProperties, constraints);
        myAttachment = Utilities.checkNull(builder.getAttachment(), "builder.getAttachment()");
    }

    /**
     * Special constructor for subclasses.
     *
     * @param builder The abstract geometry builder.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     * @param smooth When true, line smoothing is used.
     * @param type Type type of line.
     */
    protected GeoScreenPolylineGeometry(Builder builder, PolylineRenderProperties renderProperties, Constraints constraints,
            boolean smooth, LineType type)
    {
        super(builder, renderProperties, constraints, smooth, type);
        myAttachment = Utilities.checkNull(builder.getAttachment(), "builder.getAttachment()");
    }

    @Override
    public GeoScreenPolylineGeometry clone()
    {
        return (GeoScreenPolylineGeometry)super.clone();
    }

    @Override
    public Builder createBuilder()
    {
        Builder builder = (Builder)super.createBuilder();
        builder.setAttachment(getAttachment());
        return builder;
    }

    @Override
    public GeoScreenPolylineGeometry derive(BaseRenderProperties renderProperties, Constraints constraints)
        throws ClassCastException
    {
        return new GeoScreenPolylineGeometry(createBuilder(), (PolylineRenderProperties)renderProperties, constraints);
    }

    /**
     * Get the attachment.
     *
     * @return the attachment
     */
    public GeographicPosition getAttachment()
    {
        return myAttachment;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends ScreenPosition> getVertices()
    {
        return (List<? extends ScreenPosition>)super.getVertices();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(150);
        sb.append(getClass().getSimpleName()).append(' ').append(hashCode()).append(" [").append(getVertices())
                .append(", attachment: ").append(getAttachment()).append(']');
        return sb.toString();
    }

    @Override
    protected Builder createRawBuilder()
    {
        return new Builder();
    }

    /** Builder for the geometry. */
    public static class Builder extends PolylineGeometry.Builder<ScreenPosition>
    {
        /** Geographic position to which this polyline is attached. */
        private GeographicPosition myAttachment;

        /**
         * Get the attachment.
         *
         * @return the attachment
         */
        public GeographicPosition getAttachment()
        {
            return myAttachment;
        }

        /**
         * Set the attachment.
         *
         * @param attachment the attachment to set
         */
        public void setAttachment(GeographicPosition attachment)
        {
            myAttachment = attachment;
        }
    }
}
