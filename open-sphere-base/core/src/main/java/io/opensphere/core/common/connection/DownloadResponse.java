package io.opensphere.core.common.connection;

import java.io.InputStream;

/**
 * Data structure to encapsulate the download response from the server
 */
public class DownloadResponse
{
    private static final String CONTENT_TYPE = "application/zip";

    private InputStream downloadStream = null;

    private String errorMessage = null;

    private String contentType = CONTENT_TYPE;

    private long contentLength = 0L;

    public InputStream getDownloadStream()
    {
        return downloadStream;
    }

    public void setDownloadStream(InputStream downloadStream)
    {
        this.downloadStream = downloadStream;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String fileType)
    {
        this.contentType = fileType;
    }

    public long getContentLength()
    {
        return contentLength;
    }

    public void setContentLength(long contentLength)
    {
        this.contentLength = contentLength;
    }

}
