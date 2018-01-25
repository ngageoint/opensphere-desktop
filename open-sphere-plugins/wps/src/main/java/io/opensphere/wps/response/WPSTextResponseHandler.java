package io.opensphere.wps.response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.wps.source.WPSResponse;

/** The text response type handler. */
public class WPSTextResponseHandler extends WPSResponseHandler
{
    /** Static logging reference. */
    private static final Logger LOGGER = Logger.getLogger(WPSTextResponseHandler.class);

    /**
     * Constructor.
     *
     * @param response The wps response.
     */
    public WPSTextResponseHandler(WPSResponse response)
    {
        super(response);
    }

    @Override
    public Object handleResponse(Toolbox toolbox, String name)
    {
        InputStream is = getResponse().getResponseStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StringUtilities.DEFAULT_CHARSET));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                sb.append(line).append('\n');
            }
        }
        catch (IOException e)
        {
            LOGGER.error("IOException: " + e.getMessage());
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch (IOException e)
            {
                LOGGER.error("IOException: " + e.getMessage());
            }
        }
        return sb.toString();
///        return sb.toString() == null ? "" : sb.toString();
    }
}
