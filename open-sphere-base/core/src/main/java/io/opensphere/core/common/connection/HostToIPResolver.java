package io.opensphere.core.common.connection;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Class HostToIPResolver.
 *
 * Stores an internal map of host name to IP address so that a URL can be
 * re-written to contain the ipAddress only to assist OGCServerConnection to
 * have the same connection for a multiple host names that resolve to the same
 * ipAddress.
 */
public class HostToIPResolver
{

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactory.getLog(HostToIPResolver.class);

    /** The singleton instance resolver. */
    private static HostToIPResolver ourResolver;

    /** The server name to ip address map. */
    private Map<String, InetAddress> myServerNameToIPAddressMap;

    /** The my add host lock. */
    private ReentrantLock myAddHostLock;

    /**
     * Returns the singleton instance of the resolver.
     *
     * @return the {@link HostToIPResolver}
     */
    public static synchronized HostToIPResolver instance()
    {
        if (ourResolver == null)
        {
            ourResolver = new HostToIPResolver();
        }
        return ourResolver;
    }

    /**
     * Instantiates a new {@link HostToIPResolver}.
     */
    private HostToIPResolver()
    {
        myServerNameToIPAddressMap = new ConcurrentHashMap<String, InetAddress>();
        myAddHostLock = new ReentrantLock();
    }

    /**
     * Clears the internal host to IP map.
     */
    public void clearMap()
    {
        myServerNameToIPAddressMap.clear();
    }

    /**
     * Resolves the host name to an ip address and stores the mapping.
     *
     *
     * @param hostName the host name to add
     * @return the {@link InetAddress} of the host name
     * @throws UnknownHostException the unknown host exception
     */
    public InetAddress addHost(String hostName) throws UnknownHostException
    {
        // Check the cache first.
        InetAddress result = myServerNameToIPAddressMap.get(hostName);
        if (result == null)
        {
            myAddHostLock.lock();
            try
            {
                // Check cache again to see if it was added when we
                // were waiting on the lock.
                result = myServerNameToIPAddressMap.get(hostName);

                if (result == null)
                {
                    // If not in the cache look up and add to cache.
                    InetAddress addresses = InetAddress.getByName(hostName);
                    myServerNameToIPAddressMap.put(hostName, addresses);
                    result = addresses;
                }
            }
            finally
            {
                myAddHostLock.unlock();
            }
        }
        return result;
    }

    /**
     * Resolves the host name contained in the URL to an ip address and stores
     * the mapping
     *
     * @param aURL the a {@link URL} from which to extract the host name
     * @return the {@link InetAddress} of the host
     * @throws UnknownHostException the unknown host exception
     */
    public InetAddress addHost(URL aURL) throws UnknownHostException
    {
        return addHost(aURL.getHost());
    }

    /**
     * Rewrites the provided url with the host name replaced with the host IP
     * Address. If the resolver has not yet encountered the host name in the
     * provided URL the mapping will be stored in the internal map to speed
     * lookup in the future.
     *
     * @param aURL the a {@link URL} to be rewritten
     * @return the the rewritten {@link URL} that contains the IP address
     * @throws UnknownHostException the unknown host exception
     */
    public URL rewriteURLWithHostIP(URL aURL) throws UnknownHostException
    {
        String urlAsString = aURL.toExternalForm();
        URL result = null;

        String host = aURL.getHost();
        InetAddress address = myServerNameToIPAddressMap.get(host);
        if (address == null)
        {
            address = addHost(host);
        }
        String newURL = urlAsString.replaceFirst(host, address.getHostAddress());
        try
        {
            result = new URL(newURL);
        }
        catch (MalformedURLException e)
        {
            LOGGER.error(e);
        }
        return result;
    }

    /**
     * Transforms the string into a URL and rewrites it to remove the provided
     * host name and replace it with the host's IP address. If the resolver has
     * not yet encountered the host name in the provided URL the mapping will be
     * stored in the internal map to speed lookup in the future.
     *
     * @param aURL the string representation of a URL to be rewritten
     * @return the rewritten URL as a string
     * @throws UnknownHostException the unknown host exception
     * @throws MalformedURLException if the provided url string is invalid
     */
    public String rewriteURLWithHostIP(String aURL) throws UnknownHostException, MalformedURLException
    {
        URL orig = new URL(aURL);
        URL newURL = rewriteURLWithHostIP(orig);
        return newURL.toExternalForm();
    }

    /**
     * Gets the InetAddress for a given host name if it exists in the map. If
     * not in the map it will be looked up and added to the map.
     *
     * @param hostName the host name
     * @return the address for host
     * @throws UnknownHostException the unknown host exception
     */
    public InetAddress getAddressForHost(String hostName) throws UnknownHostException
    {
        return addHost(hostName);
    }

    /**
     * Gets the ip address as a string for a given host name if it exists in the
     * map. If not in the map it will be looked up and added to the map.
     *
     * @param hostName the host name
     * @return the address for host as a string
     * @throws UnknownHostException the unknown host exception
     */
    public String getAddressAsStringForHost(String hostName) throws UnknownHostException
    {
        return addHost(hostName).getHostAddress();
    }

    /* (non-Javadoc)
     *
     * @see java.lang.Object#toString() */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName()).append(":\n");
        for (Map.Entry<String, InetAddress> entry : myServerNameToIPAddressMap.entrySet())
        {
            sb.append("   IP/Host: ").append(entry.getValue().getHostAddress()).append(" / ").append(entry.getKey()).append("\n");
        }
        return sb.toString();
    }
}
