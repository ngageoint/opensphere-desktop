package io.opensphere.wfs.gml311;

import java.util.List;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.geom.AbstractMapGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolygonGeometrySupport;

/**
 * GML SAX handler for Polygon features.
 *
 * NOTE: This handler does not process interior Polygon rings because the
 * mantle/core functions do not handle them. Once those capabilities are
 * developed, interior ring handling should be added in this class.
 */
public class GmlPolygonHandler extends GmlLinestringHandler
{
    /** Constant XML tag for GML Polygon exterior rings. */
    private static final String EXTERIOR_RING_TAG = "exterior";

    /** Flag indicating whether current ring is an exterior polygon ring. */
    private boolean myIsExteriorRing;

    /**
     * Instantiates a new SAX handler for GML LineStrings.
     *
     * @param tagName the geometry tag name
     * @param isLatBeforeLon flag indicating position order in points
     */
    public GmlPolygonHandler(String tagName, boolean isLatBeforeLon)
    {
        super(tagName, isLatBeforeLon);
    }

    @Override
    public AbstractMapGeometrySupport getGeometry()
    {
        List<LatLonAlt> posList = getPositionList();
        // Make sure polygon is closed
        if (!posList.get(0).equals(posList.get(posList.size() - 1)))
        {
            posList.add(posList.get(0));
        }

        AbstractMapGeometrySupport mgs = new DefaultMapPolygonGeometrySupport(posList, null);
        mgs.setFollowTerrain(true, this);
        return mgs;
    }

    @Override
    public void handleClosingTag(String tag, String value)
    {
        if (POSITION_LIST_TAG.equals(tag))
        {
            if (myIsExteriorRing)
            {
                super.handleClosingTag(tag, value);
            }
        }
        else if (EXTERIOR_RING_TAG.equals(tag))
        {
            myIsExteriorRing = false;
        }
    }

    @Override
    public void handleOpeningTag(String tag)
    {
        if (EXTERIOR_RING_TAG.equals(tag))
        {
            myIsExteriorRing = true;
        }
    }
}
