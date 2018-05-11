package io.opensphere.myplaces.specific.regions.utils;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import de.micromata.opengis.kml.v_2_2_0.BalloonStyle;
import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.PolyStyle;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleSelector;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.DefaultColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.GeographicPositionArrayList;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.mp.impl.DefaultMapAnnotationPoint;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.util.ExtendedDataUtils;
import io.opensphere.myplaces.util.PlacemarkUtils;

/**
 * Utility class for regions.
 */
public final class RegionUtils
{
    /**
     * Create a {@link PolygonGeometry} from a {@link Polygon}.
     *
     * @param placemark The placemark containing the polygon.
     * @return A polygon geometry.
     */
    public static PolygonGeometry createGeometry(Placemark placemark)
    {
        PolygonGeometry.Builder<GeographicPosition> builder = new PolygonGeometry.Builder<>();
        PolygonRenderProperties props = new DefaultPolygonRenderProperties(ZOrderRenderProperties.TOP_Z - 1000, true, true);

        Style style = null;
        for (StyleSelector selector : placemark.getStyleSelector())
        {
            if (selector instanceof Style)
            {
                style = (Style)selector;

                if (ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_POLYGON_FILLED_ID, false))
                {
                    PolyStyle polyStyle = style.getPolyStyle();
                    Color fillColor = ColorUtilities.convertFromHexString(polyStyle.getColor(), 3, 2, 1, 0);
                    DefaultColorRenderProperties fillColorProps = new DefaultColorRenderProperties(
                            ZOrderRenderProperties.TOP_Z - 1000, true, true, true);
                    fillColorProps.setColor(fillColor);
                    props = new DefaultPolygonRenderProperties(ZOrderRenderProperties.TOP_Z - 1000, true, true, fillColorProps);
                    polyStyle.setFill(Boolean.valueOf(
                            ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_POLYGON_FILLED_ID, false)));
                }

                IconStyle iconStyle = style.getIconStyle();
                Color color = ColorUtilities.convertFromHexString(iconStyle.getColor(), 3, 2, 1, 0);
                props.setColor(color);

                break;
            }
        }

        props.setWidth(4);

        Polygon polygon = (Polygon)placemark.getGeometry();
        List<GeographicPosition> points = convertToPosition(polygon.getOuterBoundaryIs().getLinearRing());
        builder.setVertices(points);

        List<Boundary> inBnd = polygon.getInnerBoundaryIs();
        if (inBnd != null && !inBnd.isEmpty())
        {
            Collection<List<? extends GeographicPosition>> innerRings = New.collection(inBnd.size());
            for (Boundary ring : inBnd)
            {
                innerRings.add(convertToPosition(ring.getLinearRing()));
            }
            builder.addHoles(innerRings);
        }

        builder.setDataModelId(DefaultMapAnnotationPoint.getNextId());

        Constraints constraints = null;
        if (placemark.getTimePrimitive() != null)
        {
            TimeConstraint contraint = TimeConstraint
                    .getTimeConstraint(KMLSpatialTemporalUtils.timeSpanFromTimePrimitive(placemark.getTimePrimitive()));
            constraints = new Constraints(contraint);
        }

        return new PolygonGeometry(builder, props, constraints);
    }

    /**
     * Create a new {@link Placemark} that contains a polygon representing the
     * given region and add it to the specified Folder.
     *
     * @param folder The KML folder for the Placemark.
     * @param name The name for the polygon.
     * @param exteriorRing The exterior ring of the polygon.
     * @param interiorRings The interior rings of the polygon.
     *
     * @return The created placemark.
     */
    public static Placemark createRegionFromLLAs(Folder folder, String name, List<? extends LatLonAlt> exteriorRing,
            Collection<? extends List<? extends LatLonAlt>> interiorRings)
    {
        Placemark p = regionPlacemark(name, exteriorRing, interiorRings);
        folder.addToFeature(p);
        return p;
    }

    /**
     * Create a new {@link Placemark} that contains a polygon representing the
     * given region.
     *
     * @param name The name for the polygon.
     * @param exteriorRing The exterior ring of the polygon.
     * @param interiorRings The interior rings of the polygon.
     *
     * @return The created placemark.
     */
    public static Placemark regionPlacemark(String name, List<? extends LatLonAlt> exteriorRing,
            Collection<? extends List<? extends LatLonAlt>> interiorRings)
    {
        Placemark p = new Placemark();
        p.setId(UUID.randomUUID().toString());
        p.setName(name);
        p.setVisibility(Boolean.TRUE);

        Polygon poly = p.createAndSetPolygon();
        poly.createAndSetOuterBoundaryIs().setLinearRing(convertToKML(exteriorRing));

        if (interiorRings != null)
        {
            for (List<? extends LatLonAlt> hole : interiorRings)
            {
                poly.createAndAddInnerBoundaryIs().setLinearRing(convertToKML(hole));
            }
        }

        Style style = PlacemarkUtils.setPlacemarkColor(p, Color.GRAY);

        BalloonStyle bSty = new BalloonStyle();
        bSty.setColor(ColorUtilities.convertToHexString(Color.GRAY, 3, 2, 1, 0));
        bSty.setTextColor(ColorUtilities.convertToHexString(Color.WHITE, 3, 2, 1, 0));

        style.setBalloonStyle(bSty);

        ExtendedData ed = new ExtendedData();
        ExtendedDataUtils.putVisualizationType(ed, MapVisualizationType.ANNOTATION_REGIONS);
        ExtendedDataUtils.putBoolean(ed, Constants.IS_ANNOHIDE_ID, false);
        ExtendedDataUtils.putBoolean(ed, Constants.IS_FEATURE_ON_ID, true);
        ExtendedDataUtils.putBoolean(ed, Constants.IS_TITLE, true);
        ExtendedDataUtils.putBoolean(ed, Constants.IS_DESC_ID, true);
        p.setExtendedData(ed);

        return p;
    }

    /**
     * Create a new {@link MyPlacesDataTypeInfo} that contains a KML polygon
     * representing the given region. Add the new data type info to the given
     * parent data group.
     *
     * @param folder The KML folder to contain the Placemark.
     * @param name The name for the polygon.
     * @param exteriorRing The vertices of the polygon.
     * @param interiorRings The interior rings of the polygon.
     * @return The created placemark.
     */
    public static Placemark createRegionFromPositions(Folder folder, String name, List<? extends Position> exteriorRing,
            Collection<? extends List<? extends Position>> interiorRings)
    {
        List<LatLonAlt> llaExteriorRing = New.list(exteriorRing.size());
        for (Position pos : exteriorRing)
        {
            llaExteriorRing.add(((GeographicPosition)pos).getLatLonAlt());
        }

        Collection<List<LatLonAlt>> llaInteriorRings = null;
        if (interiorRings != null && !interiorRings.isEmpty())
        {
            llaInteriorRings = New.collection();
            for (List<? extends Position> hole : interiorRings)
            {
                List<LatLonAlt> llaInteriorRing = New.list(hole.size());
                for (Position pos : hole)
                {
                    llaInteriorRing.add(((GeographicPosition)pos).getLatLonAlt());
                }
                llaInteriorRings.add(llaInteriorRing);
            }
        }

        return createRegionFromLLAs(folder, name, llaExteriorRing, llaInteriorRings);
    }

    /**
     * Convert the ring of positions to a KML linear ring.
     *
     * @param llaRing the positions to convert
     * @return The newly created linear ring.
     */
    private static LinearRing convertToKML(List<? extends LatLonAlt> llaRing)
    {
        boolean closeRing = !llaRing.get(0).equals(llaRing.get(llaRing.size() - 1));
        LinearRing linearRing = new LinearRing();
        for (LatLonAlt location : llaRing)
        {
            linearRing.addToCoordinates(location.getLonD(), location.getLatD(), location.getAltM());
        }
        if (closeRing)
        {
            Coordinate first = linearRing.getCoordinates().get(0);
            linearRing.addToCoordinates(first.getLongitude(), first.getLatitude(), first.getAltitude());
        }

        return linearRing;
    }

    /**
     * Create geographic positions from the points in the ring.
     *
     * @param ring The ring to convert to positions.
     * @return The newly created position.
     */
    private static List<GeographicPosition> convertToPosition(LinearRing ring)
    {
        List<Coordinate> vertices = ring.getCoordinates();
        double[] data = new double[vertices.size() * 3];

        for (int i = 0, j = 0; i < vertices.size(); ++i, j += 3)
        {
            Coordinate coord = vertices.get(i);
            data[j] = coord.getLatitude();
            data[j + 1] = coord.getLongitude();
            data[j + 2] = coord.getAltitude();
        }

        return GeographicPositionArrayList.createFromDegreesMeters(data, ReferenceLevel.TERRAIN);
    }

    /** Disallow instantiation. */
    private RegionUtils()
    {
    }
}
