package io.opensphere.wms.capabilities;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.wms.sld.WMSUserDefinedSymbolization;
import net.opengis.wms_130.Get;
import net.opengis.wms_130.Layer;
import net.opengis.wms_130.OnlineResource;
import net.opengis.wms_130.OperationType;
import net.opengis.wms_130.WMSCapabilities;

/**
 * A WMS 1.3.0 implementation of {@link WMSServerCapabilities}.
 */
public class WMS130Capabilities implements WMSServerCapabilities
{
    /**
     * The capabilities document.
     */
    private final WMSCapabilities myCapabilities;

    /**
     * Constructor.
     *
     * @param caps The wrapped 1.3.0 capabilities.
     */
    public WMS130Capabilities(WMSCapabilities caps)
    {
        myCapabilities = Utilities.checkNull(caps, "caps");
    }

    @Override
    public String getAccessConstraints()
    {
        return myCapabilities.getService().getAccessConstraints();
    }

    /**
     * Access the wrapped capabilities.
     *
     * @return The wrapped capabilities.
     */
    public WMSCapabilities getCapabilities()
    {
        return myCapabilities;
    }

    @Override
    public Collection<String> getExceptionFormats()
    {
        Set<String> formats = New.set();
        if (myCapabilities.getCapability().getException() == null
                || myCapabilities.getCapability().getException().getFormat() == null
                || myCapabilities.getCapability().getException().getFormat().isEmpty())
        {
            formats.add("application/vnd.ogc.se_xml");
        }
        else
        {
            for (String fmt : myCapabilities.getCapability().getException().getFormat())
            {
                formats.add(fmt);
            }
        }
        return formats;
    }

    @Override
    public Collection<String> getGetMapFormats()
    {
        Set<String> formats = New.set();
        if (myCapabilities.getCapability().getRequest() == null || myCapabilities.getCapability().getRequest().getGetMap() == null
                || myCapabilities.getCapability().getRequest().getGetMap().getFormat() == null
                || myCapabilities.getCapability().getRequest().getGetMap().getFormat().isEmpty())
        {
            formats.add(MimeType.PNG.getMimeType());
        }
        else
        {
            for (String fmt : myCapabilities.getCapability().getRequest().getGetMap().getFormat())
            {
                formats.add(fmt);
            }
        }
        return formats;
    }

    @Override
    public String getGetMapURL()
    {
        /* A note on whether GET is required in WMS versions 1.1.1 and 1.3.0
         *
         * In WMS 1.1.1, GET was not explicitly required, but according to
         * section 6.2 of the spec: "The basic WMS specification only defines
         * HTTP GET for invoking operations. (A Styled Layer Descriptor WMS [3]
         * defines HTTP POST for some operations.)"
         *
         * GET is required per Section 6.3.1 of the WMS 1.3.0 Specification:
         * "HTTP supports two request methods: GET and POST. One or both of
         * these methods may be offered by a server, and the use of the Online
         * Resource URL differs in each case. Support for the GET method is
         * mandatory; support for the POST method is optional." */
        OperationType gMap = myCapabilities.getCapability().getRequest().getGetMap();
        Get get = gMap.getDCPType().get(0).getHTTP().getGet();
        if (get != null)
        {
            OnlineResource res = get.getOnlineResource();
            String getMapUrl = res.getHref();
            if (getMapUrl != null)
            {
                return getMapUrl;
            }
        }

        return null;
    }

    @Override
    public Collection<WMSCapsLayer> getLayerList()
    {
        Collection<WMSCapsLayer> layerList = New.list();
        if (myCapabilities.getCapability() == null || myCapabilities.getCapability().getLayer() == null)
        {
            return layerList;
        }
        Layer topLayer = myCapabilities.getCapability().getLayer();
        if (topLayer != null)
        {
            buildLayerList(topLayer, layerList);
        }
        return layerList;
    }

    @Override
    public String getTitle()
    {
        if (myCapabilities.getService() != null && StringUtils.isNotEmpty(myCapabilities.getService().getTitle()))
        {
            return myCapabilities.getService().getTitle();
        }
        else if (myCapabilities.getCapability() != null && myCapabilities.getCapability().getLayer() != null
                && StringUtils.isNotEmpty(myCapabilities.getCapability().getLayer().getTitle()))
        {
            return myCapabilities.getCapability().getLayer().getTitle();
        }
        return null;
    }

    @Override
    public WMSUserDefinedSymbolization getUserDefinedSymbolization()
    {
        // TODO Write a follow-on ticket for SLD.
//        // Check to see if this server supports SLD
//        UserDefinedSymbolization symbol = myCapabilities.getCapability() == null ? null : myCapabilities.getCapability()
//                .getUserDefinedSymbolization();
//        if (symbol != null && symbol.getSupportSLD().equals("1"))
//        {
//            WMSUserDefinedSymbolization sldConf = new WMSUserDefinedSymbolization();
//            sldConf.setSupportsUserLayer(symbol.getUserLayer().equals("1"));
//            sldConf.setSupportsUserStyle(symbol.getUserStyle().equals("1"));
//            sldConf.setSupportsRemoteWFS(symbol.getRemoteWFS().equals("1"));
//            return sldConf;
//        }
//        else
//        {
//            return null;
//        }
        return null;
    }

    @Override
    public String getVersion()
    {
        return "1.3.0";
    }

    /**
     * Recursive function that builds up a flat list of server layers.
     *
     * @param layer the current layer
     * @param layerList the list of layers that should be added to
     */
    private void buildLayerList(Layer layer, Collection<WMSCapsLayer> layerList)
    {
        // Per the WMS spec, if a layer has a name, it is a data layer.
        if (StringUtils.isNotEmpty(layer.getName()))
        {
            layerList.add(new WMS130CapsLayer(layer));
        }
        if (CollectionUtilities.hasContent(layer.getLayer()))
        {
            for (Layer child : layer.getLayer())
            {
                buildLayerList(child, layerList);
            }
        }
    }
}
