package io.opensphere.core.common.connection;

import java.util.EventObject;

/**
 * Event object that contains details of the download
 *
 */
public class DownloadEventObject extends EventObject
{

    private static final long serialVersionUID = 1L;

    private long totalNumberBytes;

    private long numberBytesDownloaded;

    public DownloadEventObject(Object source)
    {
        super(source);
    }

    public long getTotalNumberBytes()
    {
        return totalNumberBytes;
    }

    void setTotalNumberBytes(long totalNumberBytes)
    {
        this.totalNumberBytes = totalNumberBytes;
    }

    public long getNumberBytesDownloaded()
    {
        return numberBytesDownloaded;
    }

    void setNumberBytesDownloaded(long numberBytesDownloaded)
    {
        this.numberBytesDownloaded = numberBytesDownloaded;
    }

}
