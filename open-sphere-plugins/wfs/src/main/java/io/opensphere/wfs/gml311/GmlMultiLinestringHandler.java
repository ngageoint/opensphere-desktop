package io.opensphere.wfs.gml311;

/**
 * GML SAX handler for MultiLineString features.
 */
public class GmlMultiLinestringHandler extends AbstractGmlMultiGeometryHandler
{
    /**
     * Instantiates a new SAX handler for GML MultiLineStrings.
     *
     * @param tagName the geometry tag name
     * @param isLatBeforeLon flag indicating position order in points
     */
    public GmlMultiLinestringHandler(String tagName, boolean isLatBeforeLon)
    {
        super(tagName, isLatBeforeLon);
    }

    @Override
    protected AbstractGmlGeometryHandler getGeometryHandler(String tag, boolean isLatBeforeLong)
    {
        if (GeometryHandlerFactory.GML_LINESTRING_TAG.equals(tag))
        {
            return GeometryHandlerFactory.getGeometryHandler(tag, isLatBeforeLong);
        }
        return null;
    }

    @Override
    protected String getMemberName()
    {
        return "lineStringMember";
    }

    @Override
    protected String getMembersName()
    {
        return "lineStringMembers";
    }
}
