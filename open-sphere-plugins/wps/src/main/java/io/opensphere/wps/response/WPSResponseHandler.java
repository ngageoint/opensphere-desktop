package io.opensphere.wps.response;

import io.opensphere.core.Toolbox;
import io.opensphere.wps.source.WPSRequest;
import io.opensphere.wps.source.WPSResponse;

/** Abstract WPS response handler class. */
public abstract class WPSResponseHandler
{
    /** The wps request. */
    private WPSRequest myRequest;

    /** The wps response. */
    private WPSResponse myResponse;

    /** The data type name. */
    private String myDataTypeName;

    /**
     * Constructor.
     *
     * @param response The wps response.
     */
    public WPSResponseHandler(WPSResponse response)
    {
        myResponse = response;
    }

    /**
     * Standard getter.
     *
     * @return The data type name.
     */
    public String getDataTypeName()
    {
        return myDataTypeName;
    }

    /**
     * Standard getter.
     *
     * @return The wps request.
     */
    public WPSRequest getRequest()
    {
        return myRequest;
    }

    /**
     * Standard getter.
     *
     * @return The wps response.
     */
    public WPSResponse getResponse()
    {
        return myResponse;
    }

    /**
     * Take appropriate actions and handle the response.
     *
     * @param toolbox The tool box.
     * @param name The name of the wps request.
     * @return Response object.
     */
    public abstract Object handleResponse(Toolbox toolbox, String name);

    /**
     * Standard setter.
     *
     * @param dataTypeName The data type name.
     */
    public void setDataTypeName(String dataTypeName)
    {
        myDataTypeName = dataTypeName;
    }

    /**
     * Standard setter.
     *
     * @param request The wps request.
     */
    public void setRequest(WPSRequest request)
    {
        myRequest = request;
    }

    /**
     * Standard setter.
     *
     * @param response The wps response.
     */
    public void setResponse(WPSResponse response)
    {
        myResponse = response;
    }
}
