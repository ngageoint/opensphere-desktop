package io.opensphere.wfs.gml311;

/**
 * GML SAX handler for MultiSurface features.
 */
public class GmlMultiSurfaceHandler extends GmlMultiPolygonHandler
{
    /**
     * Instantiates a new SAX handler for GML MultiSurfaces.
     *
     * @param tagName the geometry tag name
     * @param isLatBeforeLon flag indicating position order in points
     */
    public GmlMultiSurfaceHandler(String tagName, boolean isLatBeforeLon)
    {
        super(tagName, isLatBeforeLon);
    }

    @Override
    protected String getMemberName()
    {
        return "surfaceMember";
    }

    @Override
    protected String getMembersName()
    {
        return "surfaceMembers";
    }
}
