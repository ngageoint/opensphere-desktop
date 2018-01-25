package io.opensphere.core.geometry;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.LOBRenderProperties;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.GeographicBody3D;

/**
 * A {@link Geometry} that models a line of bearing.
 */
public class LineOfBearingGeometry extends PolylineGeometry
{
    /** To determine if arrow should be displayed or not. */
    private final boolean myDisplayArrow;

    /** The line orientation (degrees clockwise from north). */
    private final float myLineOrientation;

    /** The geographic location of this geometry. */
    private final GeographicPosition myPosition;

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public LineOfBearingGeometry(LineOfBearingGeometry.Builder builder, LOBRenderProperties renderProperties,
            Constraints constraints)
    {
        super(builder, renderProperties, constraints, builder.isLineSmoothing(), builder.getLineType());
        myLineOrientation = builder.getLineOrientation();
        myPosition = builder.getPosition();
        myDisplayArrow = builder.isDisplayArrow();

        createArrow(renderProperties);
    }

    @Override
    public LineOfBearingGeometry clone()
    {
        return (LineOfBearingGeometry)super.clone();
    }

    @Override
    public Builder createBuilder()
    {
        Builder builder = (Builder)super.createBuilder();
        builder.setLineOrientation(getLineOrientation());
        builder.setDisplayArrow(isDisplayArrow());
        builder.setPosition(getPosition());
        return builder;
    }

    @Override
    public LineOfBearingGeometry derive(BaseRenderProperties renderProperties, Constraints constraints) throws ClassCastException
    {
        return new LineOfBearingGeometry(createBuilder(), (LOBRenderProperties)renderProperties, constraints);
    }

    /**
     * Accessor for the line orientation (degrees clockwise from north).
     *
     * @return The line orientation.
     */
    public final float getLineOrientation()
    {
        return myLineOrientation;
    }

    /**
     * Accessor for the position.
     *
     * @return The geographic position of geometry.
     */
    public GeographicPosition getPosition()
    {
        return myPosition;
    }

    @Override
    public LOBRenderProperties getRenderProperties()
    {
        return (LOBRenderProperties)super.getRenderProperties();
    }

    /**
     * Accessor for whether the display arrow should be displayed.
     *
     * @return True if displaying directional arrow, false otherwise.
     */
    public boolean isDisplayArrow()
    {
        return myDisplayArrow;
    }

    @Override
    protected Builder createRawBuilder()
    {
        return new Builder();
    }

    /**
     * Create the arrow portion of the geometry.
     *
     * @param renderProperties The render properties.
     * @throws IllegalArgumentException when the vertices cannot be used to
     *             build a valid polyline.
     */
    private void createArrow(LOBRenderProperties renderProperties) throws IllegalArgumentException
    {
        List<GeographicPosition> vertices = new ArrayList<>();

        // Need to find target point
        LatLonAlt targetLLA = GeographicBody3D.greatCircleEndPosition(myPosition.getLatLonAlt(),
                Math.toRadians(myLineOrientation), WGS84EarthConstants.RADIUS_MEAN_M, renderProperties.getLineLength());
        targetLLA = targetLLA.addDegreesMeters(0, 0, renderProperties.getBaseAltitude());

        GeographicPosition target = new GeographicPosition(targetLLA);

        // Add line of bearing.
        vertices.add(myPosition);
        vertices.add(target);

        if (myDisplayArrow)
        {
            // Use great circle calculations to find directional arrow
            // locations.
            LatLonAlt right = GeographicBody3D.greatCircleEndPosition(target.getLatLonAlt(),
                    Math.toRadians(180 + myLineOrientation - 20), WGS84EarthConstants.RADIUS_MEAN_M,
                    renderProperties.getDirectionalArrowLength());
            right = right.addDegreesMeters(0, 0, renderProperties.getBaseAltitude());

            LatLonAlt left = GeographicBody3D.greatCircleEndPosition(target.getLatLonAlt(),
                    Math.toRadians(180 + myLineOrientation + 20), WGS84EarthConstants.RADIUS_MEAN_M,
                    renderProperties.getDirectionalArrowLength());
            left = left.addDegreesMeters(0, 0, renderProperties.getBaseAltitude());

            // Add directional arrow.
            vertices.add(new GeographicPosition(right));
            vertices.add(target);
            vertices.add(new GeographicPosition(left));
        }

        setVertices(vertices);
    }

    /**
     * Builder for the geometry.
     *
     */
    public static class Builder extends PolylineGeometry.Builder<GeographicPosition>
    {
        /** To determine if arrow should be displayed or not. */
        private boolean myDisplayArrow;

        /** The line orientation (degrees clockwise from north). */
        private float myLineOrientation;

        /** The geographic location of this geometry. */
        private GeographicPosition myPosition;

        /**
         * Accessor for the line orientation (degrees clockwise from north).
         *
         * @return The line orientation.
         */
        public float getLineOrientation()
        {
            return myLineOrientation;
        }

        /**
         * Accessor for the position.
         *
         * @return The geographic position of geometry.
         */
        public GeographicPosition getPosition()
        {
            return myPosition;
        }

        /**
         * Accessor for whether the display arrow should be displayed.
         *
         * @return True if displaying directional arrow, false otherwise.
         */
        public boolean isDisplayArrow()
        {
            return myDisplayArrow;
        }

        /**
         * Set whether the display arrow should be displayed.
         *
         * @param displayArrow The display arrow boolean value.
         */
        public void setDisplayArrow(boolean displayArrow)
        {
            myDisplayArrow = displayArrow;
        }

        /**
         * Set orientation of the line (degrees clockwise from north).
         *
         * @param orientation The orientation to set.
         */
        public void setLineOrientation(float orientation)
        {
            myLineOrientation = orientation;
        }

        /**
         * Set the position of this geometry.
         *
         * @param geoPos The position (must be geographic).
         */
        public void setPosition(GeographicPosition geoPos)
        {
            myPosition = geoPos;
        }
    }
}
