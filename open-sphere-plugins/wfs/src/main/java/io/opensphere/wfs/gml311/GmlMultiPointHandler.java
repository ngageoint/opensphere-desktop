package io.opensphere.wfs.gml311;

/**
 * GML SAX handler for MultiPoint features.
 */
public class GmlMultiPointHandler extends AbstractGmlMultiGeometryHandler
{
    /**
     * Instantiates a new SAX handler for GML MultiPoints.
     *
     * @param tagName the geometry tag name
     * @param isLatBeforeLon flag indicating position order in points
     */
    public GmlMultiPointHandler(String tagName, boolean isLatBeforeLon)
    {
        super(tagName, isLatBeforeLon);
    }

    @Override
    protected AbstractGmlGeometryHandler getGeometryHandler(String tag, boolean isLatBeforeLong)
    {
        if (GeometryHandlerFactory.GML_POINT_TAG.equals(tag))
        {
            return GeometryHandlerFactory.getGeometryHandler(tag, isLatBeforeLong);
        }
        return null;
    }

    @Override
    protected String getMemberName()
    {
        return "pointMember";
    }

    @Override
    protected String getMembersName()
    {
        return "pointMembers";
    }
}
