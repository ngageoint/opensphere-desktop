package io.opensphere.wps.util;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import net.opengis.ows._110.ValueType;
import net.opengis.wps._100.InputDescriptionType;

/** WPS Utilities. */
public final class WpsUtilities
{
    /** Fields that represent columns. */
    private static final Set<String> COLUMN_FIELDS = Collections.unmodifiableSet(New.set("PROPERTY", "FIELDS"));

    /**
     * Determines if the input description is for a column field.
     *
     * @param inputDescription the input description
     * @return whether it's a column field
     */
    public static boolean isColumnField(InputDescriptionType inputDescription)
    {
        return COLUMN_FIELDS.contains(inputDescription.getIdentifier().getValue());
    }

    /**
     * Gets the stream of allowed ValueType values.
     *
     * @param inputDescription the input description
     * @return the stream of allowed values
     */
    public static List<String> getAllowedValues(InputDescriptionType inputDescription)
    {
        List<String> allowedValues = New.list();
        if (inputDescription.getLiteralData() != null && inputDescription.getLiteralData().getAllowedValues() != null)
        {
            allowedValues = inputDescription.getLiteralData().getAllowedValues().getValueOrRange().stream()
                    .filter(v -> v instanceof ValueType).map(v -> ((ValueType)v).getValue()).collect(Collectors.toList());
        }
        return allowedValues;
    }

    /**
     * Converts the bounding box to a string that is acceptable in a wps
     * request.
     *
     * @param boundingBox The bounding box.
     * @return An OGC compliant BBOX string.
     */
    public static String boundingBoxToString(BoundingBox<GeographicPosition> boundingBox)
    {
        StringBuilder builder = new StringBuilder();

        LatLonAlt lowerLeft = boundingBox.getLowerLeft().getLatLonAlt();
        builder.append(lowerLeft.getLonD());
        builder.append(',');
        builder.append(lowerLeft.getLatD());
        builder.append(',');

        LatLonAlt upperRight = boundingBox.getUpperRight().getLatLonAlt();
        builder.append(upperRight.getLonD());
        builder.append(',');
        builder.append(upperRight.getLatD());

        return builder.toString();
    }

    /**
     * Parses the bounding box text into a GeographicBoundingBox.
     *
     * @param bboxText the bounding box text
     * @return the GeographicBoundingBox.
     */
    public static GeographicBoundingBox parseBoundingBox(String bboxText)
    {
        double[] coords = parseCoordinates(bboxText);
        GeographicBoundingBox bbox = new GeographicBoundingBox(LatLonAlt.createFromDegrees(coords[1], coords[0]),
                LatLonAlt.createFromDegrees(coords[3], coords[2]));
        return bbox;
    }

    /**
     * Parses the bounding box text into a list of polygon locations.
     *
     * @param bboxText the bounding box text
     * @return the list of polygon locations.
     */
    public static List<LatLonAlt> parseLocations(String bboxText)
    {
        List<LatLonAlt> locations = New.list(5);
        double[] coords = toPolygonCoordinates(parseCoordinates(bboxText));
        for (int i = 0; i < coords.length; i++)
        {
            locations.add(LatLonAlt.createFromDegrees(coords[i + 1], coords[i++]));
        }
        return locations;
    }

    /**
     * Parses coordinates from the bounding box text.
     *
     * @param bboxText the bounding box text
     * @return the coordinates (minLon,minLat,maxLon,maxLat)
     */
    public static double[] parseCoordinates(String bboxText)
    {
        Utilities.checkNull(bboxText, "bbox");

        String[] coordStrings = bboxText.split(",");
        if (coordStrings.length != 4)
        {
            throw new IllegalArgumentException(
                    "Bbox must be in the format of minLon,minLat,maxLon,maxLat, but is: '" + bboxText + "'");
        }

        double[] coords = new double[4];
        int i = 0;
        for (String coordString : coordStrings)
        {
            coords[i++] = Double.parseDouble(coordString);
        }

        return coords;
    }

    /**
     * Converts bounding box coordinates to polygon coordinates.
     *
     * @param coords the bounding box coordinates
     * @return the polygon coordinates
     */
    public static double[] toPolygonCoordinates(double[] coords)
    {
        return new double[] { coords[0], coords[1], coords[0], coords[3], coords[2], coords[3], coords[2], coords[1], coords[0],
            coords[1] };
    }

    /** Disallow instantiation. */
    private WpsUtilities()
    {
    }
}
