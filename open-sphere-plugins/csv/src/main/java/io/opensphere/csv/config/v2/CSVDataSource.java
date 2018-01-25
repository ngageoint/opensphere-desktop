package io.opensphere.csv.config.v2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.filesystem.FileUtilities;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.importer.config.ImportDataSource;

/**
 * Data source configuration for a CSV.
 */
@XmlRootElement(name = "CSVDataSource")
@XmlAccessorType(XmlAccessType.NONE)
public class CSVDataSource extends ImportDataSource
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CSVDataSource.class);

    /** The parse parameters. */
    @XmlElement(name = "parseParameters", required = true)
    private CSVParseParameters myParseParameters;

    /**
     * The local file location. When myFilePath is a local file, these should be
     * the same, however they may differ when the file's real location is not
     * local.
     */
    @XmlTransient
    private String myFileLocalPath;

    /**
     * JAXB Constructor.
     */
    public CSVDataSource()
    {
    }

    /**
     * Constructor.
     *
     * @param sourceUri The data source URI
     */
    public CSVDataSource(URI sourceUri)
    {
        super(sourceUri);
        myParseParameters = new CSVParseParameters();
    }

    /**
     * Gets the parseParameters.
     *
     * @return the parseParameters
     */
    public CSVParseParameters getParseParameters()
    {
        return myParseParameters;
    }

    /**
     * Sets the parseParameters.
     *
     * @param parseParameters the parseParameters
     */
    public void setParseParameters(CSVParseParameters parseParameters)
    {
        myParseParameters = parseParameters;
    }

    /**
     * Common paradigm:  assign the local variable and also return its value.
     * @param flp new value of myFileLocalPath
     * @return see above
     */
    private String setFlp(String flp)
    {
        myFileLocalPath = flp;
        return myFileLocalPath;
    }

    /**
     * Get the path to the file on the local file system, creating it if
     * necessary. Creation can happen when the file is in a remote location such
     * as over the network.
     *
     * @param toolbox The system toolbox. This is used to for connecting to a
     *            url to download the file if necessary. When the file is known
     *            to have already been made available locally, this may be null.
     * @return the fileLocalPath
     */
    public String getFileLocalPath(Toolbox toolbox)
    {
        if (StringUtils.isNotEmpty(myFileLocalPath))
        {
            return myFileLocalPath;
        }
        String uriStr = getSourceUriString();
        if (StringUtils.isEmpty(uriStr))
        {
            return myFileLocalPath;
        }
        URI srcUri = getSourceUri();
        URL srcUrl;
        try
        {
            srcUrl = srcUri.toURL();
        }
        catch (MalformedURLException eek)
        {
            // If this isn't a URL then it is a regular file path.
            return setFlp(srcUri.getSchemeSpecificPart());
        }
        // If this URL is actual a local file, process it like a normal local file.
        if (UrlUtilities.isFile(srcUrl))
        {
            return setFlp(srcUri.getSchemeSpecificPart());
        }

        StringBuilder builder = new StringBuilder(FileUtilities.getDirectory(null, uriStr.hashCode()));
        builder.append(File.separator).append("generated.csv");
        myFileLocalPath = builder.toString();

        File localFile = new File(myFileLocalPath);
        // Only write the file if it doesn't exist.
        if (localFile.exists() || toolbox == null)
        {
            return myFileLocalPath;
        }

        ResponseValues response = new ResponseValues();
        ServerProvider<HttpServer> provider = toolbox.getServerProviderRegistry().getProvider(HttpServer.class);
        try (FileOutputStream outStream = new FileOutputStream(localFile);
                InputStream inputStream = provider.getServer(srcUrl).sendGet(srcUrl, response))
        {
            if (response.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                new StreamReader(inputStream).readStreamToOutputStream(outStream);
            }
        }
        catch (IOException | URISyntaxException e)
        {
            LOGGER.error("Write temp file for remote CSV." + e, e);
        }

        return myFileLocalPath;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + HashCodeHelper.getHashCode(myParseParameters);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        CSVDataSource other = (CSVDataSource)obj;
        return Objects.equals(myParseParameters, other.myParseParameters);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(256);
        builder.append("CSVDataSource [myParseParameters=");
        builder.append(myParseParameters);
        builder.append(", super=");
        builder.append(super.toString());
        builder.append(']');
        return builder.toString();
    }

    @Override
    public CSVDataSource clone()
    {
        CSVDataSource result = (CSVDataSource)super.clone();
        result.myParseParameters = myParseParameters.clone();
        return result;
    }

    /**
     * Generate type key.
     *
     * @return the string
     */
    @Override
    public String generateTypeKey()
    {
        return StringUtilities.concat("CSV::", getName(), "::", toString(getSourceUri()));
    }

    /**
     * Convenience method for getting the list of column names to ignore.
     *
     * @return the column names to ignore
     */
    public Set<String> getColumnFilter()
    {
        return New.set(StreamUtilities.map(myParseParameters.getColumnsToIgnore(),
            index -> myParseParameters.getColumnNames().get(index.intValue())));
    }
}
