package io.opensphere.core.common.connection;

import java.util.EventListener;

/**
 * Listener to use in doing file downloads/reachbacks. Implement this interface
 * to create a listener that will be notified of download update events
 *
 */
public interface DownloadEventListener extends EventListener
{
    /**
     * Fired when a download update occurs
     *
     * @param deo Details of the download event
     */
    public void downloadUpdateOccurred(DownloadEventObject deo);

}
