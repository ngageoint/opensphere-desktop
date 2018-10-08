package io.opensphere.core.geometry;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.TrackRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.projection.GeographicBody3D;

/**
 * A model of connected lines as tracks drawn on the screen that have nodes and
 * directional arrows.
 */
public class TrackGeometry extends GeometryGroupGeometry
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TrackGeometry.class);

    /** The PolylineGeometry used to describe tracks. */
    private PolylineGeometry myLines;

    /** The render properties. */
    private TrackRenderProperties myRenderProperties;

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     */
    public TrackGeometry(TrackGeometry.Builder builder, ZOrderRenderProperties renderProperties)
    {
        super(builder, renderProperties);
        myLines = builder.getLineGeometry();
        myRenderProperties = builder.getRenderProperties();

        if (myLines == null || myLines.getVertices().isEmpty())
        {
            LOGGER.error("Unable to create track geometry: invalid initial polyLine describing tracks");
            return;
        }
        // Add original track lines to my geometries.
        getGeometries().add(myLines);

        if (myRenderProperties.getNodeSize() > 0)
        {
            createNodes(builder);
        }
        if (myRenderProperties.getArrowLengthScale() > 0 && myRenderProperties.getArrowWidth() > 0)
        {
            createArrows(builder);
        }
    }

    @Override
    public TrackGeometry clone()
    {
        TrackGeometry clone = (TrackGeometry)super.clone();
        clone.myLines = myLines.clone();
        clone.myRenderProperties = myRenderProperties.clone();
        return clone;
    }

    /**
     * Get the line geometry that describe the tracks.
     *
     * @return The PolylineGeometry.
     */
    public PolylineGeometry getLineGeometry()
    {
        return myLines;
    }

    @Override
    public TrackRenderProperties getRenderProperties()
    {
        return myRenderProperties;
    }

    /**
     * Create the directional arrows for tracks.
     *
     * @param builder The builder for the geometry.
     */
    @SuppressWarnings("unchecked")
    private void createArrows(TrackGeometry.Builder builder)
    {
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(getRenderProperties().getZOrder(),
                getRenderProperties().isDrawable(), getRenderProperties().isPickable());
        props.setColor(myRenderProperties.getArrowColor());
        props.setWidth(myRenderProperties.getArrowWidth());

        GeographicPosition previous = null;
        for (GeographicPosition pos : (List<GeographicPosition>)myLines.getVertices())
        {
            if (previous != null && !previous.equals(pos))
            {
                // Create Arrows
                PolylineGeometry.Builder<GeographicPosition> arrowBuilder = new PolylineGeometry.Builder<>();
                arrowBuilder.setDataModelId(builder.getDataModelId());
                arrowBuilder.setRapidUpdate(builder.isRapidUpdate());

                List<GeographicPosition> vertices = new ArrayList<>();

                // Use great circle calculations to find directional arrow
                // locations
                double distance = GeographicBody3D.greatCircleDistanceM(pos.getLatLonAlt(), previous.getLatLonAlt(),
                        WGS84EarthConstants.RADIUS_MEAN_M);
                double scaledDistance = distance * myRenderProperties.getArrowLengthScale();
                double bearing = GeographicBody3D.greatCircleAzimuthD(previous.getLatLonAlt(), pos.getLatLonAlt());

                LatLonAlt right = GeographicBody3D.greatCircleEndPosition(pos.getLatLonAlt(), Math.toRadians(180 + bearing - 20),
                        WGS84EarthConstants.RADIUS_MEAN_M, scaledDistance);

                LatLonAlt left = GeographicBody3D.greatCircleEndPosition(pos.getLatLonAlt(), Math.toRadians(180 + bearing + 20),
                        WGS84EarthConstants.RADIUS_MEAN_M, scaledDistance);

                vertices.add(new GeographicPosition(right));
                vertices.add(pos);
                vertices.add(new GeographicPosition(left));
                arrowBuilder.setVertices(vertices);

                PolylineGeometry line = new PolylineGeometry(arrowBuilder, props, null);

                // Add it to our geometries
                getGeometries().add(line);
            }
            previous = pos;
        }
    }

    /**
     * Create the nodes.
     *
     * @param builder The builder for the geometry.
     */
    private void createNodes(TrackGeometry.Builder builder)
    {
        PointRenderProperties props = new DefaultPointRenderProperties(getRenderProperties().getZOrder(),
                getRenderProperties().isDrawable(), getRenderProperties().isPickable(), false);
        props.setColor(myRenderProperties.getNodeColor());
        props.setSize(myRenderProperties.getNodeSize());

        for (Position pos : myLines.getVertices())
        {
            // Create nodes
            PointGeometry.Builder<Position> pointBuilder = new PointGeometry.Builder<>();
            pointBuilder.setRapidUpdate(builder.isRapidUpdate());
            pointBuilder.setDataModelId(builder.getDataModelId());

            // Add a meter for better display
            pointBuilder.setPosition(pos.add(new Vector3d(0, 0, 1)));

            PointGeometry point = new PointGeometry(pointBuilder, props, null);

            // Add it to our geometries
            getGeometries().add(point);
        }
    }

    /**
     * Builder for the geometry.
     */
    public static class Builder extends GeometryGroupGeometry.Builder
    {
        /** The PolylineGeometry used to describe tracks. */
        private PolylineGeometry myLines;

        /** The render properties. */
        private TrackRenderProperties myRenderProperties;

        /**
         * Constructor for the builder.
         */
        public Builder()
        {
            super(GeographicPosition.class);
        }

        /**
         * Get the line geometry.
         *
         * @return The PolylineGeometry used to describe tracks.
         */
        public PolylineGeometry getLineGeometry()
        {
            return myLines;
        }

        /**
         * Get the render properties.
         *
         * @return The render properties.
         */
        public TrackRenderProperties getRenderProperties()
        {
            return myRenderProperties;
        }

        /**
         * Set the PolylineGeometry describing the tracks to draw. The vertices
         * should be ordered from start to finish.
         *
         * @param lines The PolylineGeometry used to describe tracks.
         */
        public void setLineGeometry(PolylineGeometry lines)
        {
            if (!GeographicPosition.class.isAssignableFrom(lines.getPositionType()))
            {
                throw new IllegalArgumentException(
                        "Wrong position type for PolylineGeometry.  It must be GeographicPosition and is type: "
                                + lines.getPositionType().getSimpleName());
            }
            myLines = lines;
            setDataModelId(lines.getDataModelId());
        }

        /**
         * Set the render properties for this geometry.
         *
         * @param renderProperties The track render properties.
         */
        public void setRenderProperties(TrackRenderProperties renderProperties)
        {
            myRenderProperties = renderProperties;
        }
    }
}
