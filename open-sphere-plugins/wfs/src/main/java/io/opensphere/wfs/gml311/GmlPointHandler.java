package io.opensphere.wfs.gml311;

import org.apache.log4j.Logger;

import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.geom.AbstractMapGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPointGeometrySupport;

/**
 * GML SAX handler for Point features.
 */
public class GmlPointHandler extends AbstractGmlGeometryHandler
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(GmlPointHandler.class);

    /** Constant for GML Point Positions. */
    private static final String POSITION_TAG = "pos";

    /** Latitude. */
    private double myLat;

    /** Longitude. */
    private double myLon;

    /**
     * Instantiates a new SAX handler for GML points.
     *
     * @param tagName the geometry tag name
     * @param isLatBeforeLon flag indicating position order in points
     */
    public GmlPointHandler(String tagName, boolean isLatBeforeLon)
    {
        super(tagName, isLatBeforeLon);
    }

    @Override
    public AbstractMapGeometrySupport getGeometry()
    {
        return new DefaultMapPointGeometrySupport(LatLonAlt.createFromDegrees(myLat, myLon, Altitude.ReferenceLevel.TERRAIN));
    }

    @Override
    public void handleClosingTag(String tag, String value)
    {
        if (POSITION_TAG.equals(tag))
        {
            try
            {
                String[] arr = value.split(" ");

                if (isLatBeforeLong())
                {
                    myLat = Double.valueOf(arr[0]).doubleValue();
                    myLon = Double.valueOf(arr[1]).doubleValue();
                }
                else
                {
                    myLon = Double.valueOf(arr[0]).doubleValue();
                    myLat = Double.valueOf(arr[1]).doubleValue();
                }

                if (myLon > 180.0)
                {
                    LOGGER.info("Repaired large longitude [" + myLon + "].");
                    myLon = myLon - 360.0;
                }
            }
            catch (NumberFormatException e)
            {
                LOGGER.warn("Error parsing position in a GML point.", e);
            }
        }
    }

    @Override
    public void handleOpeningTag(String tag)
    {
    }
}
