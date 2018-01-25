package io.opensphere.geopackage.util;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.MathUtil;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;

/**
 * Contains some coordinate conversion functions specific to geopackage classes.
 */
public final class GeoPackageCoordinateUtils
{
    /**
     * The instance of this class.
     */
    private static final GeoPackageCoordinateUtils ourInstance = new GeoPackageCoordinateUtils();

    /**
     * Used to transform from geodetic to web mercator.
     */
    private final ProjectionTransform myGeoToMercator;

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static GeoPackageCoordinateUtils getInstance()
    {
        return ourInstance;
    }

    /**
     * Not constructible.
     */
    private GeoPackageCoordinateUtils()
    {
        Projection geoProjection = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        myGeoToMercator = geoProjection.getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
    }

    /**
     * Converts the web mercator {@link BoundingBox} to a
     * {@link GeographicBoundingBox}.
     *
     * @param boundingBox The bounding box to convert.
     * @param projection The projection of the boundingBox.
     * @return The converted bounding box.
     */
    public GeographicBoundingBox convertToGeodetic(BoundingBox boundingBox, Projection projection)
    {
        BoundingBox box = boundingBox;

        if (!StringUtils.equals(String.valueOf(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM), projection.getCode()))
        {
            ProjectionTransform layerToGeo = projection.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            box = layerToGeo.transform(boundingBox);
            box = roundToNearest(box);
        }

        GeographicPosition lowerLeftCorner = new GeographicPosition(
                LatLonAlt.createFromDegrees(box.getMinLatitude(), box.getMinLongitude()));
        GeographicPosition upperRightCorner = new GeographicPosition(
                LatLonAlt.createFromDegrees(box.getMaxLatitude(), box.getMaxLongitude()));

        return new GeographicBoundingBox(lowerLeftCorner, upperRightCorner);
    }

    /**
     * Converts the geographic bounding box to a web mercator bounding box.
     *
     * @param boundingBox The bounding box to convert.
     * @return A web mercator {@link BoundingBox}.
     */
    public BoundingBox convertToWebMercator(GeographicBoundingBox boundingBox)
    {
        BoundingBox box = new BoundingBox(boundingBox.getMinLonD(), boundingBox.getMaxLonD(), boundingBox.getMinLatD(),
                boundingBox.getMaxLatD());
        return myGeoToMercator.transform(box);
    }

    /**
     * Converts the {@link GeographicBoundingBox} to a geopackage
     * {@link BoundingBox}.
     *
     * @param boundingBox The bounding box to convert.
     * @return The geopackage {@link BoundingBox}.
     */
    public BoundingBox getBoundingBox(GeographicBoundingBox boundingBox)
    {
        return new BoundingBox(boundingBox.getMinLonD(), boundingBox.getMaxLonD(), boundingBox.getMinLatD(),
                boundingBox.getMaxLatD());
    }

    /**
     * Converts the geopackage bounding box to a {@link GeographicBoundingBox}.
     *
     * @param box The box to convert.
     * @return The {@link GeographicBoundingBox}.
     */
    public GeographicBoundingBox getGeographicBoundingBox(BoundingBox box)
    {
        GeographicPosition lowerLeftCorner = new GeographicPosition(
                LatLonAlt.createFromDegrees(box.getMinLatitude(), box.getMinLongitude()));
        GeographicPosition upperRightCorner = new GeographicPosition(
                LatLonAlt.createFromDegrees(box.getMaxLatitude(), box.getMaxLongitude()));

        return new GeographicBoundingBox(lowerLeftCorner, upperRightCorner);
    }

    /**
     * Rounds the lats to the nearest 10 decimal because the project
     * calculations have math rounding issues.
     *
     * @param box The bounding box to round.
     * @return The bounding box.
     */
    private BoundingBox roundToNearest(BoundingBox box)
    {
        double roundedMinLat = MathUtil.roundDecimalPlace(box.getMinLatitude(), 10);
        double roundedMaxLat = MathUtil.roundDecimalPlace(box.getMaxLatitude(), 10);
        double roundedMinLon = MathUtil.roundDecimalPlace(box.getMinLongitude(), 10);
        double roundedMaxLon = MathUtil.roundDecimalPlace(box.getMaxLongitude(), 10);

        return new BoundingBox(roundedMinLon, roundedMaxLon, roundedMinLat, roundedMaxLat);
    }
}
