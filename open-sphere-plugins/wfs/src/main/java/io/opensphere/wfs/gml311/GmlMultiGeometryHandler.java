package io.opensphere.wfs.gml311;

/**
 * GML SAX handler for MultiGeometry features.
 */
public class GmlMultiGeometryHandler extends AbstractGmlMultiGeometryHandler
{
    /**
     * Instantiates a new SAX handler for GML MultiGeometries.
     *
     * @param tagName the geometry tag name
     * @param isLatBeforeLon flag indicating position order in points
     */
    public GmlMultiGeometryHandler(String tagName, boolean isLatBeforeLon)
    {
        super(tagName, isLatBeforeLon);
    }

    @Override
    protected AbstractGmlGeometryHandler getGeometryHandler(String tag, boolean isLatBeforeLong)
    {
        return GeometryHandlerFactory.getGeometryHandler(tag, isLatBeforeLong);
    }

    @Override
    protected String getMemberName()
    {
        return "geometryMember";
    }

    @Override
    protected String getMembersName()
    {
        return "geometryMembers";
    }
}
