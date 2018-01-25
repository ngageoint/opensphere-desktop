package io.opensphere.server.toolbox.impl;

import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.toolbox.ServerLabelGenerator;

/**
 * A default implementation of the {@link ServerLabelGenerator} interface, in
 * which generic OGC servers are supported.
 */
public class DefaultServerLabelGenerator implements ServerLabelGenerator
{
    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.server.toolbox.ServerLabelGenerator#buildLabelFromType(java.lang.String)
     */
    @Override
    public String buildLabelFromType(String type)
    {
        if (ServerCustomization.DEFAULT_TYPE.equals(type))
        {
            return "Generic OGC Server";
        }
        else if (type.trim().toLowerCase().endsWith("server"))
        {
            return type;
        }
        return StringUtilities.concat(type, " Server");
    }
}
