package io.opensphere.wps.response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.JAXBContextHelper;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.wps.source.WPSResponse;
import net.opengis.ows._110.ExceptionReport;
import net.opengis.ows._110.ExceptionType;
import net.opengis.wms._111.ServiceException;
import net.opengis.wms._111.ServiceExceptionReport;

/** The service error response type handler. */
public class WPSServiceErrorHandler extends WPSResponseHandler
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(WPSServiceErrorHandler.class);

    /**
     * Constructor.
     *
     * @param response The wps response.
     */
    public WPSServiceErrorHandler(WPSResponse response)
    {
        super(response);
    }

    @Override
    public Object handleResponse(Toolbox toolbox, String name)
    {
        JAXBContext jc;
        InputStream is = getResponse().getResponseStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StringUtilities.DEFAULT_CHARSET));
        StringBuilder errorString = new StringBuilder();
        try
        {
            br.mark(is.available() + 1);
            jc = JAXBContextHelper.getCachedContext(ExceptionReport.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            Object obj = unmarshaller.unmarshal(br);
            ExceptionReport ser = (ExceptionReport)obj;
            List<ExceptionType> exceptions = ser.getException();
            for (int i = 0; i < exceptions.size(); i++)
            {
                ExceptionType e = exceptions.get(i);
                errorString.append(e.getExceptionText().get(0));
                errorString.append('\n');
            }
        }
        catch (UnmarshalException ue)
        {
            LOGGER.info("Unable to extract WPS error message on first attempt.  "
                    + "Will try to parse error message again expecting another format");
            try
            {
                jc = JAXBContextHelper.getCachedContext(ServiceExceptionReport.class);
                br.reset();
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                Object obj = unmarshaller.unmarshal(br);
                ServiceExceptionReport ser = (ServiceExceptionReport)obj;
                List<ServiceException> exceptions = ser.getServiceException();
                for (int i = 0; i < exceptions.size(); i++)
                {
                    ServiceException e = exceptions.get(i);
                    errorString.append(e.getvalue().length() > 100 ? e.getvalue().substring(0, 99) + "..." : e.getvalue());

                    if (i + 1 < exceptions.size())
                    {
                        errorString.append('\n');
                    }

                    LOGGER.warn("Full WPS error message: " + e.getvalue());
                }
            }
            catch (JAXBException | IOException e)
            {
                LOGGER.error("An error occurred while attempting to parse WPS error message in second format", e);
            }
        }
        catch (JAXBException | IOException e)
        {
            LOGGER.error("Unable to parse any error message returned from the server.", e);
            errorString = new StringBuilder();
            errorString.append("An internal server error occurred");
        }
        return errorString.toString();
    }
}
