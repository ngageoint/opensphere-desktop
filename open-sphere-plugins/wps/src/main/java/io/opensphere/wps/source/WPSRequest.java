package io.opensphere.wps.source;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.wps.util.WPSConstants;

/**
 * The Class WPSRequest.
 */
@XmlType(name = "WPSRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class WPSRequest
{
    /** The logger. */
    @XmlTransient
    private static final Logger LOGGER = Logger.getLogger(WPSRequest.class);

    /** My base WPS URL. */
    @XmlElement(name = "baseWpsUrl", required = true)
    private String myBaseWpsUrl = "";

    /** My data inputs. */
    @XmlElement(name = "dataInput", required = true)
    private List<HashMapEntryType> myDataInputs = new ArrayList<>();

    /** My format. */
    @XmlElement(name = "format", required = true)
    private String myFormat = "";

    /** My identifier. */
    @XmlElement(name = "identifier", required = true)
    private String myIdentifier = "";

    /**
     * My raw data output. This is required by the specification, it's presence tells the server to stream data
     */
    @XmlElement(name = "rawDataOutput", required = true)
    private String myRawDataOutput = WPSConstants.RAW_DATA_OUTPUT;

    /** My request. */
    @XmlElement(name = "request", required = true)
    private String myRequest = "Execute";

    /** My service name. */
    @XmlElement(name = "service", required = true)
    private String myService = "WPS";

    /** My WPS version number. */
    @XmlElement(name = "version", required = true)
    private String myVersion = "1.0.0";

    /**
     * Instantiates a new wPS request.
     */
    public WPSRequest()
    {
    }

    /**
     * Create a url string.
     *
     * @return The url string.
     */
    public String createURLString()
    {
        StringBuffer url = new StringBuffer(myBaseWpsUrl);
        url.append("?service=").append(myService);
        url.append("&version=").append(myVersion);
        url.append("&request=").append(myRequest);
        url.append("&identifier=").append(myIdentifier);

        appendFormat(url);

        if (StringUtils.isNotEmpty(myRawDataOutput))
        {
            String rawData = "@mimetype=" + myFormat;
            if (StringUtils.isNotEmpty(myFormat) && !myRawDataOutput.contains(rawData))
            {
                myRawDataOutput += rawData;
            }
            try
            {
                // need to URL encoded for "text/xml; subtype=gml/3.1.1"
                // and possibly others
                url.append("&rawdataoutput=").append(URLEncoder.encode(myRawDataOutput, "ISO-8859-1"));
            }
            catch (UnsupportedEncodingException e)
            {
                LOGGER.warn("Unable to encode RAWDATAOUTPUT parameter in URL", e);
                url.append("&rawdataoutput=").append(myRawDataOutput);
            }
        }

        if (!myDataInputs.isEmpty())
        {
            url.append("&datainputs=");
            StringBuilder inputs = new StringBuilder();
            for (HashMapEntryType entry : myDataInputs)
            {
                String key = entry.getKey();
                String value = entry.getValue();
                // Take care of data types in the form
                // 'serverName!!dataTypeName'

                if (key.equals(WPSConstants.BBOX))
                {
                    String[] bboxes = value.split(";");
                    for (String bboxString : bboxes)
                    {
                        inputs.append(key).append('=').append(bboxString).append(';');
                    }
                }
                else
                {
                    inputs.append(key).append('=').append(value).append(';');
                }
            }

            try
            {
                // decode in case any of the inputs were encoded
                String decoded = URLDecoder.decode(inputs.toString(), "UTF8");

                // URL Encode in case there are any special characters
                String encoded = URLEncoder.encode(decoded, "UTF8");
                url.append(encoded);
            }
            catch (UnsupportedEncodingException e)
            {
                LOGGER.error(null, e);
            }
        }

        return url.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        WPSRequest other = (WPSRequest)obj;
        //@formatter:off
        return Objects.equals(myService, other.myService)
                && Objects.equals(myVersion, other.myVersion)
                && Objects.equals(myRequest, other.myRequest)
                && Objects.equals(myIdentifier, other.myIdentifier)
                && Objects.equals(myFormat, other.myFormat)
                && Objects.equals(myBaseWpsUrl, other.myBaseWpsUrl)
                && Objects.equals(myRawDataOutput, other.myRawDataOutput)
                && Objects.equals(myDataInputs, other.myDataInputs);
        //@formatter:on
    }

    /**
     * Gets the base wps url.
     *
     * @return the base wps url
     */
    public String getBaseWpsUrl()
    {
        return myBaseWpsUrl;
    }

    /**
     * Gets the data input.
     *
     * @return the data input
     */
    public List<HashMapEntryType> getDataInput()
    {
        return myDataInputs;
    }

    /**
     * Gets the format.
     *
     * @return the format
     */
    public String getFormat()
    {
        return myFormat;
    }

    /**
     * Gets the identifier.
     *
     * @return the identifier
     */
    public String getIdentifier()
    {
        return myIdentifier;
    }

    /**
     * Gets the raw data output.
     *
     * @return the raw data output
     */
    public String getRawDataOutput()
    {
        return myRawDataOutput;
    }

    /**
     * Gets the request.
     *
     * @return the request
     */
    public String getRequest()
    {
        return myRequest;
    }

    /**
     * Gets the service.
     *
     * @return the service
     */
    public String getService()
    {
        return myService;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public String getVersion()
    {
        return myVersion;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myService);
        result = prime * result + HashCodeHelper.getHashCode(myVersion);
        result = prime * result + HashCodeHelper.getHashCode(myRequest);
        result = prime * result + HashCodeHelper.getHashCode(myIdentifier);
        result = prime * result + HashCodeHelper.getHashCode(myFormat);
        result = prime * result + HashCodeHelper.getHashCode(myBaseWpsUrl);
        result = prime * result + HashCodeHelper.getHashCode(myRawDataOutput);
        result = prime * result + HashCodeHelper.getHashCode(myDataInputs);
        return result;
    }

    /**
     * Sets the base wps url.
     *
     * @param baseWpsUrl the new base wps url
     */
    public void setBaseWpsUrl(String baseWpsUrl)
    {
        myBaseWpsUrl = baseWpsUrl;
    }

    /**
     * Sets the data input.
     *
     * @param dataInput the new data input
     */
    public void setDataInput(List<HashMapEntryType> dataInput)
    {
        myDataInputs = dataInput;
    }

    /**
     * Sets the format.
     *
     * @param format the new format
     */
    public void setFormat(String format)
    {
        myFormat = format;
    }

    /**
     * Sets the identifier.
     *
     * @param identifier the new identifier
     */
    public void setIdentifier(String identifier)
    {
        myIdentifier = identifier;
    }

    /**
     * Sets the raw data output.
     *
     * @param rawDataOutput the new raw data output
     */
    public void setRawDataOutput(String rawDataOutput)
    {
        myRawDataOutput = rawDataOutput;
    }

    /**
     * Sets the request.
     *
     * @param request the new request
     */
    public void setRequest(String request)
    {
        myRequest = request;
    }

    /**
     * Sets the service.
     *
     * @param service the new service
     */
    public void setService(String service)
    {
        myService = service;
    }

    /**
     * Sets the version.
     *
     * @param version the new version
     */
    public void setVersion(String version)
    {
        myVersion = version;
    }

    /**
     * Appends the format parameter to the url.
     *
     * @param url The buffer to append to.
     */
    private void appendFormat(StringBuffer url)
    {
        if (StringUtils.isNotEmpty(myFormat))
        {
            String formatStr = null;
            try
            {
                // need to URL encoded for "text/xml; subtype=gml/3.1.1"
                // and possibly others
                formatStr = "&FORMAT=" + URLEncoder.encode(myFormat, "ISO-8859-1") + "&MAXFEATURES=100000";
            }
            catch (UnsupportedEncodingException e)
            {
                LOGGER.warn("Unable to encode FORMAT parameter in URL", e);
                formatStr = "&FORMAT=" + myFormat + "&MAXFEATURES=100000";
            }

            url.append(formatStr);
        }
    }
}
