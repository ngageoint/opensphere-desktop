package io.opensphere.wfs.gml311;

/**
 * GML SAX handler for MultiCurve features.
 */
public class GmlMultiCurveHandler extends GmlMultiLinestringHandler
{
    /**
     * Instantiates a new SAX handler for GML MultiCurve.
     *
     * @param tagName the geometry tag name
     * @param isLatBeforeLon flag indicating position order in points
     */
    public GmlMultiCurveHandler(String tagName, boolean isLatBeforeLon)
    {
        super(tagName, isLatBeforeLon);
    }

    @Override
    protected String getMemberName()
    {
        return "curveMember";
    }

    @Override
    protected String getMembersName()
    {
        return "curveMembers";
    }
}
