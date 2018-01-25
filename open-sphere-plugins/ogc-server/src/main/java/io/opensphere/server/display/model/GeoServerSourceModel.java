package io.opensphere.server.display.model;

import io.opensphere.server.source.OGCServerSource;

/**
 * The GeoServer GUI model for an OGCServerSource.
 */
public class GeoServerSourceModel extends SingleUrlServerSourceModel
{
    @Override
    public String getDescription()
    {
        return "GeoServer provides tile (WMS) and feature (WFS) services. WMS time for tile animation is supported as of GeoServer 2.2.";
    }

    @Override
    public String getSingleUrl(OGCServerSource source)
    {
        return getFirstFullUrl(source);
    }

    @Override
    public String getUrlExample()
    {
        return "e.g. http://www.example.com/geoserver/ows";
    }

    @Override
    protected void updateDomainModel(OGCServerSource domainModel)
    {
        domainModel.setName(getServerName().get());
        if (getURL().get() != null)
        {
            domainModel.setWMSServerURL(getURL().get());
            domainModel.setWFSServerURL(getURL().get());
        }
    }

    @Override
    protected void updateViewModel(OGCServerSource domainModel)
    {
        getServerName().set(domainModel.getName());
        getURL().set(getSingleUrl(domainModel));
    }
}
