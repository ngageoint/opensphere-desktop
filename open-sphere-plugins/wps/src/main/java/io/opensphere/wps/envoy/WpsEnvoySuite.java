package io.opensphere.wps.envoy;

/**
 * A group of envoys configured to access data for a specific identified server.
 */
public class WpsEnvoySuite
{
    /**
     * the identifier of the server for which to create the envoy suite.
     */
    private final String myServerId;

    /**
     * the envoy used to get the capabilities of the server.
     */
    private final WpsGetCapabilitiesEnvoy myGetCapabilitiesEnvoy;

    /**
     * the envoy used to describe a specific process type within the server.
     */
    private final WpsDescribeProcessTypeEnvoy myDescribeProcessTypeEnvoy;

    /**
     * Creates a new suite of envoys for the supplied server.
     *
     * @param pServerId the identifier of the server for which to create the envoy suite.
     * @param pGetCapabilitiesEnvoy the envoy used to get the capabilities of the server.
     * @param pDescribeProcessTypeEnvoy the envoy used to describe a specific process type within the server.
     */
    public WpsEnvoySuite(String pServerId, WpsGetCapabilitiesEnvoy pGetCapabilitiesEnvoy,
            WpsDescribeProcessTypeEnvoy pDescribeProcessTypeEnvoy)
    {
        myServerId = pServerId;
        myGetCapabilitiesEnvoy = pGetCapabilitiesEnvoy;
        myDescribeProcessTypeEnvoy = pDescribeProcessTypeEnvoy;
    }

    /**
     * Gets the value of the {@link #myDescribeProcessTypeEnvoy} field.
     *
     * @return the value stored in the {@link #myDescribeProcessTypeEnvoy} field.
     */
    public WpsDescribeProcessTypeEnvoy getDescribeProcessTypeEnvoy()
    {
        return myDescribeProcessTypeEnvoy;
    }

    /**
     * Gets the value of the {@link #myGetCapabilitiesEnvoy} field.
     *
     * @return the value stored in the {@link #myGetCapabilitiesEnvoy} field.
     */
    public WpsGetCapabilitiesEnvoy getGetCapabilitiesEnvoy()
    {
        return myGetCapabilitiesEnvoy;
    }

    /**
     * Gets the value of the {@link #myServerId} field.
     *
     * @return the value stored in the {@link #myServerId} field.
     */
    public String getServerId()
    {
        return myServerId;
    }
}
