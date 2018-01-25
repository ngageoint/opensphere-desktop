package io.opensphere.wps.streaming;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.wps.source.WPSRequest;
import io.opensphere.wps.source.WPSRequestExecuter;
import io.opensphere.wps.source.WPSResponse;

/**
 * Mocks up a {@link WPSRequestExecuter} used for testing.
 */
public class MockWPSEnvoy implements WPSRequestExecuter
{
    /**
     * The expted request url.
     */
    private final String myExpectedUrl;

    /**
     * The expected server url.
     */
    private final String myServerUrl;

    /**
     * Indicates if the request function was called or not.
     */
    private boolean myWasRequestCalled;

    /**
     * The xml to return.
     */
    private final String myXmlToReturn;

    /**
     * Constructs a mock {@link WPSRequestExecuter}.
     *
     * @param serverUrl The expected server url.
     * @param expectedUrl The expected request url.
     * @param xmlToReturn The xml to return.
     */
    public MockWPSEnvoy(String serverUrl, String expectedUrl, String xmlToReturn)
    {
        myServerUrl = serverUrl;
        myExpectedUrl = expectedUrl;
        myXmlToReturn = xmlToReturn;
    }

    @Override
    public WPSResponse execute(WPSRequest request)
    {
        myWasRequestCalled = true;
        request.setBaseWpsUrl(myServerUrl);
        assertEquals(myExpectedUrl, request.createURLString());

        ByteArrayInputStream stream = null;

        WPSResponse response = null;
        if (myXmlToReturn != null)
        {
            stream = new ByteArrayInputStream(myXmlToReturn.getBytes(StringUtilities.DEFAULT_CHARSET));
            response = new WPSResponse(request, stream);
        }

        return response;
    }

    /**
     * Indicates if request was called.
     *
     * @return True if called false otherwise.
     */
    public boolean wasRequestedCalled()
    {
        return myWasRequestCalled;
    }
}
