package io.opensphere.server.display.model;

import java.util.Arrays;
import java.util.Collections;

import io.opensphere.server.source.OGCServerSource;

/** A server source model for common OGC services. */
public class OGCServerSourceModel extends DefaultServerSourceModel<OGCServerSource>
{
    /**
     * Constructor.
     */
    public OGCServerSourceModel()
    {
        super(Arrays.asList(OGCServerSource.WMS_SERVICE, OGCServerSource.WFS_SERVICE, OGCServerSource.WPS_SERVICE),
                Collections.singleton(OGCServerSource.WMS_GETMAP_SERVICE));
    }

    @Override
    public String getDescription()
    {
        return "A custom OGC server can provide tile (WMS), feature (WFS), and/or process (WPS) services.";
    }
}
