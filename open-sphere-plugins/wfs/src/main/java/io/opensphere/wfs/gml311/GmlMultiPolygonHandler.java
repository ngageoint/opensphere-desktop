package io.opensphere.wfs.gml311;

/**
 * GML SAX handler for MultiPolygon features.
 */
public class GmlMultiPolygonHandler extends AbstractGmlMultiGeometryHandler
{
    /**
     * Instantiates a new SAX handler for GML MultiPolygons.
     *
     * @param tagName the geometry tag name
     * @param isLatBeforeLon flag indicating position order in points
     */
    public GmlMultiPolygonHandler(String tagName, boolean isLatBeforeLon)
    {
        super(tagName, isLatBeforeLon);
    }

    @Override
    protected AbstractGmlGeometryHandler getGeometryHandler(String tag, boolean isLatBeforeLong)
    {
        if (GeometryHandlerFactory.GML_POLYGON_TAG.equals(tag))
        {
            return GeometryHandlerFactory.getGeometryHandler(tag, isLatBeforeLong);
        }
        return null;
    }

    @Override
    protected String getMemberName()
    {
        return "polygonMember";
    }

    @Override
    protected String getMembersName()
    {
        return "polygonMembers";
    }
}
