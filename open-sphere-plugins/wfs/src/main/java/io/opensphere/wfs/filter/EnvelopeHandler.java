package io.opensphere.wfs.filter;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.geom.AbstractMapGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolygonGeometrySupport;
import io.opensphere.wfs.gml311.AbstractGmlGeometryHandler;

/**
 * Parses a gml Envelope polygon and creates a geometry for it.
 */
public class EnvelopeHandler extends AbstractGmlGeometryHandler
{
    /**
     * The envelope tag.
     */
    public static final String ENVELOPE_TAG = "Envelope";

    /**
     * The lower left corner tag.
     */
    private static final String LOWER_LEFT_TAG = "lowerCorner";

    /**
     * The upper right corner tag.
     */
    private static final String UPPER_RIGHT_TAG = "upperCorner";

    /**
     * The lower left corner.
     */
    private LatLonAlt myLowerLeft;

    /**
     * The upper right corner.
     */
    private LatLonAlt myUpperRight;

    /**
     * Constructs a new envolope handler.
     */
    public EnvelopeHandler()
    {
        super(ENVELOPE_TAG, false);
    }

    @Override
    public AbstractMapGeometrySupport getGeometry()
    {
        DefaultMapPolygonGeometrySupport geometry = null;

        if (myLowerLeft != null && myUpperRight != null)
        {
            LatLonAlt lowerRight = LatLonAlt.createFromDegrees(myLowerLeft.getLatD(), myUpperRight.getLonD());
            LatLonAlt upperLeft = LatLonAlt.createFromDegrees(myUpperRight.getLatD(), myLowerLeft.getLonD());

            geometry = new DefaultMapPolygonGeometrySupport(New.list(myLowerLeft, lowerRight, myUpperRight, upperLeft),
                    New.list());
        }

        return geometry;
    }

    @Override
    public void handleClosingTag(String tag, String value)
    {
        if (LOWER_LEFT_TAG.equals(tag) || UPPER_RIGHT_TAG.equals(tag))
        {
            String[] lonLat = value.split(" ");
            LatLonAlt latLon = LatLonAlt.createFromDegrees(Double.parseDouble(lonLat[1]), Double.parseDouble(lonLat[0]));
            if (LOWER_LEFT_TAG.equals(tag))
            {
                myLowerLeft = latLon;
            }
            else if (UPPER_RIGHT_TAG.equals(tag))
            {
                myUpperRight = latLon;
            }
        }
    }

    @Override
    public void handleOpeningTag(String tag)
    {
    }
}
