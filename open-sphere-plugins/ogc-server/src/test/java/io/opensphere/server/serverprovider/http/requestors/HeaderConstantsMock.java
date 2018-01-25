package io.opensphere.server.serverprovider.http.requestors;

import io.opensphere.server.serverprovider.http.header.HeaderValues;

/**
 * A mock header constants.
 *
 */
public class HeaderConstantsMock implements HeaderValues
{
    @Override
    public String getEncoding()
    {
        return "gzip,default";
    }

    @Override
    public String getZippedEncoding()
    {
        return "gzip";
    }

    @Override
    public String getUserAgent()
    {
        return "useragent";
    }

    @Override
    public String getAccept()
    {
        return "accept";
    }
}
