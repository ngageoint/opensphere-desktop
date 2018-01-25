package io.opensphere.wfs.gml311;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.geom.AbstractMapGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolylineGeometrySupport;

/**
 * GML SAX handler for LineString features.
 */
public class GmlLinestringHandler extends AbstractGmlGeometryHandler
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(GmlLinestringHandler.class);

    /** Constant XML tag for list of GML Point Positions. */
    protected static final String POSITION_LIST_TAG = "posList";

    /** List or translated points. */
    private final List<LatLonAlt> myLatLonList = New.list();

    /**
     * Instantiates a new SAX handler for GML LineStrings.
     *
     * @param tagName the geometry tag name
     * @param isLatBeforeLon flag indicating position order in points
     */
    public GmlLinestringHandler(String tagName, boolean isLatBeforeLon)
    {
        super(tagName, isLatBeforeLon);
    }

    @Override
    public AbstractMapGeometrySupport getGeometry()
    {
        AbstractMapGeometrySupport mgs = new DefaultMapPolylineGeometrySupport(myLatLonList);
        mgs.setFollowTerrain(true, this);
        return mgs;
    }

    /**
     * Protected accessor for the position list.
     *
     * NOTE: Only intended for inherited classes because this returns the live
     * list, not a safe copy.
     *
     * @return the list of positions
     */
    protected List<LatLonAlt> getPositionList()
    {
        return myLatLonList;
    }

    @Override
    public void handleClosingTag(String tag, String value)
    {
        if (POSITION_LIST_TAG.equals(tag))
        {
            boolean isLat = isLatBeforeLong();
            int positions = 0;
            double lat = 0.0;
            double lon = 0.0;
            for (String entry : value.split(" "))
            {
                if (StringUtils.isNotEmpty(entry)) // ignore extra spaces
                {
                    boolean hasError = false;
                    try
                    {
                        if (isLat)
                        {
                            lat = Double.valueOf(entry);
                        }
                        else
                        {
                            lon = Double.valueOf(entry);
                            if (lon > 180.0)
                            {
                                lon = lon - 360.0;
                            }
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        hasError = true;
                        LOGGER.warn("Parse error in posList parsing top element: \"" + entry + "\"", e);
                    }
                    isLat = !isLat;
                    if (++positions == 2)
                    {
                        if (!hasError)
                        {
                            myLatLonList.add(LatLonAlt.createFromDegrees(lat, lon, Altitude.ReferenceLevel.TERRAIN));
                        }
                        positions = 0;
                    }
                }
            }
        }
    }

    @Override
    public void handleOpeningTag(String tag)
    {
    }
}
