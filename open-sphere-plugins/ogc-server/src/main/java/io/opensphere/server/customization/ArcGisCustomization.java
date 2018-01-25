package io.opensphere.server.customization;

import org.w3c.dom.Node;
import org.w3c.dom.Text;

import net.opengis.wfs._110.WFSCapabilitiesType;

/**
 * The Server customizations specific to ArcGIS servers.
 */
public class ArcGisCustomization extends DefaultCustomization
{
    /* Arc servers allow administrators to specify the lat/lon ordering in the
     * extended capabilities, so this method will check that first before
     * falling back to the default. */
    @Override
    public LatLonOrder getLatLonOrder(WFSCapabilitiesType wfsCap)
    {
        LatLonOrder order = LatLonOrder.UNKNOWN;
        Object ext = wfsCap.getOperationsMetadata().getExtendedCapabilities();

        if (ext != null)
        {
            Node constraint = ((Node)ext).getFirstChild();
            if (constraint.hasChildNodes())
            {
                Node swappable = constraint.getFirstChild();
                if (swappable.getLocalName().equals("Value"))
                {
                    String value = ((Text)swappable.getFirstChild()).getData();
                    if (value.matches("(.*?)((?i)lat)(.*?)((?i)lon)(.*?)"))
                    {
                        order = LatLonOrder.LATLON;
                    }
                }
            }
        }
        if (order == LatLonOrder.UNKNOWN)
        {
            order = LatLonOrder.LONLAT;
        }
        return order;
    }

    @Override
    public String getServerType()
    {
        return "ArcGIS";
    }

    @Override
    public String getSrsName()
    {
        return "urn:ogc:def:crs:EPSG:6.9:4326";
    }
}
