package io.opensphere.server.display.model;

import java.util.Arrays;
import java.util.Collections;

import io.opensphere.server.source.OGCServerSource;

/**
 * An ArcGis GUI model for an OGCServerSource.
 */
public class ArcGisServerSourceModel extends DefaultServerSourceModel<OGCServerSource>
{
    /**
     * Constructor.
     */
    public ArcGisServerSourceModel()
    {
        super(Arrays.asList(OGCServerSource.WMS_SERVICE, OGCServerSource.WFS_SERVICE),
                Collections.singleton(OGCServerSource.WMS_GETMAP_SERVICE));
    }

    @Override
    public String getDescription()
    {
        return "ArcGIS servers provide tile and feature services.";
    }
}
